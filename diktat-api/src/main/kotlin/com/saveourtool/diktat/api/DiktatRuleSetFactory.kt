package com.saveourtool.diktat.api

/**
 * A factory which creates a [DiktatRuleSet].
 */
fun interface DiktatRuleSetFactory : Function1<List<DiktatRuleConfig>, DiktatRuleSet> {
    /**
     * @param rulesConfig all configurations for rules
     * @return the default instance of [DiktatRuleSet]
     */
    override operator fun invoke(rulesConfig: List<DiktatRuleConfig>): DiktatRuleSet
}
