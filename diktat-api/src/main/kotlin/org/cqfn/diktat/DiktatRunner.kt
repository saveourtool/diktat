package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatBaseline
import org.cqfn.diktat.api.DiktatBaseline.Companion.skipKnownErrors
import org.cqfn.diktat.api.DiktatBaselineFactory
import org.cqfn.diktat.api.DiktatProcessorListener
import org.cqfn.diktat.api.DiktatProcessorListener.Companion.closeAfterAllAsProcessorListener
import org.cqfn.diktat.api.DiktatProcessorListener.Companion.countErrorsAsProcessorListener
import org.cqfn.diktat.api.DiktatReporter
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
 *
 * @property diktatProcessor
 * @property diktatBaseline
 * @property diktatBaselineGenerator
 * @property diktatReporter
 * @property diktatReporterCloser
 */
data class DiktatRunner(
    val diktatProcessor: DiktatProcessor,
    val diktatBaseline: DiktatBaseline,
    private val diktatBaselineGenerator: DiktatProcessorListener,
    val diktatReporter: DiktatReporter,
    private val diktatReporterCloser: DiktatProcessorListener,
) {
    private fun doRun(
        args: DiktatRunnerArguments,
        runAction: RunAction,
    ): Int {
        val errorCounter = AtomicInteger()
        runAction(
            diktatProcessor,
            DiktatProcessorListener(
                args.loggingListener,
                diktatReporter.skipKnownErrors(diktatBaseline),
                diktatReporterCloser,
                diktatBaselineGenerator,
                errorCounter.countErrorsAsProcessorListener()
            ),
        )
        return errorCounter.get()
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
     * Run `diktat check` for all [files].
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
}
