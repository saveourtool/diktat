package com.saveourtool.diktat

import com.saveourtool.diktat.api.DiktatBaseline
import com.saveourtool.diktat.api.DiktatBaseline.Companion.skipKnownErrors
import com.saveourtool.diktat.api.DiktatBaselineFactory
import com.saveourtool.diktat.api.DiktatProcessorListener
import com.saveourtool.diktat.api.DiktatReporter
import com.saveourtool.diktat.api.DiktatReporterFactory
import com.saveourtool.diktat.api.DiktatRuleConfig
import com.saveourtool.diktat.api.DiktatRuleConfigReader
import com.saveourtool.diktat.api.DiktatRuleSet
import com.saveourtool.diktat.api.DiktatRuleSetFactory
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
        val diktatRuleConfigs = args.configInputStream?.let { diktatRuleConfigReader(it) }.orEmpty()
        val diktatRuleSet = diktatRuleSetFactory(diktatRuleConfigs)
        val processor = diktatProcessorFactory(diktatRuleSet)
        val (baseline, baselineGenerator) = resolveBaseline(args.baselineFile, args.sourceRootDir)

        val reporter = args.reporterArgsList
            .map { diktatReporterFactory(it) }
            .let { DiktatReporter.union(it) }

        return DiktatRunner(
            diktatProcessor = processor,
            diktatReporter = DiktatReporter(reporter.skipKnownErrors(baseline), baselineGenerator),
        )
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
