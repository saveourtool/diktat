package com.saveourtool.diktat.ktlint

import com.saveourtool.diktat.api.DiktatReporter
import com.saveourtool.diktat.api.DiktatReporterArguments
import com.saveourtool.diktat.api.DiktatReporterFactory
import com.saveourtool.diktat.api.PlainDiktatReporterArguments
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
    private val reporterProviders = setOf(
        JsonReporterProvider(),
        SarifReporterProvider(),
        CheckStyleReporterProvider(),
        HtmlReporterProvider(),
    )
        .associateBy { it.id } + (DiktatReporterFactory.PLAIN_ID to plainReporterProvider)

    override val ids: Set<String>
        get() = reporterProviders.keys

    override val colorNamesInPlain: Set<String>
        get() = Color.entries.map { it.name }.toSet()

    override fun invoke(
        args: DiktatReporterArguments,
    ): DiktatReporter {
        if (args.id == DiktatReporterFactory.NONE_ID) {
            return DiktatReporter.empty
        }
        val opts = if (args is PlainDiktatReporterArguments) {
            buildMap<String, Any> {
                args.colorName?.let {
                    put("color", true)
                    put("color_name", it)
                } ?: run {
                    put("color", false)
                    put("color_name", Color.DARK_GRAY)
                }
                args.groupByFile?.let { put("group_by_file", it) }
            }.mapValues { it.value.toString() }
        } else if (args.id == DiktatReporterFactory.PLAIN_ID) {
            mapOf("color_name" to Color.DARK_GRAY.name)
        } else {
            emptyMap()
        }

        val reporterProvider = reporterProviders[args.id] ?: throw IllegalArgumentException("Not supported reporter id by ${DiktatBaselineFactoryImpl::class.simpleName}")
        if (reporterProvider is SarifReporterProvider) {
            args.sourceRootDir?.let { System.setProperty("user.home", it.pathString) }
        }
        return reporterProvider.get(args.outputStream, args.closeOutputStreamAfterAll, opts).wrap(args.sourceRootDir)
    }
}
