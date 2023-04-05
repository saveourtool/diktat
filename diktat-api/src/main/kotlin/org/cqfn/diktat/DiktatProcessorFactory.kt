package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatRuleSet

/**
 * A factory to create [DiktatProcessor] using [DiktatRuleSet]
 */
@FunctionalInterface
interface DiktatProcessorFactory : Function1<DiktatRuleSet, DiktatProcessor> {
    /**
     * @param diktatRuleSet
     * @return created [DiktatProcessor] using [DiktatRuleSet]
     */
    override operator fun invoke(diktatRuleSet: DiktatRuleSet): DiktatProcessor
}
