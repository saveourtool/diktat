/**
 * This file contains aliases to support old names and util methods
 */

package com.saveourtool.diktat.common.config.rules

import com.saveourtool.diktat.api.DiktatRuleConfig
import com.saveourtool.diktat.api.DiktatRuleNameAware
import com.saveourtool.diktat.api.findByRuleName

/**
 * Name of common configuration
 */
const val DIKTAT_COMMON = "DIKTAT_COMMON"

typealias RuleConfiguration = com.saveourtool.diktat.ruleset.config.RuleConfiguration
typealias CommonConfiguration = com.saveourtool.diktat.ruleset.config.CommonConfiguration

/**
 * Get [DiktatRuleConfig] for particular [DiktatRuleNameAware] object.
 *
 * @param rule a [DiktatRuleNameAware] which configuration will be returned
 * @return [DiktatRuleConfig] for a particular rule if it is found, else null
 */
fun List<DiktatRuleConfig>.getRuleConfig(rule: DiktatRuleNameAware): DiktatRuleConfig? = this.findByRuleName(rule)

/**
 * @return common configuration from list of all rules configuration
 */
fun List<DiktatRuleConfig>.getCommonConfiguration(): CommonConfiguration = CommonConfiguration(getCommonConfig()?.configuration)

/**
 * Get [DiktatRuleConfig] representing common configuration part that can be used in any rule
 */
private fun List<DiktatRuleConfig>.getCommonConfig() = find { it.name == DIKTAT_COMMON }
