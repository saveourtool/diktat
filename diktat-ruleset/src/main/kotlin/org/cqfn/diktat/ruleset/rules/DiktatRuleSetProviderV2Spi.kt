package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ktlint.KtLintRuleWrapper.Companion.toKtLint
import com.pinterest.ktlint.core.RuleProvider
import com.pinterest.ktlint.core.RuleSetProviderV2
import com.pinterest.ktlint.core.initKtLintKLogger
import mu.KotlinLogging
import org.slf4j.Logger

/**
 * [RuleSetProviderV2] that provides diKTat ruleset.
 *
 * By default, it is expected to have `diktat-analysis.yml` configuration in the root folder where 'ktlint' is run
 * otherwise it will use default configuration where some rules are disabled.
 *
 * This class is registered in [resources/META-INF/services/com.pinterest.ktlint.core.RuleSetProviderV2]
 *
 * The no-argument constructor is used by the Java SPI interface.
 */
class DiktatRuleSetProviderV2Spi : RuleSetProviderV2(
    id = DIKTAT_RULE_SET_ID,
    about = About(
        maintainer = "Diktat",
        description = "Strict coding standard for Kotlin and a custom set of rules for detecting code smells, code style issues, and bugs",
        license = "https://github.com/saveourtool/diktat/blob/master/LICENSE",
        repositoryUrl = "https://github.com/saveourtool/diktat",
        issueTrackerUrl = "https://github.com/saveourtool/diktat/issues",
    ),
) {
    init {
        // Need to init KtLint logger to set log level from CLI
        KotlinLogging.logger(Logger.ROOT_LOGGER_NAME).initKtLintKLogger()
    }

    override fun getRuleProviders(): Set<RuleProvider> = DiktatRuleSetProvider().invoke().toKtLint()
}
