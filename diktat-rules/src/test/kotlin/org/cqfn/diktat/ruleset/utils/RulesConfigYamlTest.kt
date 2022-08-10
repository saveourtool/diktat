package org.cqfn.diktat.ruleset.utils

import org.cqfn.diktat.common.config.rules.DIKTAT_COMMON
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.common.config.rules.kotlinVersion
import org.cqfn.diktat.ruleset.constants.Warnings

import com.charleskorn.kaml.Yaml
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

import java.io.File

import kotlinx.serialization.encodeToString

/**
 * Special test that checks that developer has not forgotten to add his warning to a diktat-analysis.yml
 * This file is needed to be in tact with latest changes in Warnings.kt
 */
@Suppress("UNUSED")
class RulesConfigYamlTest {
    private val parentDiktatAnalysis = "${System.getProperty("user.dir")}${File.separator}..${File.separator}diktat-analysis.yml${File.separator}"
    private val pathMap: Map<String, String> =
        mapOf("diktat-analysis.yml" to "diKTat/diktat-rules/src/main/resources/diktat-analysis.yml",
            "diktat-analysis-huawei.yml" to "diKTat/diktat-rules/src/main/resources/diktat-analysis-huawei.yml",
            parentDiktatAnalysis to "diKTat/diktat-analysis.yml")

    @Test
    fun `read rules config yml`() {
        compareRulesAndConfig("diktat-analysis.yml")
        compareRulesAndConfig("diktat-analysis-huawei.yml")
        compareRulesAndConfig(parentDiktatAnalysis, "diKTat/diktat-analysis.yml")
    }

    @Test
    fun `check comments before rules`() {
        checkComments("src/main/resources/diktat-analysis.yml")
        checkComments("src/main/resources/diktat-analysis-huawei.yml")
        checkComments("../diktat-analysis.yml")
    }

    @Test
    @Suppress("UNUSED")
    fun `check kotlin version`() {
        val currentKotlinVersion = "${KotlinVersion.CURRENT.major}.${KotlinVersion.CURRENT.minor}"
        pathMap.keys.forEach { path ->
            val config = readAllRulesFromConfig(path)
            val ktVersion = config.find { it.name == DIKTAT_COMMON }
                ?.configuration
                ?.get("kotlinVersion")
                ?.kotlinVersion()
            val ktVersionNew = "${ktVersion?.major}.${ktVersion?.minor}"
            Assertions.assertEquals(ktVersionNew, currentKotlinVersion)
        }
    }

    private fun checkComments(configName: String) {
        val lines = File(configName)
            .readLines()
            .filter {
                it.startsWith("-") || it.startsWith("#")
            }

        lines.forEachIndexed { index, str ->
            if (str.startsWith("-")) {
                Assertions.assertTrue(lines[if (index > 0) index - 1 else 0].trim().startsWith("#")) {
                    """
                        There is no comment before $str in $configName
                    """.trimIndent()
                }
            }
        }
    }

    private fun compareRulesAndConfig(nameConfig: String, nameConfigToText: String? = null) {
        val filePath = nameConfigToText?.let { pathMap[it] } ?: pathMap[nameConfig]
        val allRulesFromConfig = readAllRulesFromConfig(nameConfig)
        val allRulesFromCode = readAllRulesFromCode()

        allRulesFromCode.forEach { rule ->
            if (rule == Warnings.DUMMY_TEST_WARNING) {
                return@forEach
            }
            val foundRule = allRulesFromConfig.getRuleConfig(rule)
            val ymlCodeSnippet = RulesConfig(rule.ruleName(), true, emptyMap())

            val ruleYaml = Yaml.default.encodeToString(ymlCodeSnippet)
            Assertions.assertTrue(foundRule != null) {
                """
                   Cannot find warning ${rule.ruleName()} in $filePath.
                   You can fix it by adding the following code below to $filePath:
                   $ruleYaml
                """.trimIndent()
            }
        }

        allRulesFromConfig.forEach { warning ->
            val warningName = warning.name
            val ruleFound = allRulesFromCode.any { it.ruleName() == warningName || warningName == "DIKTAT_COMMON" }
            Assertions.assertTrue(ruleFound) {
                """
                    Found rule (warning) in $filePath: <$warningName> that does not exist in the code. Misprint or configuration was renamed?
                """.trimIndent()
            }
        }
    }

    private fun readAllRulesFromConfig(nameConfig: String) =
        RulesConfigReader(javaClass.classLoader)
            .readResource(nameConfig) ?: emptyList()

    private fun readAllRulesFromCode() =
        Warnings.values()
}
