package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatBaseline
import org.cqfn.diktat.api.DiktatBaselineFactory
import org.cqfn.diktat.api.DiktatProcessorListener
import org.cqfn.diktat.api.DiktatProcessorListener.Companion.closeAfterAllAsProcessorListener
import org.cqfn.diktat.api.DiktatReporter
import org.cqfn.diktat.api.DiktatReporterFactory
import org.cqfn.diktat.api.DiktatRuleSetFactory
import java.io.OutputStream
import java.nio.file.Path

/**
 * A factory to create [DiktatRunner]
 */
class DiktatRunnerFactory(
    private val diktatRuleSetFactory: DiktatRuleSetFactory,
    private val diktatProcessorFactory: DiktatProcessorFactory,
    private val diktatBaselineFactory: DiktatBaselineFactory,
    private val diktatReporterFactory: DiktatReporterFactory,
) : Function1<DiktatRunnerArguments, DiktatRunner> {
    /**
     * @param args
     * @return an instance of [DiktatRunner] created using [args]
     */
    override fun invoke(args: DiktatRunnerArguments): DiktatRunner {
        val diktatRuleSet = diktatRuleSetFactory.create(args.configFileName)
        val processor = diktatProcessorFactory(diktatRuleSet)
        val (baseline, baselineGenerator) = resolveBaseline(args.baselineFile, args.sourceRootDir)
        val (reporter, closer) = resolveReporter(args.reporterType, args.reporterOutput, args.sourceRootDir)
        return DiktatRunner(
            diktatProcessor = processor,
            diktatBaseline = baseline,
            diktatBaselineGenerator = baselineGenerator,
            diktatReporter = reporter,
            diktatReporterCloser = closer,
        )
    }

    private fun resolveBaseline(
        baselineFile: Path?,
        sourceRootDir: Path,
    ): Pair<DiktatBaseline, DiktatProcessorListener> = baselineFile
        ?.let { diktatBaselineFactory.tryToLoad(it, sourceRootDir) }
        ?.let { it to DiktatProcessorListener.empty }
        ?: run {
            val baselineGenerator = baselineFile?.let {
                diktatBaselineFactory.generator(it, sourceRootDir)
            } ?: DiktatProcessorListener.empty
            DiktatBaseline.empty to baselineGenerator
        }

    private fun resolveReporter(
        reporterType: String,
        reporterOutput: OutputStream?,
        sourceRootDir: Path,
    ): Pair<DiktatReporter, DiktatProcessorListener> {
        val (outputStream, closeListener) = reporterOutput
            ?.let { it to it.closeAfterAllAsProcessorListener() }
            ?: run {
                System.`out` to DiktatProcessorListener.empty
            }
        val actualReporter = diktatReporterFactory(reporterType, outputStream, sourceRootDir)
        return actualReporter to closeListener
    }
}
