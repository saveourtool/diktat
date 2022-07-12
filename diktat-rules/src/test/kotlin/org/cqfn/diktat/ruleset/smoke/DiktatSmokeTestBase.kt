@file:Suppress(
    "MISSING_KDOC_CLASS_ELEMENTS",
    "MISSING_KDOC_ON_FUNCTION",
    "BACKTICKS_PROHIBITED",
)

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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlinx.serialization.builtins.ListSerializer

typealias RuleToConfig = Map<String, Map<String, String>>

/**
 * Base class for smoke test classes
 */
abstract class DiktatSmokeTestBase : FixTestBase("test/smoke/src/main/kotlin",
    { DiktatRuleSetProvider(configFilePath) },
    { lintError, _ -> unfixedLintErrors.add(lintError) },
) {
    /**
     * Disable some of the rules.
     *
     * @param rulesToDisable
     * @param rulesToOverride
     */
    @Suppress("UnsafeCallOnNullableType")
    open fun overrideRulesConfig(rulesToDisable: List<Warnings>, rulesToOverride: RuleToConfig = emptyMap()) {
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
        kotlin.io.path.createTempFile()
            .toFile()
            .also {
                configFilePath = it.absolutePath
            }
            .writeText(
                Yaml(configuration = YamlConfiguration(strictMode = true))
                    .encodeToString(ListSerializer(RulesConfig.serializer()), rulesConfig)
            )
    }

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
    fun `regression - should not fail if package is not set`() {
        overrideRulesConfig(listOf(Warnings.PACKAGE_NAME_MISSING, Warnings.PACKAGE_NAME_INCORRECT_PATH,
            Warnings.PACKAGE_NAME_INCORRECT_PREFIX))
        fixAndCompare(configFilePath, "DefaultPackageExpected.kt", "DefaultPackageTest.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test #8 - anonymous function`() {
        fixAndCompare(configFilePath, "Example8Expected.kt", "Example8Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test #7`() {
        fixAndCompare(configFilePath, "Example7Expected.kt", "Example7Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test #6`() {
        overrideRulesConfig(
            rulesToDisable = emptyList(),
            rulesToOverride = mapOf(
                Warnings.WRONG_INDENTATION.name to mapOf(
                    "extendedIndentAfterOperators" to "true",
                    "extendedIndentBeforeDot" to "true",
                )
            )
        )
        fixAndCompare(configFilePath, "Example6Expected.kt", "Example6Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test #5`() {
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
        fixAndCompare(configFilePath, "Example5Expected.kt", "Example5Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test #4`() {
        overrideRulesConfig(
            rulesToDisable = emptyList(),
            rulesToOverride = mapOf(
                Warnings.WRONG_INDENTATION.name to mapOf(
                    "extendedIndentAfterOperators" to "true",
                    "extendedIndentBeforeDot" to "false",
                )
            )
        )
        fixAndCompare(configFilePath, "Example4Expected.kt", "Example4Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test #3`() {
        fixAndCompare(configFilePath, "Example3Expected.kt", "Example3Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `regression - shouldn't throw exception in cases similar to #371`() {
        fixAndCompare(configFilePath, "Bug1Expected.kt", "Bug1Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test #2`() {
        overrideRulesConfig(
            rulesToDisable = emptyList(),
            rulesToOverride = mapOf(
                Warnings.WRONG_INDENTATION.name to mapOf(
                    "extendedIndentAfterOperators" to "true",
                    "extendedIndentBeforeDot" to "true",
                )
            )
        )
        fixAndCompare(configFilePath, "Example2Expected.kt", "Example2Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test #1`() {
        overrideRulesConfig(
            rulesToDisable = emptyList(),
            rulesToOverride = mapOf(
                Warnings.WRONG_INDENTATION.name to mapOf(
                    "extendedIndentAfterOperators" to "true",
                    "extendedIndentBeforeDot" to "false",
                )
            )
        )
        fixAndCompare(configFilePath, "Example1Expected.kt", "Example1Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test with kts files #2`() {
        fixAndCompare(configFilePath, "script/SimpleRunInScriptExpected.kts", "script/SimpleRunInScriptTest.kts")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test with kts files with package name`() {
        fixAndCompare(configFilePath, "script/PackageInScriptExpected.kts", "script/PackageInScriptTest.kts")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `regression - should correctly handle tags with empty lines`() {
        fixAndCompare(configFilePath, "KdocFormattingMultilineTagsExpected.kt", "KdocFormattingMultilineTagsTest.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `regression - FP of local variables rule`() {
        fixAndCompare(configFilePath, "LocalVariableWithOffsetExpected.kt", "LocalVariableWithOffsetTest.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `fix can cause long line`() {
        fixAndCompare(configFilePath, "ManyLineTransformInLongLineExpected.kt", "ManyLineTransformInLongLineTest.kt")
    }

    abstract fun fixAndCompare(
        config: String,
        test: String,
        expected: String
    )

    companion object {
        const val DEFAULT_CONFIG_PATH = "../diktat-analysis.yml"
        val unfixedLintErrors: MutableList<LintError> = mutableListOf()

        // by default using same yml config as for diktat code style check, but allow to override
        var configFilePath = DEFAULT_CONFIG_PATH
    }
}
