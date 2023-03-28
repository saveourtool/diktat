package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatLogLevel
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * Processor to run `diktat`
 *
 * @property diktatRuleSetProvider
 * @property logLevel
 */
class DiktatProcessor private constructor(
    val diktatRuleSetProvider: DiktatRuleSetProvider,
    val logLevel: DiktatLogLevel,
) {
    companion object {
        /**
         * @return a builder for [DiktatProcessor]
         */
        fun builder(): Builder = Builder()
    }

    /**
     * Builder for [DiktatProcessCommand]
     *
     * @property diktatRuleSetProvider
     * @property logLevel
     */
    class Builder internal constructor(
        private var diktatRuleSetProvider: DiktatRuleSetProvider? = null,
        private var logLevel: DiktatLogLevel = DiktatLogLevel.INFO,
    ) {
        /**
         * @param diktatRuleSetProvider
         * @return updated builder
         */
        fun diktatRuleSetProvider(diktatRuleSetProvider: DiktatRuleSetProvider) = apply { this.diktatRuleSetProvider = diktatRuleSetProvider }


        /**
         * @param configFile a config file to load [DiktatRuleSetProvider]
         * @return updated builder
         */
        fun diktatRuleSetProvider(configFile: String) = diktatRuleSetProvider(DiktatRuleSetProvider(configFile))

        /**
         * @param configFile a config file to load [DiktatRuleSetProvider]
         * @return updated builder
         */
        fun diktatRuleSetProvider(configFile: Path) = diktatRuleSetProvider(configFile.absolutePathString())

        /**
         * @param logLevel
         * @return updated builder
         */
        fun logLevel(logLevel: DiktatLogLevel) = apply { this.logLevel = logLevel }

        /**
         * @return built [DiktatProcessCommand]
         */
        fun build(): DiktatProcessor = DiktatProcessor(
            diktatRuleSetProvider = diktatRuleSetProvider ?: DiktatRuleSetProvider(),
            logLevel = logLevel,
        )
    }
}
