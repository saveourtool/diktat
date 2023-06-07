package com.saveourtool.diktat.api

/**
 * A group of [DiktatRule]'s as a single set.
 *
 * @property rules diktat rules.
 */
data class DiktatRuleSet(
    val rules: List<DiktatRule>
)
