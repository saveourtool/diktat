@file:Suppress(
    "MISSING_KDOC_CLASS_ELEMENTS",
    "MISSING_KDOC_ON_FUNCTION",
    "BACKTICKS_PROHIBITED",
)

package com.saveourtool.diktat.ruleset.smoke

import com.saveourtool.diktat.api.DiktatError
import com.saveourtool.diktat.common.config.rules.DIKTAT_COMMON
import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.RulesConfigReader
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.constants.Warnings.EMPTY_BLOCK_STRUCTURE_ERROR
import com.saveourtool.diktat.ruleset.constants.Warnings.FILE_NAME_MATCH_CLASS
import com.saveourtool.diktat.ruleset.constants.Warnings.HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_NO_EMPTY_TAGS
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_WITHOUT_PARAM_TAG
import com.saveourtool.diktat.ruleset.constants.Warnings.MISSING_KDOC_CLASS_ELEMENTS
import com.saveourtool.diktat.ruleset.constants.Warnings.MISSING_KDOC_ON_FUNCTION
import com.saveourtool.diktat.ruleset.constants.Warnings.MISSING_KDOC_TOP_LEVEL
import com.saveourtool.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import com.saveourtool.diktat.ruleset.rules.chapter1.FileNaming
import com.saveourtool.diktat.ruleset.rules.chapter2.comments.CommentsRule
import com.saveourtool.diktat.ruleset.rules.chapter2.comments.HeaderCommentRule
import com.saveourtool.diktat.ruleset.rules.chapter2.kdoc.KdocComments
import com.saveourtool.diktat.ruleset.rules.chapter2.kdoc.KdocFormatting
import com.saveourtool.diktat.ruleset.rules.chapter2.kdoc.KdocMethods
import com.saveourtool.diktat.ruleset.rules.chapter3.EmptyBlock
import com.saveourtool.diktat.ruleset.rules.chapter6.classes.InlineClassesRule
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_AFTER_OPERATORS
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_BEFORE_DOT
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_FOR_EXPRESSION_BODIES
import com.saveourtool.diktat.test.framework.util.deleteIfExistsSilently

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

import java.time.LocalDate
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.io.path.createTempFile
import kotlin.io.path.inputStream
import kotlin.io.path.writeText

import kotlinx.serialization.builtins.ListSerializer

typealias RuleToConfig = Map<String, Map<String, String>>

/**
 * Base class for smoke test classes
 */
abstract class DiktatSmokeTestBase {
    /**
     * Disable some of the rules.
     *
     * @param rulesToDisable
     * @param rulesToOverride
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun prepareOverriddenRulesConfig(rulesToDisable: List<Warnings> = emptyList(), rulesToOverride: RuleToConfig = emptyMap()): Path {
        val rulesConfig = RulesConfigReader().read(Paths.get(DEFAULT_CONFIG_PATH).inputStream())!!
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
        return createTempFile()
            .also {
                it.writeText(
                    Yaml(configuration = YamlConfiguration(strictMode = true))
                        .encodeToString(ListSerializer(RulesConfig.serializer()), rulesConfig)
                )
            }
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    @Timeout(TEST_TIMEOUT_SECONDS, unit = SECONDS)
    fun `regression - should not fail if package is not set`() {
        val configFilePath = prepareOverriddenRulesConfig(listOf(Warnings.PACKAGE_NAME_MISSING, Warnings.PACKAGE_NAME_INCORRECT_PATH,
            Warnings.PACKAGE_NAME_INCORRECT_PREFIX))
        fixAndCompare(configFilePath, "DefaultPackageExpected.kt", "DefaultPackageTest.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    @Timeout(TEST_TIMEOUT_SECONDS, unit = SECONDS)
    fun `smoke test #8 - anonymous function`() {
        fixAndCompare(prepareOverriddenRulesConfig(), "Example8Expected.kt", "Example8Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    @Timeout(TEST_TIMEOUT_SECONDS, unit = SECONDS)
    fun `smoke test #7`() {
        fixAndCompare(prepareOverriddenRulesConfig(), "Example7Expected.kt", "Example7Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    @Timeout(TEST_TIMEOUT_SECONDS, unit = SECONDS)
    fun `smoke test #6`() {
        val configFilePath = prepareOverriddenRulesConfig(
            rulesToDisable = emptyList(),
            rulesToOverride = mapOf(
                WRONG_INDENTATION.name to mapOf(
                    EXTENDED_INDENT_FOR_EXPRESSION_BODIES to "true",
                    EXTENDED_INDENT_AFTER_OPERATORS to "true",
                    EXTENDED_INDENT_BEFORE_DOT to "true",
                )
            )
        )
        fixAndCompare(configFilePath, "Example6Expected.kt", "Example6Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    @Timeout(TEST_TIMEOUT_SECONDS, unit = SECONDS)
    fun `smoke test #5`() {
        val configFilePath = prepareOverriddenRulesConfig(emptyList(),
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
                    "domainName" to "com.saveourtool.diktat",
                    "kotlinVersion" to "1.3.7"
                )
            )
        )
        fixAndCompare(configFilePath, "Example5Expected.kt", "Example5Test.kt")

        assertUnfixedLintErrors { unfixedLintErrors ->
            Assertions.assertFalse(
                unfixedLintErrors.contains(
                    DiktatError(
                        line = 1,
                        col = 1,
                        ruleId = "diktat-ruleset:${CommentsRule.NAME_ID}",
                        detail = "${Warnings.COMMENTED_OUT_CODE.warnText()} /*"
                    )
                )
            )

            Assertions.assertTrue(
                unfixedLintErrors.contains(DiktatError(1, 1, "diktat-ruleset:${InlineClassesRule.NAME_ID}", "${Warnings.INLINE_CLASS_CAN_BE_USED.warnText()} class Some"))
            )
        }
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    @Timeout(TEST_TIMEOUT_SECONDS, unit = SECONDS)
    fun `smoke test #4`() {
        fixAndCompare(prepareOverriddenRulesConfig(), "Example4Expected.kt", "Example4Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    @Timeout(TEST_TIMEOUT_SECONDS, unit = SECONDS)
    fun `smoke test #3`() {
        fixAndCompare(prepareOverriddenRulesConfig(), "Example3Expected.kt", "Example3Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    @Timeout(TEST_TIMEOUT_SECONDS, unit = SECONDS)
    fun `regression - shouldn't throw exception in cases similar to #371`() {
        fixAndCompare(prepareOverriddenRulesConfig(), "Bug1Expected.kt", "Bug1Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    @Timeout(TEST_TIMEOUT_SECONDS, unit = SECONDS)
    fun `smoke test #2`() {
        val configFilePath = prepareOverriddenRulesConfig(
            rulesToDisable = emptyList(),
            rulesToOverride = mapOf(
                WRONG_INDENTATION.name to mapOf(
                    EXTENDED_INDENT_AFTER_OPERATORS to "true",
                    EXTENDED_INDENT_BEFORE_DOT to "true",
                )
            )
        )
        fixAndCompare(configFilePath, "Example2Expected.kt", "Example2Test.kt")
        assertUnfixedLintErrors { unfixedLintErrors ->
            assertThat(unfixedLintErrors).containsExactlyInAnyOrder(
                DiktatError(1, 1, "$DIKTAT_RULE_SET_ID:${HeaderCommentRule.NAME_ID}",
                    "${HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE.warnText()} there are 2 declared classes and/or objects", false),
                DiktatError(15, 30, "$DIKTAT_RULE_SET_ID:${KdocMethods.NAME_ID}",
                    "${KDOC_WITHOUT_PARAM_TAG.warnText()} createWithFile (containerName)", true),
                DiktatError(31, 21, "$DIKTAT_RULE_SET_ID:${EmptyBlock.NAME_ID}",
                    "${EMPTY_BLOCK_STRUCTURE_ERROR.warnText()} empty blocks are forbidden unless it is function with override keyword", false)
            )
        }
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    @Timeout(TEST_TIMEOUT_SECONDS, unit = SECONDS)
    fun `smoke test #1`() {
        val configFilePath = prepareOverriddenRulesConfig(
            rulesToDisable = emptyList(),
            rulesToOverride = mapOf(
                WRONG_INDENTATION.name to mapOf(
                    EXTENDED_INDENT_AFTER_OPERATORS to "true",
                    EXTENDED_INDENT_FOR_EXPRESSION_BODIES to "true",
                )
            )
        )
        fixAndCompare(configFilePath, "Example1Expected.kt", "Example1Test.kt")
        assertUnfixedLintErrors { unfixedLintErrors ->
            assertThat(unfixedLintErrors).containsExactlyInAnyOrder(
                DiktatError(1, 1, "$DIKTAT_RULE_SET_ID:${FileNaming.NAME_ID}", "${FILE_NAME_MATCH_CLASS.warnText()} Example1Test.kt vs Example", true),
                DiktatError(1, 1, "$DIKTAT_RULE_SET_ID:${KdocFormatting.NAME_ID}", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false),
                DiktatError(3, 13, "$DIKTAT_RULE_SET_ID:${KdocComments.NAME_ID}", "${MISSING_KDOC_TOP_LEVEL.warnText()} Example", false),
                DiktatError(3, 33, "$DIKTAT_RULE_SET_ID:${KdocComments.NAME_ID}", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} isValid", false),
                DiktatError(6, 16, "$DIKTAT_RULE_SET_ID:${KdocComments.NAME_ID}", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} foo", false),
                DiktatError(8, 15, "$DIKTAT_RULE_SET_ID:${KdocComments.NAME_ID}", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} foo", false),
                DiktatError(8, 15, "$DIKTAT_RULE_SET_ID:${KdocMethods.NAME_ID}", "${MISSING_KDOC_ON_FUNCTION.warnText()} foo", false),
                /*
                 * This 2nd `MISSING_KDOC_ON_FUNCTION` is a duplicate caused by
                 * https://github.com/saveourtool/diktat/issues/1538.
                 */
                DiktatError(6, 5, "$DIKTAT_RULE_SET_ID:${KdocMethods.NAME_ID}", "${MISSING_KDOC_ON_FUNCTION.warnText()} foo", false),
                DiktatError(9, 10, "$DIKTAT_RULE_SET_ID:${EmptyBlock.NAME_ID}", EMPTY_BLOCK_STRUCTURE_ERROR.warnText() +
                        " empty blocks are forbidden unless it is function with override keyword", false),
                DiktatError(12, 17, "$DIKTAT_RULE_SET_ID:${KdocFormatting.NAME_ID}", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false),
                DiktatError(14, 15, "$DIKTAT_RULE_SET_ID:${KdocFormatting.NAME_ID}", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false),
                DiktatError(19, 27, "$DIKTAT_RULE_SET_ID:${KdocFormatting.NAME_ID}", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false)
            )
        }
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    @Timeout(TEST_TIMEOUT_SECONDS, unit = SECONDS)
    fun `smoke test with kts files #2`() {
        fixAndCompare(prepareOverriddenRulesConfig(), "script/SimpleRunInScriptExpected.kts", "script/SimpleRunInScriptTest.kts")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    @Timeout(TEST_TIMEOUT_SECONDS, unit = SECONDS)
    fun `smoke test with kts files with package name`() {
        fixAndCompare(prepareOverriddenRulesConfig(), "script/PackageInScriptExpected.kts", "script/PackageInScriptTest.kts")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    @Timeout(TEST_TIMEOUT_SECONDS, unit = SECONDS)
    fun `regression - should correctly handle tags with empty lines`() {
        fixAndCompare(
            prepareOverriddenRulesConfig(
//                listOf(Warnings.HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE, Warnings.KDOC_WITHOUT_RETURN_TAG),
            ),
            expected = "KdocFormattingMultilineTagsExpected.kt",
            test = "KdocFormattingMultilineTagsTest.kt",
        )
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    @Timeout(TEST_TIMEOUT_SECONDS, unit = SECONDS)
    fun `regression - FP of local variables rule`() {
        fixAndCompare(prepareOverriddenRulesConfig(), "LocalVariableWithOffsetExpected.kt", "LocalVariableWithOffsetTest.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    @Timeout(TEST_TIMEOUT_SECONDS, unit = SECONDS)
    fun `fix can cause long line`() {
        val configFilePath = prepareOverriddenRulesConfig(
            rulesToDisable = emptyList(),
            rulesToOverride = mapOf(
                WRONG_INDENTATION.name to mapOf(
                    EXTENDED_INDENT_AFTER_OPERATORS to "false",
                )
            )
        )
        fixAndCompare(configFilePath, "ManyLineTransformInLongLineExpected.kt", "ManyLineTransformInLongLineTest.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test with multiplatform project layout`() {
        fixAndCompare(
            prepareOverriddenRulesConfig(),
            "../../jsMain/kotlin/com/saveourtool/diktat/scripts/ScriptExpected.kt",
            "../../jsMain/kotlin/com/saveourtool/diktat/scripts/ScriptTest.kt"
        )
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test with kts files`() {
        val configFilePath = prepareOverriddenRulesConfig(
            emptyList(),
            mapOf(
                WRONG_INDENTATION.name to mapOf(
                    IndentationConfig.NEWLINE_AT_END to "false",
                )
            )
        )  // so that trailing newline isn't checked, because it's incorrectly read in tests and we are comparing file with itself
        // file name is `gradle_` so that IDE doesn't suggest to import gradle project
        val tmpFilePath = "../../../build.gradle.kts"
        fixAndCompare(configFilePath, tmpFilePath, tmpFilePath)
        assertUnfixedLintErrors { unfixedLintErrors ->
            assertThat(unfixedLintErrors).isEmpty()
        }
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test with gradle script plugin`() {
        fixAndCompare(prepareOverriddenRulesConfig(), "kotlin-library-expected.gradle.kts", "kotlin-library.gradle.kts")
        assertUnfixedLintErrors { unfixedLintErrors ->
            assertThat(unfixedLintErrors).containsExactly(
                DiktatError(
                    2, 1, "$DIKTAT_RULE_SET_ID:${CommentsRule.NAME_ID}", "[COMMENTED_OUT_CODE] you should not comment out code, " +
                            "use VCS to save it in history and delete this block: import org.jetbrains.kotlin.gradle.dsl.jvm", false
                )
            )
        }
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `disable chapters`() {
        val configFilePath = prepareOverriddenRulesConfig(
            emptyList(),
            mapOf(
                DIKTAT_COMMON to mapOf(
                    "domainName" to "com.saveourtool.diktat",
                    "disabledChapters" to "Naming,3,4,5,Classes"
                )
            )
        )
        fixAndCompare(configFilePath, "Example1-2Expected.kt", "Example1-2Test.kt")
        assertUnfixedLintErrors { unfixedLintErrors ->
            assertThat(unfixedLintErrors).containsExactlyInAnyOrder(
                DiktatError(1, 1, "$DIKTAT_RULE_SET_ID:${KdocFormatting.NAME_ID}", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false),
                DiktatError(3, 1, "$DIKTAT_RULE_SET_ID:${KdocComments.NAME_ID}", "${MISSING_KDOC_TOP_LEVEL.warnText()} example", false),
                DiktatError(3, 16, "$DIKTAT_RULE_SET_ID:${KdocComments.NAME_ID}", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} isValid", false),
                DiktatError(6, 5, "$DIKTAT_RULE_SET_ID:${KdocComments.NAME_ID}", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} foo", false),
                DiktatError(6, 5, "$DIKTAT_RULE_SET_ID:${KdocMethods.NAME_ID}", "${MISSING_KDOC_ON_FUNCTION.warnText()} foo", false),
                DiktatError(8, 5, "$DIKTAT_RULE_SET_ID:${KdocComments.NAME_ID}", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} foo", false),
                DiktatError(10, 4, "$DIKTAT_RULE_SET_ID:${KdocFormatting.NAME_ID}", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false),
                DiktatError(13, 9, "$DIKTAT_RULE_SET_ID:${KdocFormatting.NAME_ID}", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false),
                DiktatError(18, 40, "$DIKTAT_RULE_SET_ID:${KdocFormatting.NAME_ID}", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false)
            )
        }
    }

    abstract fun fixAndCompare(
        config: Path,
        expected: String,
        test: String,
    )

    abstract fun assertUnfixedLintErrors(diktatErrorConsumer: (List<DiktatError>) -> Unit)

    companion object {
        private const val DEFAULT_CONFIG_PATH = "../diktat-analysis.yml"
        const val RESOURCE_FILE_PATH = "test/smoke/src/main/kotlin"
        private const val TEST_TIMEOUT_SECONDS = 30L
        private val tmpFiles: MutableList<File> = mutableListOf()

        @TempDir
        @JvmStatic
        internal var tempDir: Path? = null

        @BeforeAll
        @JvmStatic
        @Suppress("AVOID_NULL_CHECKS")
        internal fun createTmpFiles() {
            listOf(
                "$RESOURCE_FILE_PATH/../../../build.gradle.kts_" to "build.gradle.kts",
                "$RESOURCE_FILE_PATH/Example1Test.kt" to "Example1-2Test.kt",
            )
                .map { (resource, targetFileName) ->
                    DiktatSmokeTestBase::class.java
                        .classLoader
                        .getResource(resource)!!
                        .toURI()
                        .let {
                            val tmpTestFile = File(it).parentFile.resolve(targetFileName)
                            File(it).copyTo(tmpTestFile, true)
                        }
                        .let { tmpFiles.add(it) }
                }
        }

        @AfterAll
        @JvmStatic
        internal fun deleteTmpFiles() {
            tmpFiles.forEach {
                it.toPath().deleteIfExistsSilently()
            }
        }
    }
}
