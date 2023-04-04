package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatBaseline
import org.cqfn.diktat.api.DiktatBaseline.Companion.skipKnownErrors
import org.cqfn.diktat.api.DiktatBaselineFactory
import org.cqfn.diktat.api.DiktatProcessorListener
import org.cqfn.diktat.api.DiktatProcessorListener.Companion.closeAfterAllAsProcessorListener
import org.cqfn.diktat.api.DiktatProcessorListener.Companion.countErrorsAsProcessorListener
import org.cqfn.diktat.api.DiktatReporterFactory
import org.cqfn.diktat.api.DiktatRuleSetFactory
import java.io.OutputStream
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.readText
import kotlin.io.path.writeText

private typealias RunAction = (DiktatProcessor, DiktatProcessorListener) -> Unit

/**
 * A runner for diktat on bunch of files using baseline and reporter
 */
data class DiktatRunner(
    private val diktatRuleSetFactory: DiktatRuleSetFactory,
    private val diktatProcessorFactory: DiktatProcessorFactory,
    private val diktatBaselineFactory: DiktatBaselineFactory,
    private val diktatReporterFactory: DiktatReporterFactory,
    private val loggingListener: DiktatProcessorListener = DiktatProcessorListener.empty,
) {
    private fun doRun(
        args: DiktatRunnerArguments,
        runAction: RunAction,
    ): Int {
        val diktatRuleSet = diktatRuleSetFactory.create(args.configFileName)
        val diktatProcessor = diktatProcessorFactory(diktatRuleSet)
        val (baseline, baselineGenerator) = resolveBaseline(args.baselineFile, args.sourceRootDir)
        val processorListener = resolveReporter(args.reporterType, args.reporterOutput, args.sourceRootDir)
        val errorCounter = AtomicInteger()
        runAction(
            diktatProcessor,
            DiktatProcessorListener(
                loggingListener,
                processorListener.skipKnownErrors(baseline),
                baselineGenerator,
                errorCounter.countErrorsAsProcessorListener()
            ),
        )
        return errorCounter.get()
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
    ): DiktatProcessorListener {
        val (outputStream, closeListener) = reporterOutput
            ?.let { it to it.closeAfterAllAsProcessorListener() }
            ?: run {
                System.`out` to DiktatProcessorListener.empty
            }
        val actualReporter = diktatReporterFactory(reporterType, outputStream, sourceRootDir)
        return DiktatProcessorListener(actualReporter, closeListener)
    }

    /**
     * Run `diktat fix` for all [files].
     *
     * @param args
     * @param fileUpdateNotifier notifier about updated files
     * @return count of detected errors
     */
    fun fixAll(
        args: DiktatRunnerArguments,
        fileUpdateNotifier: (Path) -> Unit,
    ): Int = doRun(args) { processor, listener ->
        processor.fixAll(listener, args.files) { file, formattedText ->
            val fileContent = file.readText(Charsets.UTF_8)
            if (fileContent != formattedText) {
                fileUpdateNotifier(file)
                file.writeText(formattedText, Charsets.UTF_8)
            }
        }
    }

    /**
     * Run `diktat check` for all [files].
     *
     * @param args
     * @return count of detected errors
     */
    fun checkAll(
        args: DiktatRunnerArguments,
    ): Int = doRun(args) { processor, listener ->
        processor.checkAll(listener, args.files)
    }
}
