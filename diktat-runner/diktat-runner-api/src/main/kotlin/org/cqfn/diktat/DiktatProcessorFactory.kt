package org.cqfn.diktat

import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * A factory to create [DiktatProcessor] using [DiktatRuleSetProvider]
 */
@FunctionalInterface
fun interface DiktatProcessorFactory : Function1<DiktatRuleSetProvider, DiktatProcessor> {
    /**
     * @param diktatRuleSetProvider
     * @return created [DiktatProcessor] using [diktatRuleSetProvider]
     */
    override fun invoke(diktatRuleSetProvider: DiktatRuleSetProvider): DiktatProcessor

    /**
     * @param configFile a path to file with configuration for diktat (`diktat-analysis.yml`)
     * @return created [DiktatProcessor] using [configFile]
     */
    fun create(configFile: String): DiktatProcessor = invoke(DiktatRuleSetProvider(configFile))

    /**
     * @param configFile a file with configuration for diktat (`diktat-analysis.yml`)
     * @return created [DiktatProcessor] using [configFile]
     */
    fun create(configFile: Path): DiktatProcessor = create(configFile.absolutePathString())
}
