package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter3.DebugPrintRule
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class DebugPrintRuleWarnTest : LintTestBase(::DebugPrintRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${DebugPrintRule.NAME_ID}"

    @Test
    @Tag(WarningNames.DEBUG_PRINT)
    fun `call of print`() {
        lintMethod(
            """
                |fun test() {
                |    print("test print")
                |}
            """.trimMargin(),
            LintError(2, 5, ruleId, "${Warnings.DEBUG_PRINT.warnText()} found print()", false)
        )
    }

    @Test
    @Tag(WarningNames.DEBUG_PRINT)
    fun `call of println`() {
        lintMethod(
            """
                |fun test() {
                |    println("test println")
                |}
            """.trimMargin(),
            LintError(2, 5, ruleId, "${Warnings.DEBUG_PRINT.warnText()} found println()", false)
        )
    }

    @Test
    @Tag(WarningNames.DEBUG_PRINT)
    fun `custom method print by argument list`() {
        lintMethod(
            """
                |fun test() {
                |    print("test custom args", 123)
                |    print()
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.DEBUG_PRINT)
    fun `custom method print with lambda as last parameter`() {
        lintMethod(
            """
                |fun test() {
                |    print("test custom method") {
                |       foo("")
                |    }
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.DEBUG_PRINT)
    fun `custom method print in another object`() {
        lintMethod(
            """
                |fun test() {
                |    foo.print("test custom method")
                |}
            """.trimMargin()
        )
    }
}