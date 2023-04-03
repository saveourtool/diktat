package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import org.cqfn.diktat.ktlint.KtLintRuleSetWrapper.Companion.toKtLint

/**
 * [RuleSetProvider] that provides diKTat ruleset.
 *
 * By default, it is expected to have `diktat-analysis.yml` configuration in the root folder where 'ktlint' is run
 * otherwise it will use default configuration where some rules are disabled.
 *
 * This class is registered in [resources/META-INF/services/com.pinterest.ktlint.core.RuleSetProvider]
 */
class DiktatRuleSetProviderSpi : RuleSetProvider {
    override fun get(): RuleSet = DiktatRuleSetProvider().invoke().toKtLint()
}
