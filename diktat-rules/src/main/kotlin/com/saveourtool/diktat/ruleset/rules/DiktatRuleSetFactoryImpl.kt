package com.saveourtool.diktat.ruleset.rules

import com.saveourtool.diktat.api.DiktatRuleSet
import com.saveourtool.diktat.api.DiktatRuleSetFactory

/**
 * A default implementation of [DiktatRuleSetFactory]
 */
class DiktatRuleSetFactoryImpl : DiktatRuleSetFactory {
    override fun invoke(): DiktatRuleSet = DiktatRuleSetProvider().invoke()

    override fun create(configFile: String): DiktatRuleSet = DiktatRuleSetProvider(configFile).invoke()
}
