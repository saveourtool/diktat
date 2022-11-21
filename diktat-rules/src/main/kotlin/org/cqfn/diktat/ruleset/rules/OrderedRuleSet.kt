package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.qualifiedWithRuleSetId
import org.cqfn.diktat.ruleset.constants.EmitType
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleProvider
import com.pinterest.ktlint.core.api.EditorConfigProperties
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * This is a wrapper around Ktlint RuleSet which adjusts visitorModifiers for all rules to keep order with prevRule
 * Added as a workaround after introducing a new logic for sorting KtLint Rules: https://github.com/pinterest/ktlint/issues/1478
 *
 * @param rules the rules which belong to the current [DiktatRuleSet].
 */
class OrderedRuleSet private constructor(
    private val rules: List<Rule>
) : DiktatRuleSet {
    @Suppress("CUSTOM_GETTERS_SETTERS")
    override val ruleProviders: Set<RuleProvider>
        get() =
            rules.map { rule ->
                RuleProvider {
                    rule
                }
            }.toSet()

    /**
     * @param id the ID of the [DiktatRuleSet].
     * @param rules the rules which belong to the current [DiktatRuleSet].
     */
    constructor(
        id: String,
        rules: List<Rule>
    ) : this(id, rules = rules.toTypedArray())

    /**
     * @param id the ID of the [DiktatRuleSet].
     * @param rules the rules which belong to the current [DiktatRuleSet].
     */
    constructor(
        id: String,
        vararg rules: Rule
    ) : this(adjustRules(id, rules))

    companion object {
        private fun adjustRules(ruleSetId: String, rules: Array<out Rule>): List<Rule> {
            if (rules.isEmpty()) {
                return emptyList()
            }
            return rules.mapIndexed { index, rule ->
                if (index == 0) {
                    checkVisitorModifiers(rule)
                    rule
                } else {
                    OrderedRule(ruleSetId, rule, rules[index - 1])
                }
            }.toList()
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
         * @property rule wraps this rule to keep order
         */
        private class OrderedRule(
            ruleSetId: String,
            val rule: Rule,
            prevRule: Rule
        ) : Rule(rule.id.qualifiedWithRuleSetId(ruleSetId), adjustVisitorModifiers(ruleSetId, rule, prevRule)) {
            override fun beforeFirstNode(editorConfigProperties: EditorConfigProperties) =
                rule.beforeFirstNode(editorConfigProperties)

            override fun beforeVisitChildNodes(
                node: ASTNode,
                autoCorrect: Boolean,
                emit: EmitType,
            ) =
                rule.beforeVisitChildNodes(node, autoCorrect, emit)

            override fun afterVisitChildNodes(
                node: ASTNode,
                autoCorrect: Boolean,
                emit: EmitType,
            ) =
                rule.afterVisitChildNodes(node, autoCorrect, emit)

            override fun afterLastNode() =
                rule.afterLastNode()
        }
    }
}
