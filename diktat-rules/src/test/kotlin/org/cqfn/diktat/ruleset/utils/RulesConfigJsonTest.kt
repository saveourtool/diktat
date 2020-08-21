package org.cqfn.diktat.ruleset.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.junit.Test

/**
 * Special test that checks that developer has not forgotten to add his warning to a rules-config.json
 * This file is needed to be in tact with latest changes in Warnings.kt
 */
class RulesConfigJsonTest {
    @Test
    fun `read rules config json`() {
        val allRulesFromConfig = readAllRulesFromConfig()
        val allRulesFromCode = readAllRulesFromCode()

        allRulesFromCode.forEach { rule ->
            val foundRule = allRulesFromConfig.getRuleConfig(rule)
            val jsonCodeSnippet = RulesConfig(rule.ruleName(), true, mapOf())
            val jacksonMapper = jacksonObjectMapper()

            val ruleJson = jacksonMapper.writeValueAsString(jsonCodeSnippet)
            require(foundRule != null) {
                """
                   Cannot find warning ${rule.ruleName()} in rules-config.json.
                   You can fix it by adding the following code below to rules-config.json:
                   $ruleJson
                """
            }
        }
    }

    private fun readAllRulesFromConfig() =
            RulesConfigReader(javaClass.classLoader)
                    .readResource("rules-config.json") ?: listOf()

    private fun readAllRulesFromCode() =
            Warnings.values()
}
