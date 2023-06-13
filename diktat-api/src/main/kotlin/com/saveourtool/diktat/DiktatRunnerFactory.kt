package com.saveourtool.diktat

import com.saveourtool.diktat.api.DiktatBaseline
import com.saveourtool.diktat.api.DiktatBaselineFactory
import com.saveourtool.diktat.api.DiktatProcessorListener
import com.saveourtool.diktat.api.DiktatProcessorListener.Companion.closeAfterAllAsProcessorListener
import com.saveourtool.diktat.api.DiktatReporter
import com.saveourtool.diktat.api.DiktatReporterFactory
import com.saveourtool.diktat.api.DiktatRuleConfigReader
import com.saveourtool.diktat.api.DiktatRuleSetFactory
import java.io.OutputStream
import java.nio.file.Path

/**
 * A factory to create [DiktatRunner]
 *
 * @property diktatReporterFactory a factory for [DiktatReporter]
 */
class DiktatRunnerFactory(
    private val diktatRuleConfigReader: DiktatRuleConfigReader,
    private val diktatRuleSetFactory: DiktatRuleSetFactory,
    private val diktatProcessorFactory: DiktatProcessorFactory,
    private val diktatBaselineFactory: DiktatBaselineFactory,
    val diktatReporterFactory: DiktatReporterFactory,
) : Function1<DiktatRunnerArguments, DiktatRunner> {
    /**
     * @param args
     * @return an instance of [DiktatRunner] created using [args]
     */
    override fun invoke(args: DiktatRunnerArguments): DiktatRunner {
        val diktatRuleConfigs = diktatRuleConfigReader(args.configInputStream)
        val diktatRuleSet = diktatRuleSetFactory(diktatRuleConfigs)
        val processor = diktatProcessorFactory(diktatRuleSet)
        val (baseline, baselineGenerator) = resolveBaseline(args.baselineFile, args.sourceRootDir)
        val (reporter, closer) = resolveReporter(
            args.reporterType, args.reporterOutput,
            args.colorNameInPlain, args.groupByFileInPlain,
            args.sourceRootDir
        )
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
        colorNameInPlain: String?,
        groupByFileInPlain: Boolean?,
        sourceRootDir: Path,
    ): Pair<DiktatReporter, DiktatProcessorListener> {
        val (outputStream, closeListener) = reporterOutput
            ?.let { it to it.closeAfterAllAsProcessorListener() }
            ?: run {
                System.`out` to DiktatProcessorListener.empty
            }
        val actualReporter = if (reporterType == diktatReporterFactory.plainId) {
            diktatReporterFactory.createPlain(outputStream, sourceRootDir, colorNameInPlain, groupByFileInPlain)
        } else {
            require(colorNameInPlain == null) {
                "colorization is applicable only for plain reporter"
            }
            require(groupByFileInPlain == null) {
                "groupByFile is applicable only for plain reporter"
            }
            diktatReporterFactory(reporterType, outputStream, sourceRootDir)
        }
        return actualReporter to closeListener
    }
}
