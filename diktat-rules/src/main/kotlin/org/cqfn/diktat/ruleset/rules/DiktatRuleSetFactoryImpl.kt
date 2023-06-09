package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.api.DiktatRuleSet
import org.cqfn.diktat.api.DiktatRuleSetFactory

/**
 * A default implementation of [DiktatRuleSetFactory]
 */
class DiktatRuleSetFactoryImpl : DiktatRuleSetFactory {
    override fun create(configFile: String): DiktatRuleSet = DiktatRuleSetProvider(configFile).invoke()

    override fun create(configFile: String): DiktatRuleSet = DiktatRuleSetProvider(configFile).invoke()
}
