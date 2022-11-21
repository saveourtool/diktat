package org.cqfn.diktat.ktlint

import com.pinterest.ktlint.core.ReporterProvider
import com.pinterest.ktlint.reporter.checkstyle.CheckStyleReporterProvider
import com.pinterest.ktlint.reporter.html.HtmlReporterProvider
import com.pinterest.ktlint.reporter.json.JsonReporterProvider
import com.pinterest.ktlint.reporter.plain.PlainReporterProvider
import com.pinterest.ktlint.reporter.sarif.SarifReporterProvider
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default

private val plainReporterProvider = PlainReporterProvider()

private val reporterProviders = setOf(
    plainReporterProvider,
    JsonReporterProvider(),
    SarifReporterProvider(),
    CheckStyleReporterProvider(),
    HtmlReporterProvider(),
)
    .associateBy { it.id }

/**
 * @return a single [ReporterProvider] as parsed cli arg
 */
internal fun ArgParser.reporterProvider(): ReporterProvider<*> {
    val reporterProviders: ReporterProvider<*> by this.option(
        type = ArgType.Choice(
            choices = reporterProviders.values.toList(),
            toVariant = reporterProviders::getValue,
            variantToString = ReporterProvider<*>::id,
        ),
        fullName = "reporter",
        shortName = "r",
        description = "The reporter to use"
    )
        .default(plainReporterProvider)
    return reporterProviders
}

/**
 * @return true if receiver is [PlainReporterProvider]
 */
internal fun ReporterProvider<*>.isPlain(): Boolean {
    return id == plainReporterProvider.id
}
