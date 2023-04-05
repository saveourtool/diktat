package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.ktlint.KtLintRuleSetWrapper.Companion.toKtLint
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import com.pinterest.ktlint.core.initKtLintKLogger
import mu.KotlinLogging
import org.slf4j.Logger

/**
 * [RuleSetProvider] that provides diKTat ruleset.
 *
 * By default, it is expected to have `diktat-analysis.yml` configuration in the root folder where 'ktlint' is run
 * otherwise it will use default configuration where some rules are disabled.
 *
 * This class is registered in [resources/META-INF/services/com.pinterest.ktlint.core.RuleSetProvider]
 */
class DiktatRuleSetProviderSpi : RuleSetProvider {
    init {
        // Need to init KtLint logger to set log level from CLI
        KotlinLogging.logger(Logger.ROOT_LOGGER_NAME).initKtLintKLogger()
    }

    override fun get(): RuleSet = DiktatRuleSetProvider().invoke().toKtLint()
}
