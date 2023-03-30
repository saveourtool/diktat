package org.cqfn.diktat.util

import org.cqfn.diktat.ktlint.colorNamesForPlainReporter
import org.cqfn.diktat.ktlint.plainReporterProvider
import org.cqfn.diktat.ktlint.reporterProviders
import com.pinterest.ktlint.core.ReporterProvider
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default

/**
 * @return a single [ReporterProvider] as parsed cli arg
 */
internal fun ArgParser.reporterProviderId() = option(
    type = ArgType.Choice(
        choices = reporterProviders.keys.toList(),
        toVariant = { it },
        variantToString = { it },
    ),
    fullName = "reporter",
    shortName = "r",
    description = "The reporter to use"
)
    .default(plainReporterProvider.id)

/**
 * @return a single and optional color name as parsed cli args
 */
internal fun ArgParser.colorName() = this.option(
    type = ArgType.Choice(
        choices = colorNamesForPlainReporter,
        toVariant = { it },
        variantToString = { it },
    ),
    fullName = "plain-color",
    shortName = null,
    description = "Colorize the output.",
)
