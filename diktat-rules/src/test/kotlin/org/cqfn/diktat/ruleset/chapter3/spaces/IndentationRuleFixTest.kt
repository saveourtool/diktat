package org.cqfn.diktat.ruleset.chapter3.spaces

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import org.cqfn.diktat.ruleset.rules.chapter3.files.IndentationRule
import org.cqfn.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class IndentationRuleFixTest : FixTestBase("test/paragraph3/indentation",
    ::IndentationRule,
    listOf(
        RulesConfig(WRONG_INDENTATION.name, true,
            mapOf(
                "newlineAtEnd" to "true",  // expected file should have two newlines at end in order to be read by BufferedReader correctly
                "extendedIndentOfParameters" to "true",
                "alignedParameters" to "true",
                "extendedIndentAfterOperators" to "true"
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
    fun `should add new line at the end of file`() {
        fixAndCompare("NoNewLineAtTheEndExpected.kt", "NoNewLineAtTheEndTest.kt")
    }
}
