package com.saveourtool.diktat.api

/**
 * This interface represents *name* of individual inspection in rule set.
 */
interface DiktatRuleNameAware {
    /**
     * @return name of this [DiktatRule]
     */
    fun ruleName(): String
}
