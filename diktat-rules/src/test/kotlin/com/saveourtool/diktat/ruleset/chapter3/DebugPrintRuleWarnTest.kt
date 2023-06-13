package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter3.DebugPrintRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
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
            DiktatError(2, 5, ruleId, "${Warnings.DEBUG_PRINT.warnText()} found print()", false)
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
            DiktatError(2, 5, ruleId, "${Warnings.DEBUG_PRINT.warnText()} found println()", false)
        )
    }

    @Test
    @Tag(WarningNames.DEBUG_PRINT)
    fun `call of println without arguments`() {
        lintMethod(
            """
                |fun test() {
                |    println()
                |}
            """.trimMargin(),
            DiktatError(2, 5, ruleId, "${Warnings.DEBUG_PRINT.warnText()} found println()", false)
        )
    }

    @Test
    @Tag(WarningNames.DEBUG_PRINT)
    fun `custom method print by argument list`() {
        lintMethod(
            """
                |fun test() {
                |    print("test custom args", 123)
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

    @Test
    @Tag(WarningNames.DEBUG_PRINT)
    fun `call of console`() {
        lintMethod(
            """
                |fun test() {
                |    console.error("1")
                |    console.info("1", "2")
                |    console.log("1", "2", "3")
                |    console.warn("1", "2", "3", "4")
                |}
            """.trimMargin(),
            DiktatError(2, 5, ruleId, "${Warnings.DEBUG_PRINT.warnText()} found console.error()", false),
            DiktatError(3, 5, ruleId, "${Warnings.DEBUG_PRINT.warnText()} found console.info()", false),
            DiktatError(4, 5, ruleId, "${Warnings.DEBUG_PRINT.warnText()} found console.log()", false),
            DiktatError(5, 5, ruleId, "${Warnings.DEBUG_PRINT.warnText()} found console.warn()", false)
        )
    }

    @Test
    @Tag(WarningNames.DEBUG_PRINT)
    fun `custom method console with lambda as last parameter`() {
        lintMethod(
            """
                |fun test() {
                |    console.error("1") {
                |       foo("")
                |    }
                |    console.info("1", "2") {
                |       foo("")
                |    }
                |    console.log("1", "2", "3") {
                |       foo("")
                |    }
                |    console.warn("1", "2", "3", "4") {
                |       foo("")
                |    }
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.DEBUG_PRINT)
    fun `call parameter from console`() {
        lintMethod(
            """
                |fun test() {
                |    val foo = console.size
                |}
            """.trimMargin()
        )
    }
}
