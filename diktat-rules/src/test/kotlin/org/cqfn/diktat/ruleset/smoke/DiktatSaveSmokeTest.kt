package org.cqfn.diktat.ruleset.smoke

import org.cqfn.diktat.common.config.rules.DIKTAT_COMMON
import org.cqfn.diktat.ruleset.constants.Warnings

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

import java.time.LocalDate

class DiktatSaveSmokeTest : DiktatSmokeTestBase() {
    @BeforeEach
    fun setUp() {
        unfixedLintErrors.clear()
    }

    @AfterEach
    fun tearDown() {
        configFilePath = DEFAULT_CONFIG_PATH
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    override fun `regression - should not fail if package is not set`() {
        overrideRulesConfig(listOf(Warnings.PACKAGE_NAME_MISSING, Warnings.PACKAGE_NAME_INCORRECT_PATH,
            Warnings.PACKAGE_NAME_INCORRECT_PREFIX))
        saveSmokeTest(configFilePath, "DefaultPackageExpected.kt", "DefaultPackageTest.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    override fun `smoke test #8 - anonymous function`() {
        saveSmokeTest(configFilePath, "Example8Expected.kt", "Example8Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    override fun `smoke test #7`() {
        saveSmokeTest(configFilePath, "Example7Expected.kt", "Example7Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    override fun `smoke test #6`() {
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
    override fun `smoke test #5`() {
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
    override fun `smoke test #4`() {
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
    override fun `smoke test #3`() {
        saveSmokeTest(configFilePath, "Example3Expected.kt", "Example3Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    override fun `regression - shouldn't throw exception in cases similar to #371`() {
        saveSmokeTest(configFilePath, "Bug1Expected.kt", "Bug1Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    override fun `smoke test #2`() {
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
    override fun `smoke test #1`() {
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
    override fun `smoke test with kts files #2`() {
        saveSmokeTest(configFilePath, "script/SimpleRunInScriptExpected.kts", "script/SimpleRunInScriptTest.kts")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    override fun `smoke test with kts files with package name`() {
        saveSmokeTest(configFilePath, "script/PackageInScriptExpected.kts", "script/PackageInScriptTest.kts")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    override fun `regression - should correctly handle tags with empty lines`() {
        saveSmokeTest(configFilePath, "KdocFormattingMultilineTagsExpected.kt", "KdocFormattingMultilineTagsTest.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    override fun `regression - FP of local variables rule`() {
        saveSmokeTest(configFilePath, "LocalVariableWithOffsetExpected.kt", "LocalVariableWithOffsetTest.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    override fun `fix can cause long line`() {
        saveSmokeTest(configFilePath, "ManyLineTransformInLongLineExpected.kt", "ManyLineTransformInLongLineTest.kt")
    }
}
