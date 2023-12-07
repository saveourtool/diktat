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

/**
 * Finds [DiktatRuleConfig] for particular [DiktatRuleNameAware] object.
 *
 * @param rule a [DiktatRuleNameAware] which configuration will be returned
 * @return [DiktatRuleConfig] for a particular rule if it is found, else null
 */
fun List<DiktatRuleConfig>.findByRuleName(rule: DiktatRuleNameAware): DiktatRuleConfig? = this.find { it.name == rule.ruleName() }

/**
 * checking if in yml config particular rule is enabled or disabled
 * (!) the default value is "true" (in case there is no config specified)
 *
 * @param rule a [DiktatRuleNameAware] which is being checked
 * @return true if rule is enabled in configuration, else false
 */
fun List<DiktatRuleConfig>.isRuleEnabled(rule: DiktatRuleNameAware): Boolean {
    val ruleMatched = findByRuleName(rule)
    return ruleMatched?.enabled ?: true
}

/**
 * @param rule diktat inspection
 * @param annotations set of annotations that are annotating a block of code
 * @return true if the code block is marked with annotation that is in `ignored list` in the rule
 */
fun List<DiktatRuleConfig>.isAnnotatedWithIgnoredAnnotation(rule: DiktatRuleNameAware, annotations: Set<String>): Boolean =
    findByRuleName(rule)
        ?.ignoreAnnotated
        ?.map { it.trim() }
        ?.map { it.trim('"') }
        ?.intersect(annotations)
        ?.isNotEmpty()
        ?: false
