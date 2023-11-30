package com.saveourtool.diktat.ruleset.rules

import com.saveourtool.diktat.DIKTAT_ANALYSIS_CONF
import com.saveourtool.diktat.common.config.rules.DIKTAT_CONF_PROPERTY
import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ktlint.KtLintRuleWrapper.Companion.toKtLint
import com.saveourtool.diktat.ruleset.config.DiktatRuleConfigYamlReader
import com.pinterest.ktlint.cli.ruleset.core.api.RuleSetProviderV3
import com.pinterest.ktlint.logger.api.initKtLintKLogger
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import com.pinterest.ktlint.rule.engine.core.api.RuleSetId
import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.Logger
import java.io.File
import java.io.InputStream

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
    private val diktatRuleConfigReader = DiktatRuleConfigYamlReader()
    private val diktatRuleSetFactory = DiktatRuleSetFactoryImpl()

    init {
        // Need to init KtLint logger to set log level from CLI
        KotlinLogging.logger(Logger.ROOT_LOGGER_NAME).initKtLintKLogger()
    }

    override fun getRuleProviders(): Set<RuleProvider> = diktatRuleSetFactory(diktatRuleConfigReader(readConfigFile()))
        .toKtLint()

    private fun readConfigFile(): InputStream {
        val resourceFileName = resolveConfigFile()
        val resourceFile = File(resourceFileName)
        return if (resourceFile.exists()) {
            log.debug { "Using $DIKTAT_ANALYSIS_CONF file from the following path: ${resourceFile.absolutePath}" }
            resourceFile.inputStream()
        } else {
            log.debug { "Using the default $DIKTAT_ANALYSIS_CONF file from the class path" }
            javaClass.classLoader.getResourceAsStream(resourceFileName) ?: run {
                log.error { "Not able to open file $resourceFileName from the resources" }
                object : InputStream() {
                    override fun read(): Int = -1
                }
            }
        }
    }

    private fun resolveConfigFile(): String {
        val possibleConfigs: Sequence<String?> = sequence {
            yield(DIKTAT_ANALYSIS_CONF)
            yield(resolveConfigFileFromJarLocation())
            yield(resolveConfigFileFromSystemProperty())
        }

        log.debug {
            "Will run $DIKTAT_RULE_SET_ID with $DIKTAT_ANALYSIS_CONF" +
                    " (it can be placed to the run directory or the default file from resources will be used)"
        }
        val configPath = possibleConfigs
            .firstOrNull { it != null && File(it).exists() }
        return configPath
            ?: run {
                val possibleConfigsList = possibleConfigs.toList()
                log.warn {
                    "Configuration file not found in directory where diktat is run (${possibleConfigsList[0]}) " +
                            "or in the directory where diktat.jar is stored (${possibleConfigsList[1]}) " +
                            "or in system property <diktat.config.path> (${possibleConfigsList[2]}), " +
                            "the default file included in jar will be used. " +
                            "Some configuration options will be disabled or substituted with defaults. " +
                            "Custom configuration file should be placed in diktat working directory if run from CLI " +
                            "or provided as configuration options in plugins."
                }
                DIKTAT_ANALYSIS_CONF
            }
    }

    private fun resolveConfigFileFromJarLocation(): String {
        // for some aggregators of static analyzers we need to provide configuration for cli
        // in this case diktat would take the configuration from the directory where jar file is stored
        val ruleSetProviderPath = javaClass
            .protectionDomain
            .codeSource
            .location
            .toURI()

        val configPathWithFileName = File(ruleSetProviderPath).absolutePath

        val indexOfName = configPathWithFileName.lastIndexOf(File.separator)
        val configPath = if (indexOfName > -1) configPathWithFileName.substring(0, indexOfName) else configPathWithFileName

        return "$configPath${File.separator}$DIKTAT_ANALYSIS_CONF"
    }

    private fun resolveConfigFileFromSystemProperty(): String? = System.getProperty(DIKTAT_CONF_PROPERTY)

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
