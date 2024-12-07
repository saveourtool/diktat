package com.saveourtool.diktat.ktlint

import com.saveourtool.diktat.api.DiktatRule
import com.saveourtool.diktat.api.DiktatRuleSet
import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.Rule.VisitorModifier.RunAfterRule.Mode
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

private typealias EmitType = (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit

/**
 * This is a wrapper around __KtLint__'s [Rule] which adjusts visitorModifiers to keep order with prevRule.
 * @property rule
 */
class KtLintRuleWrapper(
    val rule: DiktatRule,
    prevRuleId: RuleId? = null,
) : Rule(
    ruleId = rule.id.toRuleId(DIKTAT_RULE_SET_ID),
    about = about,
    visitorModifiers = createVisitorModifiers(rule, prevRuleId),
) {
    @Deprecated("Marked for removal in Ktlint 2.0. Please implement interface RuleAutocorrectApproveHandler.")
    override fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitType,
    ) = rule.invoke(node, autoCorrect) { offset, errorMessage, canBeAutoCorrected ->
        emit.invoke(offset, errorMessage.correctErrorDetail(canBeAutoCorrected), canBeAutoCorrected)
    }

    companion object {
        private val about: About = About(
            maintainer = "Diktat",
            repositoryUrl = "https://github.com/saveourtool/diktat",
            issueTrackerUrl = "https://github.com/saveourtool/diktat/issues",
        )

        private fun Sequence<DiktatRule>.wrapRulesToProviders(): Sequence<RuleProvider> = runningFold(null as RuleProvider?) { prevRuleProvider, diktatRule ->
            val prevRuleId = prevRuleProvider?.ruleId?.value?.toRuleId(DIKTAT_RULE_SET_ID)
            RuleProvider(
                provider = { KtLintRuleWrapper(diktatRule, prevRuleId) },
            )
        }.filterNotNull()

        /**
         * @return [Set] of __KtLint__'s [RuleProvider]s created from [DiktatRuleSet]
         */
        fun DiktatRuleSet.toKtLint(): Set<RuleProvider> = rules
            .asSequence()
            .wrapRulesToProviders()
            .toSet()

        private fun createVisitorModifiers(
            rule: DiktatRule,
            prevRuleId: RuleId?,
        ): Set<VisitorModifier> = prevRuleId?.run {
            val ruleId = rule.id.toRuleId(DIKTAT_RULE_SET_ID)
            require(ruleId != prevRuleId) {
                "PrevRule has same ID as rule: $ruleId"
            }
            setOf(
                VisitorModifier.RunAfterRule(
                    ruleId = prevRuleId,
                    mode = Mode.REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED
                )
            )
        } ?: emptySet()

        /**
         * @return a rule to which a logic is delegated
         */
        internal fun Rule.unwrap(): DiktatRule = (this as? KtLintRuleWrapper)?.rule ?: error("Provided rule ${javaClass.simpleName} is not wrapped by diktat")
    }
}
