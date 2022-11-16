package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatLogLevel
import org.cqfn.diktat.api.DiktatReporterType
import org.cqfn.diktat.common.config.rules.DIKTAT_ANALYSIS_CONF
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default

fun main(args: Array<String>) {
    val parser = ArgParser("diktat")
    val configOption = parser.option(
        type = ArgType.String,
        fullName = "config",
        shortName = "c",
        description = """
            Specify the location of the YAML configuration file.
            By default, $DIKTAT_ANALYSIS_CONF in the current
            directory is used.
        """.trimIndent(),
    ).default(DIKTAT_ANALYSIS_CONF)
    val config: String by configOption

    val formatOption = parser.option(
        type = ArgType.Boolean,
        fullName = "format",
        shortName = "F",
        description = "Fix any deviations from the code style."
    ).default(false)
    val format: Boolean by formatOption

    val reporterOption = parser.option(
        type = ArgType.Choice<DiktatReporterType>(),
        fullName = "reporter",
        shortName = "r",
        description = "The reporter to use",
    ).default(DiktatReporterType.PLAIN)
    val reporter: DiktatReporterType by reporterOption

    val outputOption = parser.option(
        type = ArgType.String,
        fullName = "output",
        shortName = "o",
        description = "Redirect the reporter output to a file.",
    )
    val output: String? by outputOption

    val logLevelOption = parser.option(
        type = ArgType.Choice<DiktatLogLevel>(),
        fullName = "log-level",
        shortName = "l",
        description = "Enable the output of specified level",
    ).default(DiktatLogLevel.INFO)
    val logLevel: DiktatLogLevel by logLevelOption

    parser.parse(args)

}
