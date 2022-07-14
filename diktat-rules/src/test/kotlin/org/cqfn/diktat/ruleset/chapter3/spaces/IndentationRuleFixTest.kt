package org.cqfn.diktat.ruleset.chapter3.spaces

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.chapter3.spaces.IndentationRuleTestMixin.IndentationConfig
import org.cqfn.diktat.ruleset.chapter3.spaces.IndentationRuleTestMixin.asRulesConfigList
import org.cqfn.diktat.ruleset.chapter3.spaces.IndentationRuleTestMixin.asSequenceWithConcatenation
import org.cqfn.diktat.ruleset.chapter3.spaces.IndentationRuleTestMixin.assertNotNull
import org.cqfn.diktat.ruleset.chapter3.spaces.IndentationRuleTestMixin.describe
import org.cqfn.diktat.ruleset.chapter3.spaces.IndentationRuleTestMixin.extendedIndent
import org.cqfn.diktat.ruleset.chapter3.spaces.IndentationRuleTestMixin.withCustomParameters
import org.cqfn.diktat.ruleset.chapter3.spaces.IndentationRuleTestResources.dotQualifiedExpressions
import org.cqfn.diktat.ruleset.chapter3.spaces.IndentationRuleTestResources.expressionBodyFunctions
import org.cqfn.diktat.ruleset.chapter3.spaces.IndentationRuleTestResources.expressionsWrappedAfterOperator
import org.cqfn.diktat.ruleset.chapter3.spaces.IndentationRuleTestResources.parenthesesSurroundedInfixExpressions
import org.cqfn.diktat.ruleset.chapter3.spaces.IndentationRuleTestResources.whitespaceInStringLiterals
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import org.cqfn.diktat.ruleset.rules.chapter3.files.IndentationRule
import org.cqfn.diktat.util.FixTestBase

import generated.WarningNames
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.MethodOrderer.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import java.nio.file.Path

@TestMethodOrder(DisplayName::class)
class IndentationRuleFixTest : FixTestBase("test/paragraph3/indentation",
    ::IndentationRule,
    listOf(
        RulesConfig(WRONG_INDENTATION.name, true,
            mapOf(
                "newlineAtEnd" to "true",  // expected file should have two newlines at end in order to be read by BufferedReader correctly
                "extendedIndentOfParameters" to "true",
                "alignedParameters" to "true",
                "extendedIndentForExpressionBodies" to "true",
                "extendedIndentAfterOperators" to "true",
                "extendedIndentBeforeDot" to "true",
            )
        )
    )
) {
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
     */
    @Nested
    @TestMethodOrder(DisplayName::class)
    inner class `Expression body functions` {
        @ParameterizedTest(name = "extendedIndentForExpressionBodies = {0}")
        @ValueSource(booleans = [false, true])
        @Tag(WarningNames.WRONG_INDENTATION)
        fun `should remain unchanged if properly indented`(extendedIndentForExpressionBodies: Boolean, @TempDir tempDir: Path) {
            val defaultConfig = IndentationConfig("newlineAtEnd" to false)
            val customConfig = defaultConfig.withCustomParameters("extendedIndentForExpressionBodies" to extendedIndentForExpressionBodies)

            lintMultipleMethods(
                expressionBodyFunctions[extendedIndentForExpressionBodies].assertNotNull(),
                tempDir = tempDir,
                rulesConfigList = customConfig.asRulesConfigList())
        }

        @ParameterizedTest(name = "extendedIndentForExpressionBodies = {0}")
        @ValueSource(booleans = [false, true])
        @Tag(WarningNames.WRONG_INDENTATION)
        fun `should be reformatted if mis-indented`(extendedIndentForExpressionBodies: Boolean, @TempDir tempDir: Path) {
            val defaultConfig = IndentationConfig("newlineAtEnd" to false)
            val customConfig = defaultConfig.withCustomParameters("extendedIndentForExpressionBodies" to extendedIndentForExpressionBodies)

            lintMultipleMethods(
                actualContent = expressionBodyFunctions[!extendedIndentForExpressionBodies].assertNotNull(),
                expectedContent = expressionBodyFunctions[extendedIndentForExpressionBodies].assertNotNull(),
                tempDir = tempDir,
                rulesConfigList = customConfig.asRulesConfigList())
        }
    }

    /**
     * See [#1347](https://github.com/saveourtool/diktat/issues/1347).
     */
    @Nested
    @TestMethodOrder(DisplayName::class)
    inner class `Multi-line string literals` {
        @ParameterizedTest(name = "extendedIndent = {0}")
        @ValueSource(booleans = [false, true])
        @Tag(WarningNames.WRONG_INDENTATION)
        fun `no whitespace should be injected (code matches settings)`(extendedIndent: Boolean, @TempDir tempDir: Path) {
            val defaultConfig = IndentationConfig("newlineAtEnd" to false)
            val customConfig = defaultConfig.withCustomParameters(*extendedIndent(enabled = extendedIndent))

            lintMultipleMethods(
                whitespaceInStringLiterals[extendedIndent].assertNotNull(),
                tempDir = tempDir,
                rulesConfigList = customConfig.asRulesConfigList())
        }

        @ParameterizedTest(name = "extendedIndent = {0}")
        @ValueSource(booleans = [false, true])
        @Tag(WarningNames.WRONG_INDENTATION)
        fun `no whitespace should be injected (mis-indented code reformatted)`(extendedIndent: Boolean, @TempDir tempDir: Path) {
            val defaultConfig = IndentationConfig("newlineAtEnd" to false)
            val customConfig = defaultConfig.withCustomParameters(*extendedIndent(enabled = extendedIndent))

            lintMultipleMethods(
                actualContent = whitespaceInStringLiterals[!extendedIndent].assertNotNull(),
                expectedContent = whitespaceInStringLiterals[extendedIndent].assertNotNull(),
                tempDir = tempDir,
                rulesConfigList = customConfig.asRulesConfigList())
        }
    }

    /**
     * See [#1340](https://github.com/saveourtool/diktat/issues/1340).
     */
    @Nested
    @TestMethodOrder(DisplayName::class)
    inner class `Expressions wrapped after operator` {
        @ParameterizedTest(name = "extendedIndentAfterOperators = {0}")
        @ValueSource(booleans = [false, true])
        @Tag(WarningNames.WRONG_INDENTATION)
        fun `should be properly indented`(extendedIndentAfterOperators: Boolean, @TempDir tempDir: Path) {
            val defaultConfig = IndentationConfig("newlineAtEnd" to false)
            val customConfig = defaultConfig.withCustomParameters("extendedIndentAfterOperators" to extendedIndentAfterOperators)

            lintMultipleMethods(
                expressionsWrappedAfterOperator[extendedIndentAfterOperators].assertNotNull(),
                tempDir = tempDir,
                rulesConfigList = customConfig.asRulesConfigList())
        }

        @ParameterizedTest(name = "extendedIndentAfterOperators = {0}")
        @ValueSource(booleans = [false, true])
        @Tag(WarningNames.WRONG_INDENTATION)
        fun `should be reformatted if mis-indented`(extendedIndentAfterOperators: Boolean, @TempDir tempDir: Path) {
            val defaultConfig = IndentationConfig("newlineAtEnd" to false)
            val customConfig = defaultConfig.withCustomParameters("extendedIndentAfterOperators" to extendedIndentAfterOperators)

            lintMultipleMethods(
                actualContent = expressionsWrappedAfterOperator[!extendedIndentAfterOperators].assertNotNull(),
                expectedContent = expressionsWrappedAfterOperator[extendedIndentAfterOperators].assertNotNull(),
                tempDir = tempDir,
                rulesConfigList = customConfig.asRulesConfigList())
        }
    }

    /**
     * See [#1409](https://github.com/saveourtool/diktat/issues/1409).
     */
    @Nested
    @TestMethodOrder(DisplayName::class)
    inner class `Parentheses-surrounded infix expressions` {
        @ParameterizedTest(name = "extendedIndentForExpressionBodies = {0}")
        @ValueSource(booleans = [false, true])
        @Tag(WarningNames.WRONG_INDENTATION)
        fun `should be properly indented`(extendedIndentForExpressionBodies: Boolean, @TempDir tempDir: Path) {
            val defaultConfig = IndentationConfig("newlineAtEnd" to false)
            val customConfig = defaultConfig.withCustomParameters("extendedIndentForExpressionBodies" to extendedIndentForExpressionBodies)

            lintMultipleMethods(
                parenthesesSurroundedInfixExpressions[extendedIndentForExpressionBodies].assertNotNull(),
                tempDir = tempDir,
                rulesConfigList = customConfig.asRulesConfigList())
        }

        @ParameterizedTest(name = "extendedIndentForExpressionBodies = {0}")
        @ValueSource(booleans = [false, true])
        @Tag(WarningNames.WRONG_INDENTATION)
        fun `should be reformatted if mis-indented`(extendedIndentForExpressionBodies: Boolean, @TempDir tempDir: Path) {
            val defaultConfig = IndentationConfig("newlineAtEnd" to false)
            val customConfig = defaultConfig.withCustomParameters("extendedIndentForExpressionBodies" to extendedIndentForExpressionBodies)

            lintMultipleMethods(
                actualContent = parenthesesSurroundedInfixExpressions[!extendedIndentForExpressionBodies].assertNotNull(),
                expectedContent = parenthesesSurroundedInfixExpressions[extendedIndentForExpressionBodies].assertNotNull(),
                tempDir = tempDir,
                rulesConfigList = customConfig.asRulesConfigList())
        }
    }

    /**
     * See [#1336](https://github.com/saveourtool/diktat/issues/1336).
     */
    @Nested
    @TestMethodOrder(DisplayName::class)
    inner class `Dot- and safe-qualified expressions` {
        @ParameterizedTest(name = "extendedIndentBeforeDot = {0}")
        @ValueSource(booleans = [false, true])
        @Tag(WarningNames.WRONG_INDENTATION)
        fun `should be properly indented`(extendedIndentBeforeDot: Boolean, @TempDir tempDir: Path) {
            val defaultConfig = IndentationConfig("newlineAtEnd" to false)
            val customConfig = defaultConfig.withCustomParameters("extendedIndentBeforeDot" to extendedIndentBeforeDot)

            lintMultipleMethods(
                dotQualifiedExpressions[extendedIndentBeforeDot].assertNotNull(),
                tempDir = tempDir,
                rulesConfigList = customConfig.asRulesConfigList())
        }

        @ParameterizedTest(name = "extendedIndentBeforeDot = {0}")
        @ValueSource(booleans = [false, true])
        @Tag(WarningNames.WRONG_INDENTATION)
        fun `should be reformatted if mis-indented`(extendedIndentBeforeDot: Boolean, @TempDir tempDir: Path) {
            val defaultConfig = IndentationConfig("newlineAtEnd" to false)
            val customConfig = defaultConfig.withCustomParameters("extendedIndentBeforeDot" to extendedIndentBeforeDot)

            lintMultipleMethods(
                actualContent = dotQualifiedExpressions[!extendedIndentBeforeDot].assertNotNull(),
                expectedContent = dotQualifiedExpressions[extendedIndentBeforeDot].assertNotNull(),
                tempDir = tempDir,
                rulesConfigList = customConfig.asRulesConfigList())
        }
    }
}
