package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import generated.WarningNames
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import org.cqfn.diktat.ruleset.rules.files.IndentationRule
import org.cqfn.diktat.util.FixTestBase
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
    fun `indentation rule - example 1`() {
        fixAndCompare("IndentationFull1Expected.kt", "IndentationFull1Test.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `indentation rule - verbose example from ktlint`() {
        fixAndCompare("IndentFullExpected.kt", "IndentFullTest.kt")
    }
}
