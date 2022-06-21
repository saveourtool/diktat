package org.cqfn.diktat.ruleset.chapter3.spaces

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import org.cqfn.diktat.ruleset.rules.chapter3.files.IndentationRule
import org.cqfn.diktat.util.FixTestBase

import generated.WarningNames
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.MethodOrderer.MethodName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Path

@TestMethodOrder(MethodName::class)
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
     * This test has a counterpart under [IndentationRuleWarnTest].
     *
     * See [#1330](https://github.com/saveourtool/diktat/issues/1330).
     */
    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `expression body functions should remain unchanged if properly indented (extendedIndentAfterOperators = true)`(@TempDir tempDir: Path) {
        val defaultConfig = IndentationConfig("newlineAtEnd" to false)
        val customConfig = defaultConfig.withCustomParameters("extendedIndentAfterOperators" to true)

        lintMultipleMethods(
            expressionBodyFunctionsContinuationIndent,
            tempDir = tempDir,
            rulesConfigList = customConfig.asRulesConfigList())
    }

    /**
     * This test has a counterpart under [IndentationRuleWarnTest].
     *
     * See [#1330](https://github.com/saveourtool/diktat/issues/1330).
     */
    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `expression body functions should remain unchanged if properly indented (extendedIndentAfterOperators = false)`(@TempDir tempDir: Path) {
        val defaultConfig = IndentationConfig("newlineAtEnd" to false)
        val customConfig = defaultConfig.withCustomParameters("extendedIndentAfterOperators" to false)

        lintMultipleMethods(
            expressionBodyFunctionsSingleIndent,
            tempDir = tempDir,
            rulesConfigList = customConfig.asRulesConfigList())
    }

    /**
     * This test has a counterpart under [IndentationRuleWarnTest].
     *
     * See [#1330](https://github.com/saveourtool/diktat/issues/1330).
     */
    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `expression body functions should be reformatted if mis-indented (extendedIndentAfterOperators = true)`(@TempDir tempDir: Path) {
        assumeTrue(testsCanBeMuted()) {
            "Skipping a known-to-fail test"
        }

        val defaultConfig = IndentationConfig("newlineAtEnd" to false)
        val customConfig = defaultConfig.withCustomParameters("extendedIndentAfterOperators" to true)

        lintMultipleMethods(
            actualContent = expressionBodyFunctionsSingleIndent,
            expectedContent = expressionBodyFunctionsContinuationIndent,
            tempDir = tempDir,
            rulesConfigList = customConfig.asRulesConfigList())
    }

    /**
     * This test has a counterpart under [IndentationRuleWarnTest].
     *
     * See [#1330](https://github.com/saveourtool/diktat/issues/1330).
     */
    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `expression body functions should be reformatted if mis-indented (extendedIndentAfterOperators = false)`(@TempDir tempDir: Path) {
        assumeTrue(testsCanBeMuted()) {
            "Skipping a known-to-fail test"
        }

        val defaultConfig = IndentationConfig("newlineAtEnd" to false)
        val customConfig = defaultConfig.withCustomParameters("extendedIndentAfterOperators" to false)

        lintMultipleMethods(
            actualContent = expressionBodyFunctionsContinuationIndent,
            expectedContent = expressionBodyFunctionsSingleIndent,
            tempDir = tempDir,
            rulesConfigList = customConfig.asRulesConfigList())
    }

    /**
     * See [#1347](https://github.com/saveourtool/diktat/issues/1347).
     */
    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `no whitespace should be injected into multi-line string literals (code matches settings, extendedIndent = true)`(@TempDir tempDir: Path) {
        val defaultConfig = IndentationConfig("newlineAtEnd" to false)
        val customConfig = defaultConfig.withCustomParameters(*extendedIndent(enabled = true))

        lintMultipleMethods(
            whitespaceInStringLiteralsContinuationIndent,
            tempDir = tempDir,
            rulesConfigList = customConfig.asRulesConfigList())
    }

    /**
     * See [#1347](https://github.com/saveourtool/diktat/issues/1347).
     */
    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `no whitespace should be injected into multi-line string literals (code matches settings, extendedIndent = false)`(@TempDir tempDir: Path) {
        val defaultConfig = IndentationConfig("newlineAtEnd" to false)
        val customConfig = defaultConfig.withCustomParameters(*extendedIndent(enabled = false))

        lintMultipleMethods(
            whitespaceInStringLiteralsSingleIndent,
            tempDir = tempDir,
            rulesConfigList = customConfig.asRulesConfigList())
    }

    /**
     * See [#1347](https://github.com/saveourtool/diktat/issues/1347).
     */
    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `no whitespace should be injected into multi-line string literals (mis-indented code reformatted, extendedIndent = true)`(@TempDir tempDir: Path) {
        assumeTrue(testsCanBeMuted()) {
            "Skipping a known-to-fail test"
        }

        val defaultConfig = IndentationConfig("newlineAtEnd" to false)
        val customConfig = defaultConfig.withCustomParameters(*extendedIndent(enabled = true))

        lintMultipleMethods(
            actualContent = whitespaceInStringLiteralsSingleIndent,
            expectedContent = whitespaceInStringLiteralsContinuationIndent,
            tempDir = tempDir,
            rulesConfigList = customConfig.asRulesConfigList())
    }

    /**
     * See [#1347](https://github.com/saveourtool/diktat/issues/1347).
     */
    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `no whitespace should be injected into multi-line string literals (mis-indented code reformatted, extendedIndent = false)`(@TempDir tempDir: Path) {
        assumeTrue(testsCanBeMuted()) {
            "Skipping a known-to-fail test"
        }

        val defaultConfig = IndentationConfig("newlineAtEnd" to false)
        val customConfig = defaultConfig.withCustomParameters(*extendedIndent(enabled = false))

        lintMultipleMethods(
            actualContent = whitespaceInStringLiteralsContinuationIndent,
            expectedContent = whitespaceInStringLiteralsSingleIndent,
            tempDir = tempDir,
            rulesConfigList = customConfig.asRulesConfigList())
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
}
