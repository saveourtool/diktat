package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter3.WhenMustHaveElseRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class WhenMustHaveElseWarnTest : LintTestBase(::WhenMustHaveElseRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${WhenMustHaveElseRule.NAME_ID}"

    @Test
    @Tag(WarningNames.WHEN_WITHOUT_ELSE)
    fun `when in func test good`() {
        lintMethod(
            """
                    |fun foo() {
                    |    when(a) {
                    |       1 -> print("x is neither 1 nor 2")
                    |       else -> {}
                    |    }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WHEN_WITHOUT_ELSE)
    fun `when in func test bad`() {
        lintMethod(
            """
                    |fun foo() {
                    |    when(a) {
                    |       1 -> print("x is neither 1 nor 2")
                    |    }
                    |}
                    |
            """.trimMargin(),
            DiktatError(2, 5, ruleId, "${Warnings.WHEN_WITHOUT_ELSE.warnText()} else was not found", true)
        )
    }

    @Test
    @Tag(WarningNames.WHEN_WITHOUT_ELSE)
    fun `when expression in func test good`() {
        lintMethod(
            """
                    |fun foo() {
                    |    val obj = when(a) {
                    |       1 -> print("x is neither 1 nor 2")
                    |    }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WHEN_WITHOUT_ELSE)
    fun `when expression in func test good 2`() {
        lintMethod(
            """
                    |fun foo() {
                    |    val x = listOf<Int>().map {
                    |           when(it) {
                    |               1 -> it * 2
                    |           }
                    |    }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WHEN_WITHOUT_ELSE)
    fun `regression - shouldn't check when in when branches and assignments`() {
        lintMethod(
            """
                    |fun foo() {
                    |    var x: Int
                    |    x = when(it) {
                    |        1 -> when (x) {
                    |            2 -> foo()
                    |        }
                    |    }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WHEN_WITHOUT_ELSE)
    fun `when in func only enum entries`() {
        lintMethod(
            """
                |fun foo() {
                |    val v: Enum
                |    when (v) {
                |        Enum.ONE, Enum.TWO -> foo()
                |        Enum.THREE -> bar()
                |        in Enum.FOUR..Enum.TEN -> boo()
                |        ELEVEN -> anotherFoo()
                |        in TWELVE..Enum.TWENTY -> anotherBar()
                |    }
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WHEN_WITHOUT_ELSE)
    fun `when in func not only enum entries`() {
        lintMethod(
            """
                |fun foo() {
                |    val v: Enum
                |    when (v) {
                |        Enum.ONE -> foo()
                |        f(Enum.TWO) -> bar()
                |    }
                |}
            """.trimMargin(),
            DiktatError(3, 5, ruleId, "${Warnings.WHEN_WITHOUT_ELSE.warnText()} else was not found", true)
        )
    }

    @Test
    @Tag(WarningNames.WHEN_WITHOUT_ELSE)
    fun `when in func not only enum entries but in ranges`() {
        lintMethod(
            """
                |fun foo() {
                |    val v: Enum
                |    when (v) {
                |        Enum.ONE -> foo()
                |        in 1..5 -> bar()
                |    }
                |}
            """.trimMargin(),
            DiktatError(3, 5, ruleId, "${Warnings.WHEN_WITHOUT_ELSE.warnText()} else was not found", true)
        )
    }
}
