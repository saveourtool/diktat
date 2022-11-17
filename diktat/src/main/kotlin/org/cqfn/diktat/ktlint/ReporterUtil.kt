package org.cqfn.diktat.ktlint

import com.pinterest.ktlint.reporter.checkstyle.CheckStyleReporterProvider
import com.pinterest.ktlint.reporter.html.HtmlReporterProvider
import com.pinterest.ktlint.reporter.json.JsonReporterProvider
import com.pinterest.ktlint.reporter.plain.PlainReporterProvider
import com.pinterest.ktlint.reporter.sarif.SarifReporterProvider
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType

private val reporters = setOf(
    PlainReporterProvider(),
    JsonReporterProvider(),
    SarifReporterProvider(),
    CheckStyleReporterProvider(),
    HtmlReporterProvider(),
)
    .associateBy { it.id }

internal fun ArgParser.createReporterOption() {
    return option(
        ArgType.Choice<String> {

        }
        ArgType.Choice {  }
    )
}
