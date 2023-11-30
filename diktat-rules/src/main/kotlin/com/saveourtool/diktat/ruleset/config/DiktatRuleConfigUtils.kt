/**
 * Util methods for List<RulesConfig>
 */

package com.saveourtool.diktat.ruleset.config

import com.saveourtool.diktat.api.DiktatRuleConfig
import com.saveourtool.diktat.api.DiktatRuleNameAware
import com.saveourtool.diktat.common.config.rules.RulesConfig

/**
 * Name of common configuration
 */
const val DIKTAT_COMMON = "DIKTAT_COMMON"


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

/**
 * Parse string into KotlinVersion
 *
 * @return KotlinVersion from configuration
 */
internal fun String.kotlinVersion(): KotlinVersion {
    require(this.contains("^(\\d+\\.)(\\d+)\\.?(\\d+)?$".toRegex())) {
        "Kotlin version format is incorrect"
    }
    val versions = this.split(".").map { it.toInt() }
    return if (versions.size == 2) {
        KotlinVersion(versions[0], versions[1])
    } else {
        KotlinVersion(versions[0], versions[1], versions[2])
    }
}

