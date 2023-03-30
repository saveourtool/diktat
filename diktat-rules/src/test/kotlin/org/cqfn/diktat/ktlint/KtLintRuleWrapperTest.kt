package org.cqfn.diktat.ktlint

import org.cqfn.diktat.common.config.rules.qualifiedWithRuleSetId
import org.cqfn.diktat.ktlint.KtLintRuleSetProviderV2Wrapper.Companion.toKtLint
import org.cqfn.diktat.ktlint.KtLintRuleWrapper.Companion.delegatee
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.rules.DiktatRuleSet
import com.pinterest.ktlint.core.Code
import com.pinterest.ktlint.core.KtLintRuleEngine
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleProvider
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
        val ruleSetId = "id"

        val rule1 = mockRule(id = "rule-first".qualifiedWithRuleSetId(ruleSetId))
        val rule2 = mockRule(id = "rule-second".qualifiedWithRuleSetId(ruleSetId))

        val orderedRuleProviders = DiktatRuleSet(listOf(rule1, rule2)).toKtLint()

        val orderedRuleProviderIterator = orderedRuleProviders.iterator()
        val orderedRule1 = orderedRuleProviderIterator.next().createNewRuleInstance()
        val orderedRule2 = orderedRuleProviderIterator.next().createNewRuleInstance()
        Assertions.assertFalse(orderedRuleProviderIterator.hasNext(), "Extra elements after ordering")

        Assertions.assertEquals(rule1, orderedRule1.delegatee(), "First rule is modified")

        orderedRule2.visitorModifiers
            .filterIsInstance<Rule.VisitorModifier.RunAfterRule>()
            .also {
                Assertions.assertEquals(1, it.size,
                    "Found invalid count of Rule.VisitorModifier.RunAfterRule")
            }
            .first()
            .let {
                Assertions.assertEquals(rule1.id.qualifiedWithRuleSetId(ruleSetId), it.ruleId,
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
        val ruleSetId = "id"
        val rules: List<DiktatRule> = sequenceOf("ccc", "bbb", "aaa").map { ruleId ->
            mockRule(
                id = ruleId.qualifiedWithRuleSetId(ruleSetId),
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
        assertThat(ruleProviders.map(RuleProvider::createNewRuleInstance).map(Rule::id)).containsExactlyElementsOf(rules.map(DiktatRule::id))

        @Language("kotlin")
        val code = "fun foo() { }"

        KtLintRuleEngine(
            ruleProviders = ruleProviders
        ).lint(
            code = Code.CodeSnippet(
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
         *
         * val expectedRuleInvocationOrder = rules.asSequence()
         *     .map(Rule::id)
         *     .flatMap { ruleId ->
         *         generateSequence { ruleId }.take(astNodeCount)
         *     }
         *     .toList()
         */
        val expectedRuleInvocationOrder = generateSequence {
            rules.map(DiktatRule::id)
        }
            .take(astNodeCount)
            .flatten()
            .toList()

        assertThat(actualRuleInvocationOrder)
            .containsExactlyElementsOf(expectedRuleInvocationOrder)
    }

    companion object {
        private fun mockRule(
            id: String,
            onVisit: (DiktatRule) -> Unit = { }
        ): DiktatRule = object : DiktatRule(id.qualifiedWithRuleSetId(), emptyList(), emptyList()) {
            override fun logic(node: ASTNode) {
                onVisit(this)
            }
        }
    }
}
