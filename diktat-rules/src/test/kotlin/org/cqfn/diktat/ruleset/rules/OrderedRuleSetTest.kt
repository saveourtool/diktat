package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.qualifiedWithRuleSetId
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.util.TEST_FILE_NAME
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class OrderedRuleSetTest {
    @Test
    fun `check OrderedRule with VisitorModifier RunAfterRule`() {
        val rule = mockRule("rule")
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            OrderedRuleSet("duplicate", rule, rule)
        }

        val ruleWithRunAfterRule = mockRule("invalid-rule", setOf(Rule.VisitorModifier.RunAfterRule("another-rule")))
        // validate that second rule which will be modified doesn't contain VisitorModifier.RunAfterRule
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            OrderedRuleSet("visitor-modifier", rule, ruleWithRunAfterRule)
        }
        // validate that first rule which won't be modified doesn't contain VisitorModifier.RunAfterRule
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            OrderedRuleSet("visitor-modifier", ruleWithRunAfterRule, rule)
        }
    }

    @Test
    fun `check OrderedRule`() {
        val ruleSetId = "id"

        val rule1 = mockRule(id = "rule-first".qualifiedWithRuleSetId(ruleSetId))
        val rule2 = mockRule(id = "rule-second".qualifiedWithRuleSetId(ruleSetId))

        val orderedRuleSet = OrderedRuleSet(ruleSetId, rule1, rule2)

        val orderedRuleSetIterator = orderedRuleSet.iterator()
        val orderedRule1 = orderedRuleSetIterator.next()
        val orderedRule2 = orderedRuleSetIterator.next()
        Assertions.assertFalse(orderedRuleSetIterator.hasNext(), "Extra elements after ordering")

        Assertions.assertEquals(rule1, orderedRule1, "First rule is modified")

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
        val onVisit: (Rule) -> Unit = { rule ->
            actualRuleInvocationOrder += rule.id
        }
        val ruleSetId = "id"
        val rules: List<Rule> = sequenceOf("ccc", "bbb", "aaa").map { ruleId ->
            mockRule(
                id = ruleId.qualifiedWithRuleSetId(ruleSetId),
                onVisit = onVisit
            )
        }.toList()
        assertThat(rules).isNotEmpty

        /*
         * Make sure the rules are not sorted by id.
         */
        val rulesOrderedById: List<Rule> = rules.sortedBy(Rule::id)
        assertThat(rules).containsExactlyInAnyOrder(*rulesOrderedById.toTypedArray())
        assertThat(rules).isNotEqualTo(rulesOrderedById)

        /*
         * Make sure OrderedRuleSet preserves the order.
         */
        val ruleSet = OrderedRuleSet(ruleSetId, *rules.toTypedArray())
        assertThat(ruleSet.rules.map(Rule::id)).containsExactlyElementsOf(rules.map(Rule::id))

        @Language("kotlin")
        val code = "fun foo() { }"

        KtLint.lint(
            KtLint.ExperimentalParams(
                fileName = TEST_FILE_NAME,
                text = code,
                ruleSets = listOf(ruleSet),
                cb = { _, _ -> },
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
            .map(Rule::id)
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
            visitorModifiers: Set<Rule.VisitorModifier> = emptySet(),
            onVisit: (Rule) -> Unit = { }
        ): Rule = object : Rule(id.qualifiedWithRuleSetId(), visitorModifiers) {
            override fun visit(
                node: ASTNode,
                autoCorrect: Boolean,
                emit: EmitType
            ) {
                onVisit(this)
            }
        }
    }
}
