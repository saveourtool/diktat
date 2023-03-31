/**
 * File contains util methods to create ktlint's [Reporter]
 */

package org.cqfn.diktat.ktlint

import org.cqfn.diktat.api.DiktatMode
import com.pinterest.ktlint.core.Reporter
import com.pinterest.ktlint.core.ReporterProvider
import com.pinterest.ktlint.reporter.checkstyle.CheckStyleReporterProvider
import com.pinterest.ktlint.reporter.html.HtmlReporterProvider
import com.pinterest.ktlint.reporter.json.JsonReporterProvider
import com.pinterest.ktlint.reporter.plain.internal.Color
import com.pinterest.ktlint.reporter.plain.PlainReporterProvider
import com.pinterest.ktlint.reporter.sarif.SarifReporterProvider
import java.io.PrintStream
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream

/**
 * supported color names in __KtLint__, taken from [Color]
 */
val colorNamesForPlainReporter = Color.values().map { it.name }

/**
 * A default [ReporterProvider] for [PlainReporterProvider]
 */
val plainReporterProvider = PlainReporterProvider()

/**
 * All [ReporterProvider] which __KtLint__ provides
 */
val reporterProviders = setOf(
    plainReporterProvider,
    JsonReporterProvider(),
    SarifReporterProvider(),
    CheckStyleReporterProvider(),
    HtmlReporterProvider(),
)
    .associateBy { it.id }

/**
 * @return true if receiver is [PlainReporterProvider]
 */
internal fun ReporterProvider<*>.isPlain(): Boolean = id == plainReporterProvider.id

/**
 * @param reporterProviderId
 * @param output
 * @param colorNameInPlain
 * @param groupByFileInPlain
 * @param mode
 * @return a configured [Reporter]
 */
fun buildReporter(
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
                put("color_name", Color.DARK_GRAY.name)
            }
            put("format", (mode == DiktatMode.FIX))
            if (groupByFileInPlain) {
                require(reporterProvider.isPlain()) {
                    "groupByFile is applicable only for plain reporter"
                }
                put("group_by_file", true)
            }
        }.mapValues { it.value.toString() },
    )
}
