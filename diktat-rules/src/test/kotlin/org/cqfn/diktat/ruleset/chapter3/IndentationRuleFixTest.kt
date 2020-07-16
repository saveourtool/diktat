package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import org.cqfn.diktat.ruleset.rules.files.IndentationRule
import org.cqfn.diktat.ruleset.utils.FixTestBase
import org.junit.Test

class IndentationRuleFixTest : FixTestBase("test/paragraph3/indentation", IndentationRule(),
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
    fun `indentation rule - example 1`() {
        fixAndCompare("IndentationFull1Expected.kt", "IndentationFull1Test.kt")
    }
}
