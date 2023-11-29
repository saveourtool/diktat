package com.saveourtool.diktat

import com.saveourtool.diktat.api.*
import com.saveourtool.diktat.api.DiktatBaseline.Companion.skipKnownErrors
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
) : Function1<DiktatRunnerFactoryArguments, DiktatRunner> {
    /**
     * @param args
     * @return an instance of [DiktatRunner] created using [args]
     */
    override fun invoke(args: DiktatRunnerFactoryArguments): DiktatRunner {
        val diktatRuleConfigs = diktatRuleConfigReader(args.configInputStream)
        val diktatRuleSet = diktatRuleSetFactory(diktatRuleConfigs)
        val processor = diktatProcessorFactory(diktatRuleSet)
        val (baseline, baselineGenerator) = resolveBaseline(args.baselineFile, args.sourceRootDir)

        val reporter = args.reporterArgsList
            .map { diktatReporterFactory(it) }
            .let { DiktatProcessorListener.union(it) }

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
