package org.cqfn.diktat.ruleset.chapter3.spaces

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import org.cqfn.diktat.ruleset.junit.NaturalDisplayName
import org.cqfn.diktat.ruleset.rules.chapter3.files.IndentationRule
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig.Companion.ALIGNED_PARAMETERS
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_AFTER_OPERATORS
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_BEFORE_DOT
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_FOR_EXPRESSION_BODIES
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_OF_PARAMETERS
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig.Companion.NEWLINE_AT_END
import org.cqfn.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

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

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `multiline string`() {
        fixAndCompare("MultilionStringExpected.kt", "MultilionStringTest.kt")
    }
}
