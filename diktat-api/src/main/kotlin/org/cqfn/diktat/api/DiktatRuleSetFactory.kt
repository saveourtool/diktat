package org.cqfn.diktat.api

import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * A factory which creates a [DiktatRuleSet].
 */
interface DiktatRuleSetFactory : Function0<DiktatRuleSet> {
    /**
     * @return the default instance of [DiktatRuleSet]
     */
    override operator fun invoke(): DiktatRuleSet

    /**
     * @param configFile a path to file with configuration for diktat (`diktat-analysis.yml`)
     * @return created [DiktatRuleSet] using [configFile]
     */
    fun create(configFile: String): DiktatRuleSet

    /**
     * @param configFile a file with configuration for diktat (`diktat-analysis.yml`)
     * @return created [DiktatRuleSet] using [configFile]
     */
    fun create(configFile: Path): DiktatRuleSet = create(configFile.absolutePathString())
}
