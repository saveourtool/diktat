package com.saveourtool.diktat

import com.saveourtool.diktat.api.DiktatBaseline
import com.saveourtool.diktat.api.DiktatBaselineFactory
import com.saveourtool.diktat.api.DiktatProcessorListener
import com.saveourtool.diktat.api.DiktatReporter
import com.saveourtool.diktat.api.DiktatReporterFactory
import com.saveourtool.diktat.api.DiktatRuleConfig
import com.saveourtool.diktat.api.DiktatRuleConfigReader
import com.saveourtool.diktat.api.DiktatRuleSet
import com.saveourtool.diktat.api.DiktatRuleSetFactory
import java.io.OutputStream
import java.nio.file.Path

/**
 * A factory to create [DiktatRunner]
 *
 * @param diktatRuleConfigReader a reader for [DiktatRuleConfig]
 * @param diktatRuleSetFactory a factory for [DiktatRuleSet]
 * @param diktatProcessorFactory a factory for [DiktatProcessor]
 * @param diktatBaselineFactory a factory for [DiktatBaseline]
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
        val reporter = resolveReporter(
            args.reporterType, args.reporterOutput,
            args.colorNameInPlain, args.groupByFileInPlain,
            args.sourceRootDir
        )
        return DiktatRunner(
            diktatProcessor = processor,
            diktatBaseline = baseline,
            diktatBaselineGenerator = baselineGenerator,
            diktatReporter = reporter,
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
    ): DiktatReporter {
        val (outputStream, closeOutputStream) = reporterOutput?.let { it to true } ?: (System.`out` to false)
        return if (reporterType == diktatReporterFactory.plainId) {
            diktatReporterFactory.createPlain(outputStream, closeOutputStream, sourceRootDir, colorNameInPlain, groupByFileInPlain)
        } else {
            require(colorNameInPlain == null) {
                "colorization is applicable only for plain reporter"
            }
            require(groupByFileInPlain == null) {
                "groupByFile is applicable only for plain reporter"
            }
            diktatReporterFactory(reporterType, outputStream, closeOutputStream, sourceRootDir)
        }
    }
}
