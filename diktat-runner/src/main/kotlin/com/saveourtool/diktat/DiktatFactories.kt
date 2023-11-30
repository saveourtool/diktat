/**
 * Contains only initialized factories
 */

package com.saveourtool.diktat

import com.saveourtool.diktat.api.DiktatBaselineFactory
import com.saveourtool.diktat.api.DiktatReporterFactory
import com.saveourtool.diktat.api.DiktatRuleConfigReader
import com.saveourtool.diktat.api.DiktatRuleSetFactory
import com.saveourtool.diktat.ktlint.DiktatBaselineFactoryImpl
import com.saveourtool.diktat.ktlint.DiktatProcessorFactoryImpl
import com.saveourtool.diktat.ktlint.DiktatReporterFactoryImpl
import com.saveourtool.diktat.ruleset.config.DiktatRuleConfigYamlReader
import com.saveourtool.diktat.ruleset.rules.DiktatRuleSetFactoryImpl
import generated.KTLINT_VERSION

/**
 * Info about engine
 */
const val ENGINE_INFO: String = "Ktlint: $KTLINT_VERSION"

/**
 * @return initialized [DiktatRuleConfigReader]
 */
val diktatRuleConfigReader: DiktatRuleConfigReader = DiktatRuleConfigYamlReader()

/**
 * @return initialized [DiktatRuleSetFactory]
 */
val diktatRuleSetFactory: DiktatRuleSetFactory = DiktatRuleSetFactoryImpl()

/**
 * @return initialized [DiktatProcessorFactory]
 */
val diktatProcessorFactory: DiktatProcessorFactory = DiktatProcessorFactoryImpl()

/**
 * @return initialized [DiktatBaselineFactory]
 */
val diktatBaselineFactory: DiktatBaselineFactory = DiktatBaselineFactoryImpl()

/**
 * @return initialized [DiktatReporterFactory]
 */
val diktatReporterFactory: DiktatReporterFactory = DiktatReporterFactoryImpl()

/**
 * @return initialized [DiktatRunnerFactory]
 */
val diktatRunnerFactory: DiktatRunnerFactory = DiktatRunnerFactory(
    diktatRuleConfigReader,
    diktatRuleSetFactory,
    diktatProcessorFactory,
    diktatBaselineFactory,
    diktatReporterFactory,
)
