package org.cqfn.diktat.ktlint

import org.cqfn.diktat.api.DiktatError
import org.cqfn.diktat.api.DiktatReporter
import org.cqfn.diktat.ktlint.DiktatErrorImpl.Companion.unwrap
import com.pinterest.ktlint.core.Reporter
import java.nio.file.Path

/**
 * [DiktatReporter] using __KtLint__
 */
class DiktatReporterImpl(
    private val ktLintReporter: Reporter,
    private val sourceRootDir: Path,
) : DiktatReporter {
    override fun beforeAll(): Unit = ktLintReporter.beforeAll()
    override fun before(file: Path): Unit = ktLintReporter.before(file.relativePathStringTo(sourceRootDir))
    override fun onError(
        file: Path,
        error: DiktatError,
        isCorrected: Boolean,
    ): Unit = ktLintReporter.onLintError(file.relativePathStringTo(sourceRootDir), error.unwrap(), isCorrected)
    override fun after(file: Path): Unit = ktLintReporter.after(file.relativePathStringTo(sourceRootDir))
    override fun afterAll(): Unit = ktLintReporter.beforeAll()

    companion object {
        /**
         * @param sourceRootDir
         * @return [DiktatReporter] which wraps __KtLint__'s [Reporter]
         */
        fun Reporter.wrap(sourceRootDir: Path): DiktatReporter = DiktatReporterImpl(this, sourceRootDir)

        /**
         * @return __KtLint__'s [Reporter]
         */
        internal fun DiktatReporter.unwrap(): Reporter = (this as? DiktatReporterImpl)?.ktLintReporter
            ?: error("Unsupported wrapper of ${DiktatReporter::class.java.simpleName}: ${this::class.java.canonicalName}")
    }
}
