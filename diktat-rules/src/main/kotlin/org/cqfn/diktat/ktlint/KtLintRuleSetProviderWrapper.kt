package org.cqfn.diktat.ktlint

import org.cqfn.diktat.ktlint.KtLintRuleSetWrapper.Companion.toKtLint
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider

/**
 * This is a wrapper around __KtLint__'s [RuleSetProvider].
 */
class KtLintRuleSetProviderWrapper private constructor(
    private val diktatRuleSetFactory: DiktatRuleSetProvider,
) : RuleSetProvider {
    override fun get(): RuleSet = diktatRuleSetFactory().toKtLint()

    companion object {
        /**
         * @return __KtLint__'s [RuleSetProvider] created from [DiktatRuleSetProvider]
         */
        fun DiktatRuleSetProvider.toKtLint(): RuleSetProvider = KtLintRuleSetProviderWrapper(this)
    }
}
