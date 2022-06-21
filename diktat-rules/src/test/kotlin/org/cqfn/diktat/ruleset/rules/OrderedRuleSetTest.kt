package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.ruleset.constants.EmitType
import com.pinterest.ktlint.core.Rule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class OrderedRuleSetTest {

    @Test
    fun `check OrderedRule with VisitorModifier RunAfterRule`() {
        val rule = object : Rule("rule") {
            override fun visit(
                node: ASTNode,
                autoCorrect: Boolean,
                emit: EmitType
            ) {
                // do nothing
            }
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            OrderedRuleSet("duplicate", rule, rule)
        }

        val ruleWithRunAfterRule = object : Rule("invalid-rule", setOf(VisitorModifier.RunAfterRule("another-rule"))) {
            override fun visit(
                node: ASTNode,
                autoCorrect: Boolean,
                emit: EmitType
            ) {
                // do nothing
            }
        }
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
        val rule1 = object : Rule("rule-first") {
            override fun visit(
                node: ASTNode,
                autoCorrect: Boolean,
                emit: EmitType
            ) {
                // do nothing
            }
        }
        val rule2 = object : Rule("rule-second") {
            override fun visit(
                node: ASTNode,
                autoCorrect: Boolean,
                emit: EmitType
            ) {
                // do nothing
            }
        }

        val ruleSetId = "id"
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
                Assertions.assertEquals(ruleSetId + ":" + rule1.id, it.ruleId,
                    "Invalid ruleId in Rule.VisitorModifier.RunAfterRule")
            }
    }
}
