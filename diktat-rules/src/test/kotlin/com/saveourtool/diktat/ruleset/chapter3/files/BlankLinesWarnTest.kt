package com.saveourtool.diktat.ruleset.chapter3.files

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings.TOO_MANY_BLANK_LINES
import com.saveourtool.diktat.ruleset.rules.chapter3.files.BlankLinesRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class BlankLinesWarnTest : LintTestBase(::BlankLinesRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${BlankLinesRule.NAME_ID}"
    private val consecutiveLinesWarn = "${TOO_MANY_BLANK_LINES.warnText()} do not use more than two consecutive blank lines"
    private fun blankLinesInBlockWarn(isBeginning: Boolean) =
        "${TOO_MANY_BLANK_LINES.warnText()} do not put newlines ${if (isBeginning) "in the beginning" else "at the end"} of code blocks"

    @Test
    @Tag(WarningNames.TOO_MANY_BLANK_LINES)
    fun `blank lines usage - positive example`() {
        lintMethod(
            """
                    |class Example {
                    |    fun foo() {
                    |
                    |    }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_BLANK_LINES)
    fun `check lambda with empty block`() {
        lintMethod(
            """
                    |fun foo() {
                    |   run {
                    |
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_BLANK_LINES)
    fun `should prohibit usage of two or more consecutive blank lines`() {
        lintMethod(
            """
                    |class Example {
                    |
                    |
                    |    val foo = 0
                    |
                    |
                    |    fun bar() { }
                    |}
            """.trimMargin(),
            DiktatError(1, 16, ruleId, consecutiveLinesWarn, true),
            DiktatError(4, 16, ruleId, consecutiveLinesWarn, true)
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_BLANK_LINES)
    fun `should prohibit blank lines in the beginning and at the end of code block`() {
        lintMethod(
            """
                    |class Example {
                    |
                    |    fun foo() {
                    |
                    |        bar()
                    |
                    |    }
                    |}
            """.trimMargin(),
            DiktatError(1, 16, ruleId, blankLinesInBlockWarn(true), true),
            DiktatError(3, 16, ruleId, blankLinesInBlockWarn(true), true),
            DiktatError(5, 14, ruleId, blankLinesInBlockWarn(false), true)
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_BLANK_LINES)
    fun `should prohibit empty line before the closing quote`() {
        lintMethod(
            """
                    |class Example {
                    |    fun foo() {
                    |        bar()
                    |
                    |    }
                    |
                    |}
            """.trimMargin(),
            DiktatError(3, 14, ruleId, blankLinesInBlockWarn(false), true),
            DiktatError(5, 6, ruleId, blankLinesInBlockWarn(false), true)
        )
    }
}
