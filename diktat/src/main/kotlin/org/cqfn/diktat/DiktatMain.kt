package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatLogLevel
import org.cqfn.diktat.api.DiktatReporterType
import org.cqfn.diktat.common.config.rules.DIKTAT
import org.cqfn.diktat.common.config.rules.DIKTAT_ANALYSIS_CONF
import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default

fun main(args: Array<String>) {
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
    val format: Boolean by parser.option(
        type = ArgType.Boolean,
        fullName = "format",
        shortName = "F",
        description = "Fix any deviations from the code style."
    ).default(false)
    val reporter: DiktatReporterType by parser.option(
        type = ArgType.Choice<DiktatReporterType>(),
        fullName = "reporter",
        shortName = "r",
        description = "The reporter to use",
    ).default(DiktatReporterType.PLAIN)
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

    parser.parse(args)

    val diktatRuleSetProvider = DiktatRuleSetProvider(config)
}
