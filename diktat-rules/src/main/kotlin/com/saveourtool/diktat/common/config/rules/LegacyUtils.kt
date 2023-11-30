/**
 * This file contains aliases to support old names and util methods
 */

package com.saveourtool.diktat.common.config.rules

import com.saveourtool.diktat.api.DiktatRuleConfig
import com.saveourtool.diktat.api.DiktatRuleNameAware
import com.saveourtool.diktat.api.findByRuleName

typealias RuleConfiguration = com.saveourtool.diktat.ruleset.config.RuleConfiguration

/**
 * Get [DiktatRuleConfig] for particular [DiktatRuleNameAware] object.
 *
 * @param rule a [DiktatRuleNameAware] which configuration will be returned
 * @return [DiktatRuleConfig] for a particular rule if it is found, else null
 */
fun List<DiktatRuleConfig>.getRuleConfig(rule: DiktatRuleNameAware): DiktatRuleConfig? = this.findByRuleName(rule)
