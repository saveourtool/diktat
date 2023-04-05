package org.cqfn.diktat.ktlint

import org.cqfn.diktat.api.DiktatRule
import org.cqfn.diktat.api.DiktatRuleSet
import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet

/**
 * This is a wrapper around __KtLint__'s [RuleSet] which adjusts visitorModifiers for all rules to keep order with prevRule
 * Added as a workaround after introducing a new logic for sorting KtLint Rules: https://github.com/pinterest/ktlint/issues/1478
 *
 * @param diktatRuleSet the rules which belong to the current [DiktatRuleSet].
 */
class KtLintRuleSetWrapper private constructor(
    diktatRuleSet: DiktatRuleSet,
) : RuleSet(DIKTAT_RULE_SET_ID, rules = wrapRules(diktatRuleSet.rules)) {
    companion object {
        /**
         * @return __KtLint__'s [RuleSet] created from [DiktatRuleSet]
         */
        fun DiktatRuleSet.toKtLint(): RuleSet = KtLintRuleSetWrapper(this)

        private fun wrapRules(rules: List<DiktatRule>): Array<Rule> {
            if (rules.isEmpty()) {
                return emptyArray()
            }
            return rules.asSequence()
                .runningFold(null as KtLintRuleWrapper?) { prevRule, diktatRule ->
                    KtLintRuleWrapper(diktatRule, prevRule)
                }
                .filterNotNull()
                .toList()
                .toTypedArray()
        }
    }
}
