package org.cqfn.diktat.ktlint

import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.common.config.rules.qualifiedWithRuleSetId
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.rules.DiktatRuleSet
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * This is a wrapper around Ktlint RuleSet which adjusts visitorModifiers for all rules to keep order with prevRule
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
            return rules.runningFold(null as OrderedRule?) { prevRule, diktatRule ->
                    OrderedRule(diktatRule, prevRule)
                }
                .filterNotNull()
                .toTypedArray()
        }

        private fun createVisitorModifiers(
            rule: DiktatRule,
            prevRule: OrderedRule?,
        ): Set<Rule.VisitorModifier> = prevRule?.id?.qualifiedWithRuleSetId(DIKTAT_RULE_SET_ID)
            ?.let { previousRuleId ->
                val ruleId = rule.id.qualifiedWithRuleSetId(DIKTAT_RULE_SET_ID)
                require(ruleId != previousRuleId) {
                    "PrevRule has same ID as rule: $ruleId"
                }
                setOf(
                    Rule.VisitorModifier.RunAfterRule(
                        ruleId = previousRuleId,
                        loadOnlyWhenOtherRuleIsLoaded = false,
                        runOnlyWhenOtherRuleIsEnabled = false
                    )
                )
            } ?: emptySet()

        /**
         * @return a rule to which a logic is delegated
         */
        internal fun Rule.delegatee(): DiktatRule =(this as? OrderedRule)?.rule ?: error("Provided rule ${javaClass.simpleName} is not wrapped by diktat")

        /**
         * @property rule wraps this rule to keep order
         */
        private class OrderedRule(
            val rule: DiktatRule,
            prevRule: OrderedRule? = null,
        ) : Rule(
            id = rule.id.qualifiedWithRuleSetId(DIKTAT_RULE_SET_ID),
            visitorModifiers = createVisitorModifiers(rule, prevRule),
        ) {
            @Deprecated(
                "Marked for deletion in ktlint 0.48.0",
                replaceWith = ReplaceWith("beforeVisitChildNodes(node, autoCorrect, emit)"),
            )
            override fun visit(
                node: ASTNode,
                autoCorrect: Boolean,
                emit: EmitType,
            ) = rule.visit(node, autoCorrect, emit)
        }
    }
}
