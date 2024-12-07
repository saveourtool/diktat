package com.saveourtool.diktat.ktlint

import com.saveourtool.diktat.api.DiktatBaseline
import com.saveourtool.diktat.api.DiktatBaselineFactory
import com.saveourtool.diktat.api.DiktatProcessorListener
import com.saveourtool.diktat.ktlint.DiktatReporterImpl.Companion.wrap

import com.pinterest.ktlint.cli.reporter.baseline.Baseline
import com.pinterest.ktlint.cli.reporter.baseline.BaselineErrorHandling
import com.pinterest.ktlint.cli.reporter.baseline.BaselineReporterProvider
import com.pinterest.ktlint.cli.reporter.baseline.loadBaseline

import java.nio.file.Path

import kotlin.io.path.absolutePathString
import kotlin.io.path.outputStream

/**
 * A factory to create or generate [DiktatBaseline] using `KtLint`
 */
class DiktatBaselineFactoryImpl : DiktatBaselineFactory {
    private val baselineReporterProvider = BaselineReporterProvider()

    override fun tryToLoad(
        baselineFile: Path,
        sourceRootDir: Path?,
    ): DiktatBaseline? = loadBaseline(baselineFile.absolutePathString(), BaselineErrorHandling.LOG)
        .takeIf { it.status == Baseline.Status.VALID }
        ?.let { ktLintBaseline ->
            DiktatBaseline { file ->
                ktLintBaseline.lintErrorsPerFile[file.relativePathStringTo(sourceRootDir)]
                    .orEmpty()
                    .map { it.wrap() }
                    .toSet()
            }
        }

    override fun generator(
        baselineFile: Path,
        sourceRootDir: Path?,
    ): DiktatProcessorListener = baselineReporterProvider.get(
        baselineFile.outputStream(),
        closeOutAfterAll = true,
        emptyMap(),
    ).wrap(sourceRootDir)
}
