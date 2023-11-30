/**
 * Contains typealias for legacy support
 */
package com.saveourtool.diktat.common.config.rules

import com.saveourtool.diktat.api.DiktatRuleConfig
import com.saveourtool.diktat.api.DiktatRuleNameAware

typealias RulesConfig = DiktatRuleConfig
typealias Rule = DiktatRuleNameAware

const val DIKTAT_CONF_PROPERTY = "diktat.config.path"
/**
 * this constant will be used everywhere in the code to mark usage of Diktat ruleset
 */
const val DIKTAT_RULE_SET_ID = "diktat-ruleset"
