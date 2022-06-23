package org.cqfn.diktat.ruleset.chapter3.spaces

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import org.cqfn.diktat.ruleset.rules.chapter3.files.IndentationRule
import org.cqfn.diktat.util.FixTestBase

import generated.WarningNames
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import java.nio.file.Path

@TestMethodOrder(MethodOrderer.DisplayName::class)
class IndentationRuleFixTest : FixTestBase("test/paragraph3/indentation",
    ::IndentationRule,
    listOf(
        RulesConfig(WRONG_INDENTATION.name, true,
            mapOf(
                "newlineAtEnd" to "true",  // expected file should have two newlines at end in order to be read by BufferedReader correctly
                "extendedIndentOfParameters" to "true",
                "alignedParameters" to "true",
                "extendedIndentAfterOperators" to "true",
                "extendedIndentBeforeDot" to "true",
            )
        )
    )
), IndentationRuleTestMixin {
    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `parameters should be properly aligned`() {
        fixAndCompare("IndentationParametersExpected.kt", "IndentationParametersTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `indentation rule - example 1`() {
        fixAndCompare("IndentationFull1Expected.kt", "IndentationFull1Test.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `indentation rule - verbose example from ktlint`() {
        fixAndCompare("IndentFullExpected.kt", "IndentFullTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `regression - incorrect fixing in constructor parameter list`() {
        fixAndCompare("ConstructorExpected.kt", "ConstructorTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `multiline string`() {
        fixAndCompare("MultilionStringExpected.kt", "MultilionStringTest.kt")
    }

    /**
     * @param actualContent the original file content (may well be modified as
     *   fixes are applied).
     * @param expectedContent the content the file is expected to have after the
     *   fixes are applied.
     */
    private fun lintMultipleMethods(
        @Language("kotlin") actualContent: Array<String>,
        @Language("kotlin") expectedContent: Array<String> = actualContent,
        tempDir: Path,
        rulesConfigList: List<RulesConfig>? = null
    ) {
        require(actualContent.isNotEmpty()) {
            "code fragments is an empty array"
        }

        require(actualContent.size == expectedContent.size) {
            "The actual and expected code fragments are arrays of different size: ${actualContent.size} != ${expectedContent.size}"
        }

        assertSoftly { softly ->
            (actualContent.asSequenceWithConcatenation() zip
                expectedContent.asSequenceWithConcatenation()).forEach { (actual, expected) ->
                val lintResult = fixAndCompareContent(
                    actual,
                    expected,
                    tempDir,
                    rulesConfigList)

                if (!lintResult.isSuccessful) {
                    softly.assertThat(lintResult.actualContent)
                        .describedAs("lint result for ${actual.describe()}")
                        .isEqualTo(lintResult.expectedContent)
                }
            }
        }
    }

    /**
     * See [#1330](https://github.com/saveourtool/diktat/issues/1330).
     *
     * @see IndentationRuleWarnTest.ExpressionBodyFunctions
     */
    @Nested
    @DisplayName("Expression body functions")
    @TestMethodOrder(MethodOrderer.DisplayName::class)
    inner class ExpressionBodyFunctions {
        /**
         * @see IndentationRuleWarnTest.ExpressionBodyFunctions.shouldBeProperlyIndented
         */
        @ParameterizedTest(name = "extendedIndentAfterOperators = {0}")
        @ValueSource(booleans = [false, true])
        @DisplayName("should remain unchanged if properly indented")
        @Tag(WarningNames.WRONG_INDENTATION)
        fun shouldRemainUnchangedIfProperlyIndented(extendedIndentAfterOperators: Boolean, @TempDir tempDir: Path) {
            val defaultConfig = IndentationConfig("newlineAtEnd" to false)
            val customConfig = defaultConfig.withCustomParameters("extendedIndentAfterOperators" to extendedIndentAfterOperators)

            val expressionBodyFunctions = when {
                extendedIndentAfterOperators -> expressionBodyFunctionsContinuationIndent
                else -> expressionBodyFunctionsSingleIndent
            }

            lintMultipleMethods(
                expressionBodyFunctions,
                tempDir = tempDir,
                rulesConfigList = customConfig.asRulesConfigList())
        }

        /**
         * @see IndentationRuleWarnTest.ExpressionBodyFunctions.shouldBeReportedIfMisIndented
         */
        @ParameterizedTest(name = "extendedIndentAfterOperators = {0}")
        @ValueSource(booleans = [false, true])
        @DisplayName("should be reformatted if mis-indented")
        @Tag(WarningNames.WRONG_INDENTATION)
        fun shouldBeReformattedIfMisIndented(extendedIndentAfterOperators: Boolean, @TempDir tempDir: Path) {
            val defaultConfig = IndentationConfig("newlineAtEnd" to false)
            val customConfig = defaultConfig.withCustomParameters("extendedIndentAfterOperators" to extendedIndentAfterOperators)

            val expressionBodyFunctionsActual = when {
                extendedIndentAfterOperators -> expressionBodyFunctionsSingleIndent
                else -> expressionBodyFunctionsContinuationIndent
            }
            val expressionBodyFunctionsExpected = when {
                extendedIndentAfterOperators -> expressionBodyFunctionsContinuationIndent
                else -> expressionBodyFunctionsSingleIndent
            }

            lintMultipleMethods(
                actualContent = expressionBodyFunctionsActual,
                expectedContent = expressionBodyFunctionsExpected,
                tempDir = tempDir,
                rulesConfigList = customConfig.asRulesConfigList())
        }
    }

    /**
     * See [#1347](https://github.com/saveourtool/diktat/issues/1347).
     */
    @Nested
    @DisplayName("Multi-line string literals")
    @TestMethodOrder(MethodOrderer.DisplayName::class)
    inner class MultilineStringLiterals {
        @ParameterizedTest(name = "extendedIndent = {0}")
        @ValueSource(booleans = [false, true])
        @DisplayName("no whitespace should be injected (code matches settings)")
        @Tag(WarningNames.WRONG_INDENTATION)
        fun noWhitespaceShouldBeInjectedCodeMatchesSettings(extendedIndent: Boolean, @TempDir tempDir: Path) {
            val defaultConfig = IndentationConfig("newlineAtEnd" to false)
            val customConfig = defaultConfig.withCustomParameters(*extendedIndent(enabled = extendedIndent))

            val whitespaceInStringLiterals = when {
                extendedIndent -> whitespaceInStringLiteralsContinuationIndent
                else -> whitespaceInStringLiteralsSingleIndent
            }

            lintMultipleMethods(
                whitespaceInStringLiterals,
                tempDir = tempDir,
                rulesConfigList = customConfig.asRulesConfigList())
        }

        @ParameterizedTest(name = "extendedIndent = {0}")
        @ValueSource(booleans = [false, true])
        @DisplayName("no whitespace should be injected (mis-indented code reformatted)")
        @Tag(WarningNames.WRONG_INDENTATION)
        fun noWhitespaceShouldBeInjectedMisIndentedCodeReformatted(extendedIndent: Boolean, @TempDir tempDir: Path) {
            val defaultConfig = IndentationConfig("newlineAtEnd" to false)
            val customConfig = defaultConfig.withCustomParameters(*extendedIndent(enabled = extendedIndent))

            val whitespaceInStringLiteralsActual = when {
                extendedIndent -> whitespaceInStringLiteralsSingleIndent
                else -> whitespaceInStringLiteralsContinuationIndent
            }
            val whitespaceInStringLiteralsExpected = when {
                extendedIndent -> whitespaceInStringLiteralsContinuationIndent
                else -> whitespaceInStringLiteralsSingleIndent
            }

            lintMultipleMethods(
                actualContent = whitespaceInStringLiteralsActual,
                expectedContent = whitespaceInStringLiteralsExpected,
                tempDir = tempDir,
                rulesConfigList = customConfig.asRulesConfigList())
        }
    }

    /**
     * See [#1340](https://github.com/saveourtool/diktat/issues/1340).
     *
     * @see IndentationRuleWarnTest.ExpressionsWrappedAfterOperator
     */
    @Nested
    @DisplayName("Expressions wrapped after operator")
    @TestMethodOrder(MethodOrderer.DisplayName::class)
    inner class ExpressionsWrappedAfterOperator {
        /**
         * @see IndentationRuleWarnTest.ExpressionsWrappedAfterOperator.shouldBeProperlyIndented
         */
        @ParameterizedTest(name = "extendedIndentAfterOperators = {0}")
        @ValueSource(booleans = [false, true])
        @DisplayName("should be properly indented")
        @Tag(WarningNames.WRONG_INDENTATION)
        fun shouldRemainUnchangedIfProperlyIndented(extendedIndentAfterOperators: Boolean, @TempDir tempDir: Path) {
            val defaultConfig = IndentationConfig("newlineAtEnd" to false)
            val customConfig = defaultConfig.withCustomParameters("extendedIndentAfterOperators" to extendedIndentAfterOperators)

            val expressionsWrappedAfterOperator = when {
                extendedIndentAfterOperators -> expressionsWrappedAfterOperatorContinuationIndent
                else -> expressionsWrappedAfterOperatorSingleIndent
            }

            lintMultipleMethods(
                expressionsWrappedAfterOperator,
                tempDir = tempDir,
                rulesConfigList = customConfig.asRulesConfigList())
        }

        /**
         * @see IndentationRuleWarnTest.ExpressionsWrappedAfterOperator.shouldBeReportedIfMisIndented
         */
        @ParameterizedTest(name = "extendedIndentAfterOperators = {0}")
        @ValueSource(booleans = [false, true])
        @DisplayName("should be reformatted if mis-indented")
        @Tag(WarningNames.WRONG_INDENTATION)
        fun shouldBeReformattedIfMisIndented(extendedIndentAfterOperators: Boolean, @TempDir tempDir: Path) {
            val defaultConfig = IndentationConfig("newlineAtEnd" to false)
            val customConfig = defaultConfig.withCustomParameters("extendedIndentAfterOperators" to extendedIndentAfterOperators)

            val expressionsWrappedAfterOperatorActual = when {
                extendedIndentAfterOperators -> expressionsWrappedAfterOperatorSingleIndent
                else -> expressionsWrappedAfterOperatorContinuationIndent
            }
            val expressionsWrappedAfterOperatorExpected = when {
                extendedIndentAfterOperators -> expressionsWrappedAfterOperatorContinuationIndent
                else -> expressionsWrappedAfterOperatorSingleIndent
            }

            lintMultipleMethods(
                actualContent = expressionsWrappedAfterOperatorActual,
                expectedContent = expressionsWrappedAfterOperatorExpected,
                tempDir = tempDir,
                rulesConfigList = customConfig.asRulesConfigList())
        }
    }
}
