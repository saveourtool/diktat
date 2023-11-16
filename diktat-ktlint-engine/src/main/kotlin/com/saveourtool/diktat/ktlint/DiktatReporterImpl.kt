package com.saveourtool.diktat.ktlint

import com.saveourtool.diktat.api.DiktatError
import com.saveourtool.diktat.api.DiktatReporter
import com.saveourtool.diktat.ktlint.ReporterV2Wrapper.Companion.unwrapIfNeeded
import com.saveourtool.diktat.util.DiktatProcessorListenerWrapper
import com.pinterest.ktlint.cli.reporter.core.api.ReporterV2
import java.nio.file.Path

/**
 * [DiktatReporter] using __KtLint__
 *
 * @param ktLintReporter
 * @param sourceRootDir
 */
class DiktatReporterImpl(
    ktLintReporter: ReporterV2,
    private val sourceRootDir: Path?,
) : DiktatProcessorListenerWrapper<ReporterV2>(ktLintReporter) {
    override fun doBeforeAll(wrappedValue: ReporterV2, files: Collection<Path>): Unit = wrappedValue.beforeAll()
    override fun doBefore(wrappedValue: ReporterV2, file: Path): Unit = wrappedValue.before(file.relativePathStringTo(sourceRootDir))
    override fun doOnError(
        wrappedValue: ReporterV2,
        file: Path,
        error: DiktatError,
        isCorrected: Boolean,
    ): Unit = wrappedValue.onLintError(file.relativePathStringTo(sourceRootDir), error.toKtLintForCli())
    override fun doAfter(wrappedValue: ReporterV2, file: Path): Unit = wrappedValue.after(file.relativePathStringTo(sourceRootDir))
    override fun doAfterAll(wrappedValue: ReporterV2): Unit = wrappedValue.afterAll()

    companion object {
        /**
         * @param sourceRootDir
         * @return [DiktatReporter] which wraps __KtLint__'s [ReporterV2]
         */
        fun ReporterV2.wrap(sourceRootDir: Path?): DiktatReporter = DiktatReporterImpl(this, sourceRootDir)

        /**
         * @return __KtLint__'s [ReporterV2]
         */
        fun DiktatReporter.unwrapToKtlint(): ReporterV2 = unwrap<ReporterV2>().unwrapIfNeeded()
    }
}
