package com.saveourtool.diktat.ktlint

import com.saveourtool.diktat.api.DiktatError
import com.saveourtool.diktat.api.DiktatReporter
import com.saveourtool.diktat.ktlint.ReporterV2Wrapper.Companion.unwrapIfNeeded
import com.pinterest.ktlint.cli.reporter.core.api.ReporterV2
import java.nio.file.Path

/**
 * [DiktatReporter] using __KtLint__
 *
 * @param ktLintReporter
 * @param sourceRootDir
 */
class DiktatReporterImpl(
    private val ktLintReporter: ReporterV2,
    private val sourceRootDir: Path?,
) : DiktatReporter {
    override fun beforeAll(files: Collection<Path>): Unit = ktLintReporter.beforeAll()
    override fun before(file: Path): Unit = ktLintReporter.before(file.relativePathStringTo(sourceRootDir))
    override fun onError(
        file: Path,
        error: DiktatError,
        isCorrected: Boolean,
    ): Unit = ktLintReporter.onLintError(file.relativePathStringTo(sourceRootDir), error.toKtLintForCli())
    override fun after(file: Path): Unit = ktLintReporter.after(file.relativePathStringTo(sourceRootDir))
    override fun afterAll(): Unit = ktLintReporter.afterAll()

    companion object {
        /**
         * @param sourceRootDir
         * @return [DiktatReporter] which wraps __KtLint__'s [ReporterV2]
         */
        fun ReporterV2.wrap(sourceRootDir: Path?): DiktatReporter = DiktatReporterImpl(this, sourceRootDir)

        /**
         * @return __KtLint__'s [ReporterV2]
         */
        fun DiktatReporter.unwrap(): ReporterV2 = (this as? DiktatReporterImpl)?.ktLintReporter?.unwrapIfNeeded()
            ?: error("Unsupported wrapper of ${DiktatReporter::class.java.simpleName}: ${this::class.java.canonicalName}")
    }
}
