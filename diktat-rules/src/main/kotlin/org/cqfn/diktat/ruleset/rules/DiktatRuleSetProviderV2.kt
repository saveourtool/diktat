package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.pinterest.ktlint.core.RuleProvider
import com.pinterest.ktlint.core.RuleSetProviderV2

/**
 * [RuleSetProviderV2] that provides diKTat ruleset.
 *
 * The no-argument constructor is used by the Java SPI interface; that's why
 * it's explicitly annotated with [JvmOverloads].
 */
@Suppress("serial")
class DiktatRuleSetProviderV2
@JvmOverloads
constructor(private val factory: DiktatRuleSetFactory = DiktatRuleSetFactory()) : RuleSetProviderV2(
    id = DIKTAT_RULE_SET_ID,
    about = About(
        maintainer = "Diktat",
        description = "Strict coding standard for Kotlin and a custom set of rules for detecting code smells, code style issues, and bugs",
        license = "https://github.com/saveourtool/diktat/blob/master/LICENSE",
        repositoryUrl = "https://github.com/saveourtool/diktat",
        issueTrackerUrl = "https://github.com/saveourtool/diktat/issues",
    ),
) {
    /**
     * @param diktatConfigFile the configuration file where all configurations for
     *   inspections and rules are stored.
     */
    constructor(diktatConfigFile: String) : this(DiktatRuleSetFactory(diktatConfigFile))

    override fun getRuleProviders(): Set<RuleProvider> =
        factory().ruleProviders
}
