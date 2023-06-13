package com.saveourtool.diktat.ktlint

import com.saveourtool.diktat.api.DiktatErrorEmitter
import com.saveourtool.diktat.api.DiktatRule
import com.saveourtool.diktat.api.DiktatRuleSet
import com.saveourtool.diktat.ktlint.KtLintRuleWrapper.Companion.toKtLint
import com.saveourtool.diktat.ktlint.KtLintRuleWrapper.Companion.unwrap
import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class KtLintRuleWrapperTest {
    @Test
    fun `check KtLintRuleSetWrapper with duplicate`() {
        val rule = mockRule("rule")
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            DiktatRuleSet(listOf(rule, rule)).toKtLint()
        }
    }

    @Test
    fun `check OrderedRule`() {
        val rule1 = mockRule(id = "rule-first")
        val rule2 = mockRule(id = "rule-second")

        val orderedRuleProviders = DiktatRuleSet(listOf(rule1, rule2)).toKtLint()

        val orderedRuleProviderIterator = orderedRuleProviders.iterator()
        val orderedRule1 = orderedRuleProviderIterator.next().createNewRuleInstance()
        val orderedRule2 = orderedRuleProviderIterator.next().createNewRuleInstance()
        Assertions.assertFalse(orderedRuleProviderIterator.hasNext(), "Extra elements after ordering")

        Assertions.assertEquals(rule1, orderedRule1.unwrap(), "First rule is modified")

        orderedRule2.visitorModifiers
            .filterIsInstance<Rule.VisitorModifier.RunAfterRule>()
            .also {
                Assertions.assertEquals(1, it.size,
                    "Found invalid count of Rule.VisitorModifier.RunAfterRule")
            }
            .first()
            .let {
                Assertions.assertEquals(rule1.id.toRuleId(), it.ruleId,
                    "Invalid ruleId in Rule.VisitorModifier.RunAfterRule")
            }
    }

    @Test
    @Suppress("TOO_LONG_FUNCTION")
    fun `KtLint keeps order with RuleVisitorModifierRunAfterRule`() {
        val actualRuleInvocationOrder: MutableList<String> = mutableListOf()
        val onVisit: (DiktatRule) -> Unit = { rule ->
            actualRuleInvocationOrder += rule.id
        }
        val rules: List<DiktatRule> = sequenceOf("ccc", "bbb", "aaa").map { ruleId ->
            mockRule(
                id = ruleId,
                onVisit = onVisit
            )
        }.toList()
        assertThat(rules).isNotEmpty

        /*
         * Make sure the rules are not sorted by id.
         */
        val rulesOrderedById: List<DiktatRule> = rules.sortedBy(DiktatRule::id)
        assertThat(rules).containsExactlyInAnyOrder(*rulesOrderedById.toTypedArray())
        assertThat(rules).isNotEqualTo(rulesOrderedById)

        /*
         * Make sure OrderedRuleSet preserves the order.
         */
        val ruleProviders = DiktatRuleSet(rules).toKtLint()
        assertThat(ruleProviders.map(RuleProvider::createNewRuleInstance).map(Rule::ruleId))
            .containsExactlyElementsOf(rules.map(DiktatRule::id).map { it.toRuleId() })

        @Language("kotlin")
        val code = "fun foo() { }"

        KtLintRuleEngine(
            ruleProviders = ruleProviders
        ).lint(
            code = Code.fromSnippet(
                content = code
            )
        )

        val ruleCount = rules.size
        assertThat(actualRuleInvocationOrder)
            .describedAs("The ordered list of rule invocations")
            .matches({ order ->
                order.size % ruleCount == 0
            }, "has a size which is multiple of $ruleCount")

        /*
         * This is the count of AST nodes in `code` above.
         */
        val astNodeCount = actualRuleInvocationOrder.size / ruleCount

        /*-
         * This is new in ktlint 0.47.
         * Previously, rules were applied in this sequence:
         *
         * A -> B -> C (File)
         *      |
         *      V
         * A -> B -> C (Node)
         *      |
         *      V
         * A -> B -> C (Leaf)
         *
         * Now, each rule is recursively applied to all AST nodes, and then the
         * control is passed to the next rule:
         *
         * A(File) -> A(Node) -> A(Leaf)
         *            |
         *            V
         * B(File) -> B(Node) -> B(Leaf)
         *            |
         *            V
         * C(File) -> C(Node) -> C(Leaf)
         */
         val expectedRuleInvocationOrder = rules.asSequence()
              .map(DiktatRule::id)
              .flatMap { ruleId ->
                  generateSequence { ruleId }.take(astNodeCount)
              }
              .toList()

        assertThat(actualRuleInvocationOrder)
            .containsExactlyElementsOf(expectedRuleInvocationOrder)
    }

    companion object {
        private fun mockRule(
            id: String,
            onVisit: (DiktatRule) -> Unit = { }
        ): DiktatRule = object : DiktatRule {
            override val id: String
                get() = id
            override fun invoke(node: ASTNode, autoCorrect: Boolean, emitter: DiktatErrorEmitter) {
                onVisit(this)
            }
        }
    }
}
