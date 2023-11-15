package com.saveourtool.diktat.ktlint

import com.saveourtool.diktat.api.DiktatReporter
import com.saveourtool.diktat.api.DiktatReporterFactory
import com.saveourtool.diktat.ktlint.DiktatReporterImpl.Companion.wrap
import com.pinterest.ktlint.cli.reporter.checkstyle.CheckStyleReporterProvider
import com.pinterest.ktlint.cli.reporter.html.HtmlReporterProvider
import com.pinterest.ktlint.cli.reporter.json.JsonReporterProvider
import com.pinterest.ktlint.cli.reporter.plain.Color
import com.pinterest.ktlint.cli.reporter.plain.PlainReporterProvider
import com.pinterest.ktlint.cli.reporter.sarif.SarifReporterProvider
import java.io.OutputStream
import java.nio.file.Path
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
        plainReporterProvider,
        JsonReporterProvider(),
        SarifReporterProvider(),
        CheckStyleReporterProvider(),
        HtmlReporterProvider(),
    )
        .associateBy { it.id }

    override val ids: Set<String>
        get() = reporterProviders.keys

    override val plainId: String
        get() = plainReporterProvider.id

    override val colorNamesInPlain: Set<String>
        get() = Color.entries.map { it.name }.toSet()

    override fun invoke(
        id: String,
        outputStream: OutputStream,
        closeOutputStreamAfterAll: Boolean,
        sourceRootDir: Path?,
    ): DiktatReporter {
        val reporterProvider = reporterProviders[id] ?: throw IllegalArgumentException("Not supported reporter id by ${DiktatBaselineFactoryImpl::class.simpleName}")
        if (reporterProvider is SarifReporterProvider) {
            sourceRootDir?.let { System.setProperty("user.home", it.pathString) }
        }
        val opt = if (reporterProvider is PlainReporterProvider) {
            mapOf("color_name" to Color.DARK_GRAY.name)
        } else {
            emptyMap()
        }
        return reporterProvider.get(outputStream, closeOutputStreamAfterAll, opt).wrap(sourceRootDir)
    }

    override fun createPlain(
        outputStream: OutputStream,
        closeOutputStreamAfterAll: Boolean,
        sourceRootDir: Path?,
        colorName: String?,
        groupByFile: Boolean?,
    ): DiktatReporter {
        val opt = buildMap<String, Any> {
            colorName?.let {
                put("color", true)
                put("color_name", it)
            } ?: run {
                put("color", false)
                put("color_name", Color.DARK_GRAY)
            }
            groupByFile?.let { put("group_by_file", it) }
        }.mapValues { it.value.toString() }
        return plainReporterProvider.get(outputStream, closeOutputStreamAfterAll, opt).wrap(sourceRootDir)
    }
}
