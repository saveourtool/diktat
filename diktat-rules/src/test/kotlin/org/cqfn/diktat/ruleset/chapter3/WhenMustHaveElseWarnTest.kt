package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter3.WhenMustHaveElseRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class WhenMustHaveElseWarnTest : LintTestBase(::WhenMustHaveElseRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:no-else-in-when"

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
            LintError(2, 5, ruleId, "${Warnings.WHEN_WITHOUT_ELSE.warnText()} else was not found", true)
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
    fun `when in func covers all the enum`() {
        lintMethod(
            """
                |enum class ConfirmationType {
                |   NO_BINARY_CONFIRM, NO_CONFIRM, DELETE_CONFIRM
                |}
                |
                |fun foo() {
                |    val confirmationType = ConfirmationType.NO_CONFIRM
                |    when (confirmationType) {
                |        ConfirmationType.NO_BINARY_CONFIRM, ConfirmationType.NO_CONFIRM -> println("NO")
                |        ConfirmationType.DELETE_CONFIRM -> println("YES")
                |        else -> println("REDUNDANT")
                |    }
                |}
            """.trimMargin()
        )
    }
}
