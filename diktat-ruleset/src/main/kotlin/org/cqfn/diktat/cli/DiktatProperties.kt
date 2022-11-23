package org.cqfn.diktat.cli

import org.cqfn.diktat.api.DiktatMode
import org.cqfn.diktat.common.config.rules.DIKTAT
import org.cqfn.diktat.common.config.rules.DIKTAT_ANALYSIS_CONF
import org.cqfn.diktat.ktlint.buildReporter
import org.cqfn.diktat.ktlint.colorName
import org.cqfn.diktat.ktlint.reporterProviderId
import com.pinterest.ktlint.core.Reporter
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.slf4j.event.Level
import kotlin.system.exitProcess
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import kotlinx.cli.optional
import kotlinx.cli.vararg

/**
 * @property config path to `diktat-analysis.yml`
 * @property mode mode of `diktat`
 * @property reporterProviderId
 * @property output
 * @property patterns
 */
data class DiktatProperties(
    val config: String,
    val mode: DiktatMode,
    val reporterProviderId: String,
    val output: String?,
    private val groupByFileInPlain: Boolean,
    private val colorNameInPlain: String?,
    private val logLevel: Level,
    val patterns: List<String>,
) {
    /**
     * @return a configured [Reporter]
     */
    fun reporter(): Reporter = buildReporter(
        reporterProviderId, output, colorNameInPlain, groupByFileInPlain, mode
    )

    /**
     * Configure logger level using [logLevel]
     */
    fun configureLogger() {
        // set log level
        LogManager.getContext(false)
            .let { it as LoggerContext }
            .also { ctx ->
                ctx.configuration.rootLogger.level = when (logLevel) {
                    Level.ERROR -> org.apache.logging.log4j.Level.ERROR
                    Level.WARN -> org.apache.logging.log4j.Level.WARN
                    Level.INFO -> org.apache.logging.log4j.Level.INFO
                    Level.DEBUG -> org.apache.logging.log4j.Level.DEBUG
                    Level.TRACE -> org.apache.logging.log4j.Level.TRACE
                }
            }
            .updateLoggers()
    }

    companion object {
        /**
         * @param args cli arguments
         * @return parsed [DiktatProperties]
         */
        @OptIn(ExperimentalCli::class)
        @Suppress(
            "LongMethod",
            "TOO_LONG_FUNCTION"
        )
        fun parse(args: Array<String>): DiktatProperties {
            val parser = ArgParser(DIKTAT)
            val config: String by parser.option(
                type = ArgType.String,
                fullName = "config",
                shortName = "c",
                description = "Specify the location of the YAML configuration file. By default, $DIKTAT_ANALYSIS_CONF in the current directory is used.",
            ).default(DIKTAT_ANALYSIS_CONF)
            val mode: DiktatMode by parser.option(
                type = ArgType.Choice<DiktatMode>(),
                fullName = "mode",
                shortName = "m",
                description = "Mode of `diktat` controls that `diktat` fixes or only finds any deviations from the code style."
            ).default(DiktatMode.CHECK)
            val reporterProviderId: String by parser.reporterProviderId()
            val output: String? by parser.option(
                type = ArgType.String,
                fullName = "output",
                shortName = "o",
                description = "Redirect the reporter output to a file.",
            )
            val groupByFileInPlain: Boolean by parser.option(
                type = ArgType.Boolean,
                fullName = "plain-group-by-file",
                shortName = null,
                description = "A flag for plain reporter"
            ).default(false)
            val colorName: String? by parser.colorName()
            val logLevel: Level by parser.option(
                type = ArgType.Choice<Level>(),
                fullName = "log-level",
                shortName = "l",
                description = "Enable the output with specific level",
            ).default(Level.INFO)
            val patterns: List<String> by parser.argument(
                type = ArgType.String,
                description = "A list of files to process by diktat"
            )
                .vararg()

            parser.subcommands(
                object : Subcommand("version", "Output version information and exit.") {
                    override fun execute() {
                        println(readFromResource("META-INF/diktat/version"))
                        exitProcess(0)
                    }
                },
                object : Subcommand("license", "Display the license and exit.") {
                    override fun execute() {
                        println(readFromResource("META-INF/diktat/LICENSE"))
                        exitProcess(0)
                    }
                },
            )
//            val showVersion: Boolean? by parser.option(
//                type = ArgType.Boolean,
//                fullName = "version",
//                shortName = "V",
//                description = "Output version information and exit."
//            )
//            val showLicense: Boolean? by parser.option(
//                type = ArgType.Boolean,
//                fullName = "license",
//                shortName = null,
//                description = "Display the license and exit."
//            )

            parser.parse(args)
            return DiktatProperties(
                config = config,
                mode = mode,
                reporterProviderId = reporterProviderId,
                output = output,
                groupByFileInPlain = groupByFileInPlain,
                colorNameInPlain = colorName,
                logLevel = logLevel,
                patterns = patterns,
            )
        }

        private fun readFromResource(resourceName: String): String = DiktatProperties::class.java
            .classLoader
            .getResource(resourceName)
            ?.readText()
            ?: error("Resource $resourceName not found")
    }
}
