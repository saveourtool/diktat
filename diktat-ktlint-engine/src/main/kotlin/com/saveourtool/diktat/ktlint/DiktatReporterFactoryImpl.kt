package com.saveourtool.diktat.ktlint

import com.saveourtool.diktat.api.DiktatReporter
import com.saveourtool.diktat.api.DiktatReporterCreationArguments
import com.saveourtool.diktat.api.DiktatReporterFactory
import com.saveourtool.diktat.api.DiktatReporterType
import com.saveourtool.diktat.api.PlainDiktatReporterCreationArguments
import com.saveourtool.diktat.ktlint.DiktatReporterImpl.Companion.wrap
import com.pinterest.ktlint.cli.reporter.checkstyle.CheckStyleReporterProvider
import com.pinterest.ktlint.cli.reporter.html.HtmlReporterProvider
import com.pinterest.ktlint.cli.reporter.json.JsonReporterProvider
import com.pinterest.ktlint.cli.reporter.plain.Color
import com.pinterest.ktlint.cli.reporter.plain.PlainReporterProvider
import com.pinterest.ktlint.cli.reporter.sarif.SarifReporterProvider
import kotlin.io.path.pathString

/**
 * A factory to create [DiktatReporter] using `KtLint`
 */
class DiktatReporterFactoryImpl : DiktatReporterFactory {
    private val plainReporterProvider = PlainReporterProvider()

    /**
     * All reporters which __KtLint__ provides
     */
    private val reporterProviders = mapOf(
        DiktatReporterType.JSON to JsonReporterProvider(),
        DiktatReporterType.SARIF to SarifReporterProvider(),
        DiktatReporterType.CHECKSTYLE to CheckStyleReporterProvider(),
        DiktatReporterType.HTML to HtmlReporterProvider(),
        DiktatReporterType.PLAIN to plainReporterProvider,
    )

    override val colorNamesInPlain: Set<String>
        get() = Color.entries.map { it.name }.toSet()

    override fun invoke(
        args: DiktatReporterCreationArguments,
    ): DiktatReporter {
        if (args.reporterType == DiktatReporterType.NONE) {
            return DiktatReporter.empty
        }
        val opts = if (args is PlainDiktatReporterCreationArguments) {
            buildMap<String, Any> {
                put("color", args.colorName?.let { true } ?: false)
                put("color_name", args.colorName ?: Color.DARK_GRAY)
                args.groupByFile?.let { put("group_by_file", it) }
            }.mapValues { it.value.toString() }
        } else if (args.reporterType == DiktatReporterType.PLAIN) {
            mapOf("color_name" to Color.DARK_GRAY.name)
        } else {
            emptyMap()
        }

        val reporterProvider = reporterProviders[args.reporterType] ?: throw IllegalArgumentException("Not supported reporter id by ${DiktatBaselineFactoryImpl::class.simpleName}")
        if (reporterProvider is SarifReporterProvider) {
            args.sourceRootDir?.let { System.setProperty("user.home", it.pathString) }
        }
        return reporterProvider.get(args.outputStream, args.closeOutputStreamAfterAll, opts).wrap(args.sourceRootDir)
    }
}
