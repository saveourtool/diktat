package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.ruleset.constants.EmitType
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * This is a wrapper around Ktlint RuleSet which adjusts visitorModifiers for all rules to keep order with prevRule
 * Added as a workaround after introducing a new logic for sorting KtLint Rules: https://github.com/pinterest/ktlint/issues/1478
 *
 * @param id ID of RuleSet
 * @param rules rules which belongs to current RuleSet
 */
class OrderedRuleSet(id: String, vararg rules: Rule) : RuleSet(id, rules = rules) {

    private val orderedIterator: Iterator<Rule> = adjustRules(id, listOf(super.iterator())).iterator()

    /**
     * @return ordered iterator of rules
     */
    override fun iterator(): Iterator<Rule> {
        return orderedIterator
    }

    companion object {
        private fun adjustRules(ruleSetId: String, rules: Sequence<Rule>): Sequence<Rule> {
            return rules.take(1) +
                    rules.zipWithNext { prevRule, rule -> OrderedRule(ruleSetId, rule, prevRule) }
        }

        private class OrderedRule(ruleSetId: String, val rule: Rule, prevRule: Rule) : Rule(rule.id, adjustVisitorModifiers(ruleSetId, rule, prevRule)) {
            /**
             * Delegating a call of this method
             */
            override fun visit(
                node: ASTNode,
                autoCorrect: Boolean,
                emit: EmitType
            ) {
                rule.visit(node, autoCorrect, emit)
            }
        }

        private fun adjustVisitorModifiers(ruleSetId: String, rule: Rule, prevRule: Rule): Set<Rule.VisitorModifier> {
            val visitorModifiers: Set<Rule.VisitorModifier> = rule.visitorModifiers
            require(visitorModifiers.none { it is Rule.VisitorModifier.RunAfterRule }) {
                "Rule ${rule.id} already contains VisitorModifier.RunAfterRule"
            }
            require(rule.id != prevRule.id) {
                "PrevRule has same ID as rule: ${rule.id}"
            }
            return visitorModifiers + Rule.VisitorModifier.RunAfterRule(
                ruleId = ruleSetId + ":" + prevRule.id,
                loadOnlyWhenOtherRuleIsLoaded = false,
                runOnlyWhenOtherRuleIsEnabled = false
            )
        }

        fun Rule.delegatee(): Rule = if (this is OrderedRule) this.rule else this

        /**
         * @return RuleSet with ordered rules
         */
        fun RuleSet.ordered(): OrderedRuleSet = OrderedRuleSet(id = id, rules = rules)
    }
}
