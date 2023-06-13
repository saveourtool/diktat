package com.saveourtool.diktat.api

import kotlinx.serialization.Serializable

/**
 * Configuration of individual [DiktatRule]
 *
 * @property name name of the rule
 * @property enabled
 * @property configuration a map of strings with configuration options
 * @property ignoreAnnotated if a code block is marked with these annotations - it will not be checked by this rule
 */
@Serializable
data class DiktatRuleConfig(
    val name: String,
    val enabled: Boolean = true,
    val configuration: Map<String, String> = emptyMap(),
    val ignoreAnnotated: Set<String> = emptySet(),
)
