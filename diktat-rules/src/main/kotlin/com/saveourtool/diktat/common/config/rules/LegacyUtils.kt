/**
 * This file contains aliases to support old names and util methods
 */

package com.saveourtool.diktat.common.config.rules

import com.saveourtool.diktat.api.DiktatRuleConfig
import com.saveourtool.diktat.api.DiktatRuleNameAware

typealias RuleConfiguration = com.saveourtool.diktat.ruleset.config.RuleConfiguration



/**
 * Get [DiktatRuleConfig] for particular [DiktatRuleNameAware] object.
 *
 * @param rule a [DiktatRuleNameAware] which configuration will be returned
 * @return [DiktatRuleConfig] for a particular rule if it is found, else null
 */
fun List<DiktatRuleConfig>.getRuleConfig(rule: DiktatRuleNameAware): RulesConfig? = this.find { it.name == rule.ruleName() }

/**
 * checking if in yml config particular rule is enabled or disabled
 * (!) the default value is "true" (in case there is no config specified)
 *
 * @param rule a [DiktatRuleNameAware] which is being checked
 * @return true if rule is enabled in configuration, else false
 */
fun List<DiktatRuleConfig>.isRuleEnabled(rule: DiktatRuleNameAware): Boolean {
    val ruleMatched = getRuleConfig(rule)
    return ruleMatched?.enabled ?: true
}

/**
 * @param rule diktat inspection
 * @param annotations set of annotations that are annotating a block of code
 * @return true if the code block is marked with annotation that is in `ignored list` in the rule
 */
fun List<DiktatRuleConfig>.isAnnotatedWithIgnoredAnnotation(rule: DiktatRuleNameAware, annotations: Set<String>): Boolean =
    getRuleConfig(rule)
        ?.ignoreAnnotated
        ?.map { it.trim() }
        ?.map { it.trim('"') }
        ?.intersect(annotations)
        ?.isNotEmpty()
        ?: false
