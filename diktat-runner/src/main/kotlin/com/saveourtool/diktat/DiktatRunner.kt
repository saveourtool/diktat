package com.saveourtool.diktat

import com.saveourtool.diktat.api.DiktatBaseline
import com.saveourtool.diktat.api.DiktatBaseline.Companion.skipKnownErrors
import com.saveourtool.diktat.api.DiktatProcessorListener
import com.saveourtool.diktat.api.DiktatProcessorListener.Companion.countErrorsAsProcessorListener
import com.saveourtool.diktat.api.DiktatReporter
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.readText
import kotlin.io.path.writeText

private typealias RunAction = (DiktatProcessor, DiktatProcessorListener) -> Unit

/**
 * A runner for diktat on bunch of files using baseline and reporter
 */
object DiktatRunner {
    /**
     * Run `diktat fix` for all [DiktatRunnerArguments.files].
     *
     * @param args
     * @param fileUpdateNotifier notifier about updated files
     * @return count of detected errors
     */
    fun fixAll(
        args: DiktatRunnerArguments,
        fileUpdateNotifier: (Path) -> Unit,
    ): Int = doRun(args) { processor, listener ->
        listener.beforeAll(args.files)
        args.files.forEach { file ->
            listener.before(file)
            val formattedContent = processor.fix(file) { error, isCorrected ->
                listener.onError(file, error, isCorrected)
            }
            val fileContent = file.readText(Charsets.UTF_8)
            if (fileContent != formattedContent) {
                fileUpdateNotifier(file)
                file.writeText(formattedContent, Charsets.UTF_8)
            }
            listener.after(file)
        }
        listener.afterAll()
    }

    /**
     * Run `diktat check` for all [DiktatRunnerArguments.files].
     *
     * @param args
     * @return count of detected errors
     */
    fun checkAll(
        args: DiktatRunnerArguments,
    ): Int = doRun(args) { processor, listener ->
        listener.beforeAll(args.files)
        args.files.forEach { file ->
            listener.before(file)
            processor.check(file) { error, isCorrected ->
                listener.onError(file, error, isCorrected)
            }
            listener.after(file)
        }
        listener.afterAll()
    }
    private fun doRun(
        args: DiktatRunnerArguments,
        runAction: RunAction,
    ): Int {
        val diktatRuleConfigs = diktatRuleConfigReader(args.configInputStream)
        val diktatRuleSet = diktatRuleSetFactory(diktatRuleConfigs)
        val processor = diktatProcessorFactory(diktatRuleSet)
        val (baseline, baselineGenerator) = resolveBaseline(args.baselineFile, args.sourceRootDir)

        val reporter = args.reporterArgsList
            .map { diktatReporterFactory(it) }
            .let { DiktatReporter.union(it) }

        val errorCounter = AtomicInteger()
        runAction(
            processor,
            DiktatProcessorListener(
                args.loggingListener,
                DiktatReporter(reporter.skipKnownErrors(baseline), baselineGenerator),
                errorCounter.countErrorsAsProcessorListener()
            ),
        )
        return errorCounter.get()
    }

    private fun resolveBaseline(
        baselineFile: Path?,
        sourceRootDir: Path?,
    ): Pair<DiktatBaseline, DiktatProcessorListener> = baselineFile
        ?.let { diktatBaselineFactory.tryToLoad(it, sourceRootDir) }
        ?.let { it to DiktatProcessorListener.empty }
        ?: run {
            val baselineGenerator = baselineFile?.let {
                diktatBaselineFactory.generator(it, sourceRootDir)
            } ?: DiktatProcessorListener.empty
            DiktatBaseline.empty to baselineGenerator
        }
}
