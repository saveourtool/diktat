@file:Suppress(
    "MISSING_KDOC_CLASS_ELEMENTS",
    "MISSING_KDOC_ON_FUNCTION",
    "BACKTICKS_PROHIBITED",
)

package org.cqfn.diktat.ruleset.smoke

import org.cqfn.diktat.common.config.rules.DIKTAT_COMMON
import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.constants.Warnings.EMPTY_BLOCK_STRUCTURE_ERROR
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_NAME_MATCH_CLASS
import org.cqfn.diktat.ruleset.constants.Warnings.HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NO_EMPTY_TAGS
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_WITHOUT_PARAM_TAG
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_CLASS_ELEMENTS
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_ON_FUNCTION
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_TOP_LEVEL
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import org.cqfn.diktat.ruleset.rules.chapter1.FileNaming
import org.cqfn.diktat.ruleset.rules.chapter2.comments.CommentsRule
import org.cqfn.diktat.ruleset.rules.chapter2.comments.HeaderCommentRule
import org.cqfn.diktat.ruleset.rules.chapter2.kdoc.KdocComments
import org.cqfn.diktat.ruleset.rules.chapter2.kdoc.KdocFormatting
import org.cqfn.diktat.ruleset.rules.chapter2.kdoc.KdocMethods
import org.cqfn.diktat.ruleset.rules.chapter3.EmptyBlock
import org.cqfn.diktat.ruleset.rules.chapter6.classes.InlineClassesRule
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_AFTER_OPERATORS
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_BEFORE_DOT
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_FOR_EXPRESSION_BODIES
import org.cqfn.diktat.test.framework.util.deleteIfExistsSilently

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.pinterest.ktlint.core.LintError
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.io.File
import java.nio.file.Path

import java.time.LocalDate
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.io.path.createTempFile
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
                    "domainName" to "org.cqfn.diktat",
                    "kotlinVersion" to "1.3.7"
                )
            )
        )
        fixAndCompare(configFilePath, "Example5Expected.kt", "Example5Test.kt")

        assertUnfixedLintErrors { unfixedLintErrors ->
            Assertions.assertFalse(
                unfixedLintErrors.contains(LintError(line = 1, col = 1, ruleId = "diktat-ruleset:${CommentsRule.NAME_ID}", detail = "${Warnings.COMMENTED_OUT_CODE.warnText()} /*"))
            )

            Assertions.assertTrue(
                unfixedLintErrors.contains(LintError(1, 1, "diktat-ruleset:${InlineClassesRule.NAME_ID}", "${Warnings.INLINE_CLASS_CAN_BE_USED.warnText()} class Some"))
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
                LintError(1, 1, "$DIKTAT_RULE_SET_ID:${HeaderCommentRule.NAME_ID}",
                    "${HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE.warnText()} there are 2 declared classes and/or objects", false),
                LintError(15, 23, "$DIKTAT_RULE_SET_ID:${KdocMethods.NAME_ID}",
                    "${KDOC_WITHOUT_PARAM_TAG.warnText()} createWithFile (containerName)", true),
                LintError(31, 14, "$DIKTAT_RULE_SET_ID:${EmptyBlock.NAME_ID}",
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
                LintError(1, 1, "$DIKTAT_RULE_SET_ID:${FileNaming.NAME_ID}", "${FILE_NAME_MATCH_CLASS.warnText()} Example1Test.kt vs Example", true),
                LintError(1, 1, "$DIKTAT_RULE_SET_ID:${KdocFormatting.NAME_ID}", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false),
                LintError(3, 6, "$DIKTAT_RULE_SET_ID:${KdocComments.NAME_ID}", "${MISSING_KDOC_TOP_LEVEL.warnText()} Example", false),
                LintError(3, 26, "$DIKTAT_RULE_SET_ID:${KdocComments.NAME_ID}", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} isValid", false),
                LintError(6, 9, "$DIKTAT_RULE_SET_ID:${KdocComments.NAME_ID}", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} foo", false),
                LintError(8, 8, "$DIKTAT_RULE_SET_ID:${KdocComments.NAME_ID}", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} foo", false),
                LintError(8, 8, "$DIKTAT_RULE_SET_ID:${KdocMethods.NAME_ID}", "${MISSING_KDOC_ON_FUNCTION.warnText()} foo", false),
                /*
                 * This 2nd `MISSING_KDOC_ON_FUNCTION` is a duplicate caused by
                 * https://github.com/saveourtool/diktat/issues/1538.
                 */
                LintError(6, 5, "$DIKTAT_RULE_SET_ID:${KdocMethods.NAME_ID}", "${MISSING_KDOC_ON_FUNCTION.warnText()} foo", false),
                LintError(9, 3, "$DIKTAT_RULE_SET_ID:${EmptyBlock.NAME_ID}", EMPTY_BLOCK_STRUCTURE_ERROR.warnText() +
                        " empty blocks are forbidden unless it is function with override keyword", false),
                LintError(12, 10, "$DIKTAT_RULE_SET_ID:${KdocFormatting.NAME_ID}", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false),
                LintError(14, 8, "$DIKTAT_RULE_SET_ID:${KdocFormatting.NAME_ID}", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false),
                LintError(19, 20, "$DIKTAT_RULE_SET_ID:${KdocFormatting.NAME_ID}", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false)
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
        fixAndCompare(prepareOverriddenRulesConfig(), "KdocFormattingMultilineTagsExpected.kt", "KdocFormattingMultilineTagsTest.kt")
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
            "../../jsMain/kotlin/org/cqfn/diktat/scripts/ScriptExpected.kt",
            "../../jsMain/kotlin/org/cqfn/diktat/scripts/ScriptTest.kt"
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
        fixAndCompare(configFilePath, tmpFilePath, tmpFilePath, false)
        assertUnfixedLintErrors { unfixedLintErrors ->
            Assertions.assertTrue(unfixedLintErrors.isEmpty())
        }
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test with gradle script plugin`() {
        fixAndCompare(prepareOverriddenRulesConfig(), "kotlin-library-expected.gradle.kts", "kotlin-library.gradle.kts")
        assertUnfixedLintErrors { unfixedLintErrors ->
            assertThat(unfixedLintErrors).containsExactly(
                LintError(
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
                    "domainName" to "org.cqfn.diktat",
                    "disabledChapters" to "Naming,3,4,5,Classes"
                )
            )
        )
        fixAndCompare(configFilePath, "Example1-2Expected.kt", "Example1-2Test.kt")
        assertUnfixedLintErrors { unfixedLintErrors ->
            assertThat(unfixedLintErrors).containsExactlyInAnyOrder(
                LintError(1, 1, "$DIKTAT_RULE_SET_ID:${KdocFormatting.NAME_ID}", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false),
                LintError(3, 1, "$DIKTAT_RULE_SET_ID:${KdocComments.NAME_ID}", "${MISSING_KDOC_TOP_LEVEL.warnText()} example", false),
                LintError(3, 16, "$DIKTAT_RULE_SET_ID:${KdocComments.NAME_ID}", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} isValid", false),
                LintError(6, 5, "$DIKTAT_RULE_SET_ID:${KdocComments.NAME_ID}", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} foo", false),
                LintError(6, 5, "$DIKTAT_RULE_SET_ID:${KdocMethods.NAME_ID}", "${MISSING_KDOC_ON_FUNCTION.warnText()} foo", false),
                LintError(8, 5, "$DIKTAT_RULE_SET_ID:${KdocComments.NAME_ID}", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} foo", false),
                LintError(10, 4, "$DIKTAT_RULE_SET_ID:${KdocFormatting.NAME_ID}", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false),
                LintError(13, 9, "$DIKTAT_RULE_SET_ID:${KdocFormatting.NAME_ID}", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false),
                LintError(18, 40, "$DIKTAT_RULE_SET_ID:${KdocFormatting.NAME_ID}", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false)
            )
        }
    }

    abstract fun fixAndCompare(
        config: Path,
        expected: String,
        test: String,
        trimLastEmptyLine: Boolean = true,
    )

    abstract fun assertUnfixedLintErrors(lintErrorsConsumer: (List<LintError>) -> Unit)

    companion object {
        private const val DEFAULT_CONFIG_PATH = "../diktat-analysis.yml"
        const val RESOURCE_FILE_PATH = "test/smoke/src/main/kotlin"
        private const val TEST_TIMEOUT_SECONDS = 30L
        private val tmpFiles: MutableList<File> = mutableListOf()

        @BeforeAll
        @JvmStatic
        @Suppress("AVOID_NULL_CHECKS")
        internal fun createTmpFiles() {
            listOf(
                "$RESOURCE_FILE_PATH/../../../build.gradle_.kts" to "build.gradle.kts",
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
