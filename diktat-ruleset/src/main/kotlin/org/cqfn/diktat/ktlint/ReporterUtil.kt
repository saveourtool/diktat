package org.cqfn.diktat.ktlint

import org.cqfn.diktat.api.DiktatMode
import com.pinterest.ktlint.core.Reporter
import com.pinterest.ktlint.core.ReporterProvider
import com.pinterest.ktlint.reporter.checkstyle.CheckStyleReporterProvider
import com.pinterest.ktlint.reporter.html.HtmlReporterProvider
import com.pinterest.ktlint.reporter.json.JsonReporterProvider
import com.pinterest.ktlint.reporter.plain.PlainReporterProvider
import com.pinterest.ktlint.reporter.sarif.SarifReporterProvider
import java.io.PrintStream
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream
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

private const val defaultColorName = "DARK_GRAY"

// supported color names in KtLint
private val colorNames = listOf(
    "BLACK",
    "RED",
    "GREEN",
    "YELLOW",
    "BLUE",
    "MAGENTA",
    "CYAN",
    "LIGHT_GRAY",
    defaultColorName,
    "LIGHT_RED",
    "LIGHT_GREEN",
    "LIGHT_YELLOW",
    "LIGHT_BLUE",
    "LIGHT_MAGENTA",
    "LIGHT_CYAN",
    "WHITE",
)

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
        choices = colorNames,
        toVariant = { it },
        variantToString = { it },
    ),
    fullName = "plain-color",
    shortName = null,
    description = "Colorize the output.",
)

/**
 * @return true if receiver is [PlainReporterProvider]
 */
internal fun ReporterProvider<*>.isPlain(): Boolean {
    return id == plainReporterProvider.id
}

/**
 * @return a configured [Reporter]
 */
internal fun buildReporter(
    reporterProviderId: String,
    output: String?,
    colorNameInPlain: String?,
    groupByFileInPlain: Boolean,
    mode: DiktatMode,
): Reporter {
    val reporterProvider = reporterProviders.getValue(reporterProviderId)
    return reporterProvider.get(
        out = output
            ?.let { Paths.get(it) }
            ?.also { it.parent.createDirectories() }
            ?.outputStream()
            ?.let { PrintStream(it) }
            ?: System.out,
        opt = buildMap<String, Any> {
            colorNameInPlain?.let {
                require(reporterProvider.isPlain()) {
                    "colorization is applicable only for plain reporter"
                }
                put("color", true)
                put("color_name", it)
            } ?: run {
                put("color", false)
                put("color_name", defaultColorName)
            }
            put("format", (mode == DiktatMode.FIX))
            if (groupByFileInPlain) {
                require(reporterProvider.isPlain()) {
                    "groupByFile is applicable only for plain reporter"
                }
                put("group_by_file", true)
            }
        }.mapValues { it.toString() },
    )
}
