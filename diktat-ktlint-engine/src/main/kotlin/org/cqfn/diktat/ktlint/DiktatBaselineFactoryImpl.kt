package org.cqfn.diktat.ktlint

import org.cqfn.diktat.api.DiktatBaseline
import org.cqfn.diktat.api.DiktatBaselineFactory
import org.cqfn.diktat.api.DiktatProcessorListener
import org.cqfn.diktat.api.DiktatProcessorListener.Companion.closeAfterAllAsProcessorListener
import org.cqfn.diktat.ktlint.DiktatErrorImpl.Companion.wrap
import org.cqfn.diktat.ktlint.DiktatReporterImpl.Companion.wrap
import com.pinterest.ktlint.core.internal.loadBaseline
import com.pinterest.ktlint.reporter.baseline.BaselineReporter
import java.io.PrintStream
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.outputStream

/**
 * A factory to create or generate [DiktatBaseline] using `KtLint`
 */
@Suppress("DEPRECATION")
class DiktatBaselineFactoryImpl : DiktatBaselineFactory {
    override fun tryToLoad(
        baselineFile: Path,
        sourceRootDir: Path,
    ): DiktatBaseline? = loadBaseline(baselineFile.absolutePathString())
        .takeUnless { it.baselineGenerationNeeded }
        ?.let { ktLintBaseline ->
            DiktatBaseline { file ->
                ktLintBaseline.baselineRules
                    ?.get(file.relativePathStringTo(sourceRootDir))
                    .orEmpty()
                    .map { it.wrap() }
                    .toSet()
            }
        }

    override fun generator(baselineFile: Path, sourceRootDir: Path): DiktatProcessorListener {
        val outputStream = baselineFile.outputStream()
        return DiktatProcessorListener(
            BaselineReporter(PrintStream(outputStream)).wrap(sourceRootDir),
            outputStream.closeAfterAllAsProcessorListener()
        )
    }
}
