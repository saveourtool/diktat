package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ktlint.KtLintRuleWrapper.Companion.toKtLint
import com.pinterest.ktlint.cli.ruleset.core.api.RuleSetProviderV3
import com.pinterest.ktlint.logger.api.initKtLintKLogger
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.pinterest.ktlint.rule.engine.core.api.RuleSetId
import mu.KotlinLogging
import org.slf4j.Logger

/**
 * [RuleSetProviderV3] that provides diKTat ruleset.
 *
 * By default, it is expected to have `diktat-analysis.yml` configuration in the root folder where 'ktlint' is run
 * otherwise it will use default configuration where some rules are disabled.
 *
 * This class is registered in [resources/META-INF/services/com.pinterest.ktlint.cli.ruleset.core.api.RuleSetProviderV3]
 *
 * The no-argument constructor is used by the Java SPI interface.
 */
class DiktatRuleSetProviderV3Spi : RuleSetProviderV3(
    id = RuleSetId(DIKTAT_RULE_SET_ID),
) {
    private val diktatRuleConfigReader = DiktatRuleConfigReaderImpl()
    private val diktatRuleSetFactory = DiktatRuleSetFactoryImpl()

    init {
        // Need to init KtLint logger to set log level from CLI
        KotlinLogging.logger(Logger.ROOT_LOGGER_NAME).initKtLintKLogger()
    }

    override fun getRuleProviders(): Set<RuleProvider> = diktatRuleSetFactory(diktatRuleConfigReader(DiktatRuleConfigReaderImpl.readConfigFile()))
        .toKtLint()
}
