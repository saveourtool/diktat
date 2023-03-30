package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.ktlint.KtLintRuleSetProviderV2Wrapper.Companion.toKtLint
import com.pinterest.ktlint.core.RuleProvider
import com.pinterest.ktlint.core.RuleSetProviderV2

/**
 * [RuleSetProviderV2] that provides diKTat ruleset.
 *
 * By default, it is expected to have `diktat-analysis.yml` configuration in the root folder where 'ktlint' is run
 * otherwise it will use default configuration where some rules are disabled.
 *
 * This class is registered in [resources/META-INF/services/com.pinterest.ktlint.core.RuleSetProviderV2]
 */
class DiktatRuleSetProviderV2Spi private constructor(
    private val ruleSetProviderV2: RuleSetProviderV2
) : RuleSetProviderV2(
    id = ruleSetProviderV2.id,
    about = ruleSetProviderV2.about,
) {
    /**
     * The no-argument constructor is used by the Java SPI interface.
     */
    constructor() : this(DiktatRuleSetProvider().toKtLint())

    override fun getRuleProviders(): Set<RuleProvider> = ruleSetProviderV2.getRuleProviders()
}
