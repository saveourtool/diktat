/**
 * Contains typealias for legacy support
 */

package com.saveourtool.diktat.common.config.rules

import com.saveourtool.diktat.api.DiktatRuleConfig
import com.saveourtool.diktat.api.DiktatRuleNameAware

const val DIKTAT_CONF_PROPERTY = "diktat.config.path"

/**
 * this constant will be used everywhere in the code to mark usage of Diktat ruleset
 *
 * Should be removed from Diktat's code and should be presented only in `diktat-ruleset`
 */
const val DIKTAT_RULE_SET_ID = "diktat-ruleset"

typealias RulesConfig = DiktatRuleConfig
typealias Rule = DiktatRuleNameAware
