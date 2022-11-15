@file:Suppress(
    "Deprecation"
)

package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.qualifiedWithRuleSetId
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
class OrderedRuleSet(id: String, vararg rules: Rule) : RuleSet(id, rules = adjustRules(id, rules = rules)) {
    companion object {
        private fun adjustRules(ruleSetId: String, vararg rules: Rule): Array<out Rule> {
            if (rules.isEmpty()) {
                return emptyArray()
            }
            return rules.mapIndexed { index, rule ->
                if (index == 0) {
                    checkVisitorModifiers(rule)
                    rule
                } else {
                    OrderedRule(ruleSetId, rule, rules[index - 1])
                }
            }.toTypedArray()
        }

        private fun adjustVisitorModifiers(
            ruleSetId: String,
            rule: Rule,
            prevRule: Rule
        ): Set<Rule.VisitorModifier> {
            val visitorModifiers: Set<Rule.VisitorModifier> = rule.visitorModifiers
            checkVisitorModifiers(rule)
            val ruleId = rule.id.qualifiedWithRuleSetId(ruleSetId)
            val previousRuleId = prevRule.id.qualifiedWithRuleSetId(ruleSetId)
            require(ruleId != previousRuleId) {
                "PrevRule has same ID as rule: $ruleId"
            }
            return visitorModifiers + Rule.VisitorModifier.RunAfterRule(
                ruleId = previousRuleId,
                loadOnlyWhenOtherRuleIsLoaded = false,
                runOnlyWhenOtherRuleIsEnabled = false
            )
        }

        private fun checkVisitorModifiers(rule: Rule) {
            require(rule.visitorModifiers.none { it is Rule.VisitorModifier.RunAfterRule }) {
                "Rule ${rule.id} contains VisitorModifier.RunAfterRule"
            }
        }

        /**
         * @return a rule to which a logic is delegated
         */
        internal fun Rule.delegatee(): Rule = if (this is OrderedRule) this.rule else this

        /**
         * @return RuleSet with ordered rules
         */
        fun RuleSet.ordered(): RuleSet =
            when (this) {
                is OrderedRuleSet -> this
                else -> OrderedRuleSet(id = id, rules = rules)
            }

        /**
         * @property rule wraps this rule to keep order
         */
        private class OrderedRule(
            ruleSetId: String,
            val rule: Rule,
            prevRule: Rule
        ) : Rule(rule.id.qualifiedWithRuleSetId(ruleSetId), adjustVisitorModifiers(ruleSetId, rule, prevRule)) {
            @Deprecated(
                "Marked for deletion in ktlint 0.48.0",
                replaceWith = ReplaceWith("beforeVisitChildNodes(node, autoCorrect, emit)"),
            )
            override fun visit(
                node: ASTNode,
                autoCorrect: Boolean,
                emit: EmitType,
            ) =
                rule.visit(node, autoCorrect, emit)
        }
    }
}
