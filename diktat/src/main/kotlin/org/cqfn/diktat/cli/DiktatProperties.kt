package org.cqfn.diktat.cli

import org.cqfn.diktat.api.DiktatLogLevel
import org.cqfn.diktat.api.DiktatMode
import org.cqfn.diktat.api.DiktatReporterType
import org.cqfn.diktat.common.config.rules.DIKTAT
import org.cqfn.diktat.common.config.rules.DIKTAT_ANALYSIS_CONF
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.multiple
import kotlinx.cli.vararg

/**
 * @property config path to `diktat-analysis.yml`
 * @property mode mode of `diktat`
 * @property reporter
 * @property output
 * @property logLevel
 */
data class DiktatProperties(
    val config: String,
    val mode: DiktatMode,
    val reporter: String,
    val output: String,
    val logLevel: DiktatLogLevel,
) {
    companion object {
        /**
         * @param args cli arguments
         * @return parsed [DiktatProperties]
         */
        fun parse(args: Array<String>): DiktatProperties {
            val parser = ArgParser(DIKTAT)
            val config: String by parser.option(
                type = ArgType.String,
                fullName = "config",
                shortName = "c",
                description = """
            Specify the location of the YAML configuration file.
            By default, $DIKTAT_ANALYSIS_CONF in the current
            directory is used.
        """.trimIndent(),
            ).default(DIKTAT_ANALYSIS_CONF)
            val mode: DiktatMode by parser.option(
                type = ArgType.Choice<DiktatMode>(),
                fullName = "mode",
                shortName = "m",
                description = "Mode of `diktat` controls that `diktat` fixes or only finds any deviations from the code style."
            ).default(DiktatMode.CHECK)
            val reporters: List<DiktatReporterType> by parser.option(
                type = ArgType.Choice<DiktatReporterType>(),
                fullName = "reporter",
                shortName = "r",
                description = "The reporter to use",
            )
                .default(DiktatReporterType.PLAIN)
                .multiple()
            val output: String? by parser.option(
                type = ArgType.String,
                fullName = "output",
                shortName = "o",
                description = "Redirect the reporter output to a file.",
            )
            val logLevel: DiktatLogLevel by parser.option(
                type = ArgType.Choice<DiktatLogLevel>(),
                fullName = "log-level",
                shortName = "l",
                description = "Enable the output with specific level",
            ).default(DiktatLogLevel.INFO)
            val patterns: List<String> by parser.argument(
                type = ArgType.String,
                description = ""
            )
                .vararg()

            parser.parse(args)

            return DiktatProperties(
                config = config,
                mode = mode,
                reporter = "N/A",
                output = "N/A",
                logLevel = logLevel,
            )
        }
    }
}

