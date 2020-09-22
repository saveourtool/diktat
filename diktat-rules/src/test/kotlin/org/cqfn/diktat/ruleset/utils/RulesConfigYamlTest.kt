package org.cqfn.diktat.ruleset.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Special test that checks that developer has not forgotten to add his warning to a diktat-analysis.yml
 * This file is needed to be in tact with latest changes in Warnings.kt
 */
class RulesConfigYamlTest {
    @Test
    fun `read rules config yml`() {
        compareRulesAndConfig("diktat-analysis.yml")
        compareRulesAndConfig("diktat-analysis-huawei.yml")
        val thirdConfig = "${System.getProperty("user.dir")}${File.separator}..${File.separator}diktat-analysis.yml${File.separator}"
        compareRulesAndConfig(thirdConfig, "parent/diktat-analysis.yml")
    }

    private fun compareRulesAndConfig(nameConfig: String, nameConfigToText: String? = null) {
        val allRulesFromConfig = readAllRulesFromConfig(nameConfig)
        val allRulesFromCode = readAllRulesFromCode()

        allRulesFromCode.forEach { rule ->
            val foundRule = allRulesFromConfig.getRuleConfig(rule)
            val ymlCodeSnippet = RulesConfig(rule.ruleName(), true, mapOf())
            val jacksonMapper = jacksonObjectMapper()

            val ruleYaml = jacksonMapper.writeValueAsString(ymlCodeSnippet)
            Assertions.assertTrue(foundRule != null) {
                """
                   Cannot find warning ${rule.ruleName()} in ${nameConfigToText ?: nameConfig}.
                   You can fix it by adding the following code below to ${nameConfigToText ?: nameConfig}:
                   $ruleYaml
                """
            }
        }

        allRulesFromConfig.forEach { warning ->
            val warningName = warning.name
            val ruleFound = allRulesFromCode.find { it.ruleName() == warningName || warningName == "DIKTAT_COMMON" } != null
            Assertions.assertTrue(ruleFound) {
                """
                    Found rule (warning) in ${nameConfigToText ?: nameConfig}: <$warningName> that does not exist in the code. Misprint or configuration was renamed? 
                """.trimIndent()
            }
        }
    }

    private fun readAllRulesFromConfig(nameConfig: String) =
            RulesConfigReader(javaClass.classLoader)
                    .readResource(nameConfig) ?: listOf()

    private fun readAllRulesFromCode() =
            Warnings.values()
}
