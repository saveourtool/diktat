package org.cqfn.diktat.ruleset.smoke

import org.cqfn.diktat.common.config.rules.DIKTAT_COMMON
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import org.cqfn.diktat.util.FixTestBase

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.pinterest.ktlint.core.LintError
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

import java.time.LocalDate

import kotlin.io.path.createTempFile
import kotlinx.serialization.builtins.ListSerializer

class DiktatSaveSmokeTest : FixTestBase("test/smoke/src/main/kotlin",
    { DiktatRuleSetProvider(configFilePath) },
    { lintError, _ -> unfixedLintErrors.add(lintError) },
) {
    /**
     * Disable some of the rules.
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun overrideRulesConfig(rulesToDisable: List<Warnings>, rulesToOverride: RuleToConfig = emptyMap()) {
        val rulesConfig = RulesConfigReader(javaClass.classLoader).readResource(DEFAULT_CONFIG_PATH)!!
            .toMutableList()
            .also { rulesConfig ->
                rulesToDisable.forEach { warning ->
                    rulesConfig.removeIf { it.name == warning.name }
                    rulesConfig.add(RulesConfig(warning.name, enabled = false, configuration = emptyMap()))
                }
                rulesToOverride.forEach { (warning, configuration) ->
                    rulesConfig.removeIf { it.name == warning }
                    rulesConfig.add(RulesConfig(warning, enabled = true, configuration = configuration))
                }
            }
            .toList()
        createTempFile().toFile()
            .also {
                configFilePath = it.absolutePath
            }
            .writeText(
                Yaml(configuration = YamlConfiguration(strictMode = true))
                    .encodeToString(ListSerializer(RulesConfig.serializer()), rulesConfig)
            )
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `save smoke test #1`() {
        saveSmokeTest(DEFAULT_CONFIG_PATH, "Bug1Expected.kt", "Bug1Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `save smoke test #2`() {
        saveSmokeTest(DEFAULT_CONFIG_PATH, "DefaultPackageExpected.kt", "DefaultPackageTest.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `save smoke test #3`() {
        overrideRulesConfig(
            rulesToDisable = emptyList(),
            rulesToOverride = mapOf(
                Warnings.WRONG_INDENTATION.name to mapOf(
                    "extendedIndentAfterOperators" to "true",
                    "extendedIndentBeforeDot" to "false",
                )
            )
        )
        saveSmokeTest(configFilePath, "Example1Expected.kt", "Example1Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `save smoke test #4`() {
        overrideRulesConfig(
            rulesToDisable = emptyList(),
            rulesToOverride = mapOf(
                Warnings.WRONG_INDENTATION.name to mapOf(
                    "extendedIndentAfterOperators" to "true",
                    "extendedIndentBeforeDot" to "true",
                )
            )
        )
        saveSmokeTest(configFilePath, "Example2Expected.kt", "Example2Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `save smoke test #5`() {
        saveSmokeTest(DEFAULT_CONFIG_PATH, "Example3Expected.kt", "Example3Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `save smoke test #6`() {
        overrideRulesConfig(
            rulesToDisable = emptyList(),
            rulesToOverride = mapOf(
                Warnings.WRONG_INDENTATION.name to mapOf(
                    "extendedIndentAfterOperators" to "true",
                    "extendedIndentBeforeDot" to "false",
                )
            )
        )
        saveSmokeTest(configFilePath, "Example4Expected.kt", "Example4Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `save smoke test #7`() {
        overrideRulesConfig(emptyList(),
            mapOf(
                Warnings.HEADER_MISSING_OR_WRONG_COPYRIGHT.name to mapOf(
                    "isCopyrightMandatory" to "true",
                    "copyrightText" to """|Copyright 2018-${LocalDate.now().year} John Doe.
                                    |    Licensed under the Apache License, Version 2.0 (the "License");
                                    |    you may not use this file except in compliance with the License.
                                    |    You may obtain a copy of the License at
                                    |
                                    |        http://www.apache.org/licenses/LICENSE-2.0
                                """.trimMargin()
                ),
                DIKTAT_COMMON to mapOf(
                    "domainName" to "org.cqfn.diktat",
                    "kotlinVersion" to "1.3.7"
                )
            )
        )
        saveSmokeTest(configFilePath, "Example5Expected.kt", "Example5Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `save smoke test #8`() {
        overrideRulesConfig(
            rulesToDisable = emptyList(),
            rulesToOverride = mapOf(
                Warnings.WRONG_INDENTATION.name to mapOf(
                    "extendedIndentAfterOperators" to "true",
                    "extendedIndentBeforeDot" to "true",
                )
            )
        )
        saveSmokeTest(configFilePath, "Example6Expected.kt", "Example6Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `save smoke test #9`() {
        saveSmokeTest(DEFAULT_CONFIG_PATH, "Example7Expected.kt", "Example7Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `save smoke test #10`() {
        saveSmokeTest(DEFAULT_CONFIG_PATH, "Example8Expected.kt", "Example8Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `save smoke test #11`() {
        saveSmokeTest(DEFAULT_CONFIG_PATH, "KdocFormattingMultilineTagsExpected.kt", "KdocFormattingMultilineTagsTest.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `save smoke test #12`() {
        saveSmokeTest(DEFAULT_CONFIG_PATH, "ManyLineTransformInLongLineExpected.kt", "ManyLineTransformInLongLineTest.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `save smoke test #13`() {
        saveSmokeTest(DEFAULT_CONFIG_PATH, "LocalVariableWithOffsetExpected.kt", "LocalVariableWithOffsetTest.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `save smoke test with kts files #2`() {
        saveSmokeTest(DEFAULT_CONFIG_PATH, "script/SimpleRunInScriptExpected.kts", "script/SimpleRunInScriptTest.kts")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `save smoke test with kts files with package name`() {
        saveSmokeTest(DEFAULT_CONFIG_PATH, "script/PackageInScriptExpected.kts", "script/PackageInScriptTest.kts")
    }

    companion object {
        private const val DEFAULT_CONFIG_PATH = "../diktat-analysis.yml"
        private val unfixedLintErrors: MutableList<LintError> = mutableListOf()

        // by default using same yml config as for diktat code style check, but allow to override
        private var configFilePath = DEFAULT_CONFIG_PATH
    }
}
