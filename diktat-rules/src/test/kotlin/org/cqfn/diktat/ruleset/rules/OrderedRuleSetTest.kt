package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.qualifiedWithRuleSetId
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.util.TEST_FILE_NAME
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
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
    fun `KtLint keeps order with RuleVisitorModifierRunAfterRule`() {
        val ruleIdOrder: MutableList<String> = mutableListOf()
        val onVisit: (Rule) -> Unit = { rule ->
            ruleIdOrder += rule.id
        }
        val ruleSetId = "id"
        val rule1 = mockRule(id = "ccc".qualifiedWithRuleSetId(ruleSetId), onVisit = onVisit)
        val rule2 = mockRule(id = "bbb".qualifiedWithRuleSetId(ruleSetId), onVisit = onVisit)
        val rule3 = mockRule(id = "aaa".qualifiedWithRuleSetId(ruleSetId), onVisit = onVisit)
        val expectedRuleIdOrder = listOf(rule1, rule2, rule3).map { it.id }

        val ruleSet = OrderedRuleSet(ruleSetId, rule1, rule2, rule3)

        KtLint.lint(
            KtLint.ExperimentalParams(
                fileName = TEST_FILE_NAME,
                text = "fun foo() { }",
                ruleSets = listOf(ruleSet),
                cb = { _, _ -> },
            )
        )

        val rulesSize = expectedRuleIdOrder.size
        Assertions.assertTrue(ruleIdOrder.size % rulesSize == 0, "Rules are called several times but together")
        for (repeat in 0 until (ruleIdOrder.size / rulesSize)) {
            // check each run for each node
            Assertions.assertEquals(expectedRuleIdOrder, ruleIdOrder.subList(repeat * rulesSize, (repeat + 1) * rulesSize))
        }
    }

    companion object {
        private fun mockRule(
            id: String,
            visitorModifiers: Set<Rule.VisitorModifier> = emptySet(),
            onVisit: (Rule) -> Unit = { }
        ) = object : Rule(id.qualifiedWithRuleSetId(), visitorModifiers) {
            override fun beforeVisitChildNodes(
                node: ASTNode,
                autoCorrect: Boolean,
                emit: EmitType
            ) {
                onVisit(this)
            }
        }
    }
}
