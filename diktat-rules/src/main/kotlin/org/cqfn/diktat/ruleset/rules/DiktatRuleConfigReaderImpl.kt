package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.api.DiktatRuleConfig
import org.cqfn.diktat.api.DiktatRuleConfigReader
import org.cqfn.diktat.common.config.rules.DIKTAT_ANALYSIS_CONF
import org.cqfn.diktat.common.config.rules.DIKTAT_COMMON
import org.cqfn.diktat.common.config.rules.DIKTAT_CONF_PROPERTY
import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.ruleset.constants.Warnings
import mu.KotlinLogging
import org.jetbrains.kotlin.org.jline.utils.Levenshtein
import java.io.File
import java.io.InputStream

/**
 * Default implementation for [DiktatRuleConfigReader]
 */
class DiktatRuleConfigReaderImpl : DiktatRuleConfigReader {
    override fun invoke(inputStream: InputStream): List<DiktatRuleConfig> {
        return RulesConfigReader(javaClass.classLoader)
            .read(inputStream)
            ?.onEach(::validate)
            ?: emptyList()
    }

    private fun validate(config: org.cqfn.diktat.common.config.rules.RulesConfig) =
        require(config.name == DIKTAT_COMMON || config.name in Warnings.names) {
            val closestMatch = Warnings.names.minByOrNull { Levenshtein.distance(it, config.name) }
            "Warning name <${config.name}> in configuration file is invalid, did you mean <$closestMatch>?"
        }

    companion object {
        private val log = KotlinLogging.logger {}

        /**
         * @param diktatConfigFile the configuration file where all configurations for
         *   inspections and rules are stored.
         * @return resolved existed diktatConfigFile
         */
        fun resolveConfigFile(diktatConfigFile: String = DIKTAT_ANALYSIS_CONF): String {
            val possibleConfigs: Sequence<String?> = sequence {
                yield(resolveDefaultConfig(diktatConfigFile))
                yield(resolveConfigFileFromJarLocation(diktatConfigFile))
                yield(resolveConfigFileFromSystemProperty())
            }

            log.debug("Will run $DIKTAT_RULE_SET_ID with $diktatConfigFile" +
                    " (it can be placed to the run directory or the default file from resources will be used)")
            val configPath = possibleConfigs
                .firstOrNull { it != null && File(it).exists() }
            return configPath
                ?: run {
                    val possibleConfigsList = possibleConfigs.toList()
                    log.warn(
                        "Configuration file not found in directory where diktat is run (${possibleConfigsList[0]}) " +
                                "or in the directory where diktat.jar is stored (${possibleConfigsList[1]}) " +
                                "or in system property <diktat.config.path> (${possibleConfigsList[2]}), " +
                                "the default file included in jar will be used. " +
                                "Some configuration options will be disabled or substituted with defaults. " +
                                "Custom configuration file should be placed in diktat working directory if run from CLI " +
                                "or provided as configuration options in plugins."
                    )
                    diktatConfigFile
                }
        }

        private fun resolveDefaultConfig(diktatConfigFile: String) = diktatConfigFile

        private fun resolveConfigFileFromJarLocation(diktatConfigFile: String): String {
            // for some aggregators of static analyzers we need to provide configuration for cli
            // in this case diktat would take the configuration from the directory where jar file is stored
            val ruleSetProviderPath =
                javaClass
                    .protectionDomain
                    .codeSource
                    .location
                    .toURI()

            val configPathWithFileName = File(ruleSetProviderPath).absolutePath

            val indexOfName = configPathWithFileName.lastIndexOf(File.separator)
            val configPath = if (indexOfName > -1) configPathWithFileName.substring(0, indexOfName) else configPathWithFileName

            return "$configPath${File.separator}$diktatConfigFile"
        }

        private fun resolveConfigFileFromSystemProperty(): String? = System.getProperty(DIKTAT_CONF_PROPERTY)
    }
}
