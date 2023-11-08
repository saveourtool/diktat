@file:Suppress("FILE_UNORDERED_IMPORTS")// False positives, see #1494.

package com.saveourtool.diktat.ruleset.chapter3.spaces

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import com.saveourtool.diktat.ruleset.junit.NaturalDisplayName
import com.saveourtool.diktat.ruleset.rules.chapter3.files.IndentationRule
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.ALIGNED_PARAMETERS
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_AFTER_OPERATORS
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_BEFORE_DOT
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_FOR_EXPRESSION_BODIES
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_OF_PARAMETERS
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.NEWLINE_AT_END
import com.saveourtool.diktat.test.framework.processing.TestFileContent
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

import com.saveourtool.diktat.ruleset.chapter3.spaces.IndentationConfigFactory as IndentationConfig

/**
 * Legacy indentation tests.
 *
 * Consider adding new tests to [IndentationRuleTest] instead.
 *
 * @see IndentationRuleTest
 */
@TestMethodOrder(NaturalDisplayName::class)
class IndentationRuleFixTest : FixTestBase("test/paragraph3/indentation",
    ::IndentationRule,
    listOf(
        RulesConfig(WRONG_INDENTATION.name, true,
            mapOf(
                NEWLINE_AT_END to "true",  // expected file should have two newlines at end in order to be read by BufferedReader correctly
                EXTENDED_INDENT_OF_PARAMETERS to "true",
                ALIGNED_PARAMETERS to "true",
                EXTENDED_INDENT_FOR_EXPRESSION_BODIES to "true",
                EXTENDED_INDENT_AFTER_OPERATORS to "true",
                EXTENDED_INDENT_BEFORE_DOT to "true",
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

    @Nested
    @TestMethodOrder(NaturalDisplayName::class)
    inner class `Multi-line string literals` {
        /**
         * Correctly-indented opening quotation mark, incorrectly-indented
         * closing quotation mark.
         */
        @Test
        @Tag(WarningNames.WRONG_INDENTATION)
        @Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")  // False positives
        fun `case 1 - mis-aligned opening and closing quotes`(@TempDir tempDir: Path) {
            val actualCode = """
            |fun f() {
            |    g(
            |        ""${'"'}
            |            |val q = 1
            |            |
            |                    ""${'"'}.trimMargin(),
            |        arg1 = "arg1"
            |    )
            |}
            """.trimMargin()

            val expectedCode = """
            |fun f() {
            |    g(
            |        ""${'"'}
            |            |val q = 1
            |            |
            |        ""${'"'}.trimMargin(),
            |        arg1 = "arg1"
            |    )
            |}
            """.trimMargin()

            val lintResult = fixAndCompareContent(actualCode, expectedCode, tempDir)
            lintResult.assertSuccessful()
        }

        /**
         * Both the opening and the closing quotation marks are incorrectly
         * indented (indentation level is less than needed).
         */
        @Test
        @Tag(WarningNames.WRONG_INDENTATION)
        @Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")  // False positives
        fun `case 2`(@TempDir tempDir: Path) {
            val actualCode = """
            |fun f() {
            |    g(
            |    ""${'"'}
            |            |val q = 1
            |            |
            |    ""${'"'}.trimMargin(),
            |        arg1 = "arg1"
            |    )
            |}
            """.trimMargin()

            val expectedCode = """
            |fun f() {
            |    g(
            |        ""${'"'}
            |                |val q = 1
            |                |
            |        ""${'"'}.trimMargin(),
            |        arg1 = "arg1"
            |    )
            |}
            """.trimMargin()

            val lintResult = fixAndCompareContent(actualCode, expectedCode, tempDir)
            lintResult.assertSuccessful()
        }

        /**
         * Both the opening and the closing quotation marks are incorrectly
         * indented (indentation level is greater than needed).
         */
        @Test
        @Tag(WarningNames.WRONG_INDENTATION)
        @Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")  // False positives
        fun `case 3`(@TempDir tempDir: Path) {
            val actualCode = """
            |fun f() {
            |    g(
            |            ""${'"'}
            |                    |val q = 1
            |                    |
            |            ""${'"'}.trimMargin(),
            |        arg1 = "arg1"
            |    )
            |}
            """.trimMargin()

            val expectedCode = """
            |fun f() {
            |    g(
            |        ""${'"'}
            |                |val q = 1
            |                |
            |        ""${'"'}.trimMargin(),
            |        arg1 = "arg1"
            |    )
            |}
            """.trimMargin()

            val lintResult = fixAndCompareContent(actualCode, expectedCode, tempDir)
            lintResult.assertSuccessful()
        }

        /**
         * Both the opening and the closing quotation marks are incorrectly
         * indented and misaligned.
         */
        @Test
        @Tag(WarningNames.WRONG_INDENTATION)
        @Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")  // False positives
        fun `case 4 - mis-aligned opening and closing quotes`(@TempDir tempDir: Path) {
            val actualCode = """
            |fun f() {
            |    g(
            |            ""${'"'}
            |                    |val q = 1
            |                    |
            |                            ""${'"'}.trimMargin(),
            |        arg1 = "arg1"
            |    )
            |}
            """.trimMargin()

            val expectedCode = """
            |fun f() {
            |    g(
            |        ""${'"'}
            |                |val q = 1
            |                |
            |        ""${'"'}.trimMargin(),
            |        arg1 = "arg1"
            |    )
            |}
            """.trimMargin()

            val lintResult = fixAndCompareContent(actualCode, expectedCode, tempDir)
            lintResult.assertSuccessful()
        }

        private fun fixAndCompareContent(@Language("kotlin") actualCode: String,
                                         @Language("kotlin") expectedCode: String,
                                         tempDir: Path
        ): TestFileContent {
            val config = IndentationConfig(NEWLINE_AT_END to false).withCustomParameters().asRulesConfigList()
            return fixAndCompareContent(actualCode, expectedCode, tempDir, overrideRulesConfigList = config)
        }
    }
}
