package org.cqfn.diktat.ktlint

import org.cqfn.diktat.api.DiktatReporter
import org.cqfn.diktat.api.DiktatReporterFactory
import org.cqfn.diktat.ktlint.DiktatReporterImpl.Companion.wrap
import com.pinterest.ktlint.core.ReporterProvider
import com.pinterest.ktlint.reporter.checkstyle.CheckStyleReporterProvider
import com.pinterest.ktlint.reporter.html.HtmlReporterProvider
import com.pinterest.ktlint.reporter.json.JsonReporterProvider
import com.pinterest.ktlint.reporter.plain.PlainReporterProvider
import com.pinterest.ktlint.reporter.sarif.SarifReporterProvider
import java.io.OutputStream
import java.io.PrintStream
import java.nio.file.Path
import kotlin.io.path.pathString

/**
 * A factory to create [DiktatReporter] using `KtLint`
 */
class DiktatReporterFactoryImpl : DiktatReporterFactory {
    /**
     * All [ReporterProvider] which __KtLint__ provides
     */
    private val reporterProviders = setOf(
        PlainReporterProvider(),
        JsonReporterProvider(),
        SarifReporterProvider(),
        CheckStyleReporterProvider(),
        HtmlReporterProvider(),
    )
        .associateBy { it.id }

    override fun invoke(
        id: String,
        outputStream: OutputStream,
        sourceRootDir: Path,
    ): DiktatReporter {
        val reporterProvider = reporterProviders[id] ?: throw IllegalArgumentException("Not supported reporter id by ${DiktatBaselineFactoryImpl::class.simpleName}")
        if (reporterProvider is SarifReporterProvider) {
            System.setProperty("user.home", sourceRootDir.pathString)
        }
        val printStream = (outputStream as? PrintStream) ?: PrintStream(outputStream)
        return reporterProvider.get(printStream, emptyMap()).wrap(sourceRootDir)
    }
}
