package org.cqfn.diktat.ktlint

import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ktlint.KtLintRuleWrapper.Companion.asProvider
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.rules.DiktatRuleSet
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleProvider
import com.pinterest.ktlint.core.RuleSetProviderV2

/**
 * This is a wrapper around __KtLint__'s [RuleSetProviderV2].
 */
class KtLintRuleSetProviderV2Wrapper private constructor(
    private val diktatRuleSetFactory: DiktatRuleSetProvider,
) : RuleSetProviderV2(
    id = DIKTAT_RULE_SET_ID,
    about = about,
) {
    override fun getRuleProviders(): Set<RuleProvider> = diktatRuleSetFactory().toKtLint()

    companion object {
        private fun Sequence<DiktatRule>.wrapRules(): Sequence<Rule> = runningFold(null as KtLintRuleWrapper?) { prevRule, diktatRule ->
            KtLintRuleWrapper(diktatRule, prevRule)
        }.filterNotNull()

        /**
         * About for diktat ruleset
         */
        val about: About = About(
            maintainer = "Diktat",
            description = "Strict coding standard for Kotlin and a custom set of rules for detecting code smells, code style issues, and bugs",
            license = "https://github.com/saveourtool/diktat/blob/master/LICENSE",
            repositoryUrl = "https://github.com/saveourtool/diktat",
            issueTrackerUrl = "https://github.com/saveourtool/diktat/issues",
        )

        /**
         * @return __KtLint__'s [RuleSetProviderV2] created from [DiktatRuleSetProvider]
         */
        fun DiktatRuleSetProvider.toKtLint(): RuleSetProviderV2 = KtLintRuleSetProviderV2Wrapper(this)

        /**
         * @return a set of __KtLint__'s [RuleProvider] created from [DiktatRuleSet]
         */
        fun DiktatRuleSet.toKtLint(): Set<RuleProvider> = rules.asSequence()
            .wrapRules()
            .map { it.asProvider() }
            .toSet()
    }

}
