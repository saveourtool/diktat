package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.constants.Warnings.MORE_THAN_ONE_STATEMENT_PER_LINE
import org.cqfn.diktat.ruleset.rules.chapter3.SingleLineStatementsRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class SingleLineStatementsRuleWarnTest : LintTestBase(::SingleLineStatementsRule) {
    private val ruleId = SingleLineStatementsRule.NAME_ID

    @Test
    @Tag(WarningNames.MORE_THAN_ONE_STATEMENT_PER_LINE)
    fun `check two statement per line`() {
        lintMethod(
            """
                    |import com.pinterest.ktlint.core.KtLint; import com.pinterest.ktlint.core.LintError
                    |
                    |fun foo() {
                    |    if (x < -5) {
                    |       goo(); hoo()
                    |    }
                    |    else {
                    |    }
                    |
                    |    when(x) {
                    |       1 -> println(1)
                    |       else -> println("3;5")
                    |    }
                    |    val a = 5; val b = 10
                    |    println(1); println(1)
                    |}
            """.trimMargin(),
            LintError(1, 40, ruleId, "${MORE_THAN_ONE_STATEMENT_PER_LINE.warnText()} import com.pinterest.ktlint.core.KtLint; import com.pinterest.ktlint.core.LintError", true),
            LintError(5, 13, ruleId, "${MORE_THAN_ONE_STATEMENT_PER_LINE.warnText()} goo(); hoo()", true),
            LintError(14, 14, ruleId, "${MORE_THAN_ONE_STATEMENT_PER_LINE.warnText()} val a = 5; val b = 10", true),
            LintError(15, 15, ruleId, "${MORE_THAN_ONE_STATEMENT_PER_LINE.warnText()} println(1); println(1)", true)
        )
    }

    @Test
    @Tag(WarningNames.MORE_THAN_ONE_STATEMENT_PER_LINE)
    fun `check two statement in one line without space`() {
        lintMethod(
            """
                    |fun foo() {
                    |    val a = 5;val b = 10
                    |    println(1);println(1)
                    |}
            """.trimMargin(),
            LintError(2, 14, ruleId, "${MORE_THAN_ONE_STATEMENT_PER_LINE.warnText()} val a = 5;val b = 10", true),
            LintError(3, 15, ruleId, "${MORE_THAN_ONE_STATEMENT_PER_LINE.warnText()} println(1);println(1)", true)
        )
    }

    @Test
    @Tag(WarningNames.MORE_THAN_ONE_STATEMENT_PER_LINE)
    fun `check if expression with semicolon and else block in one line`() {
        lintMethod(
            """
                    |fun foo() {
                    |   if (x > 0){
                    |       goo()
                    |   }; else { print(123) }
                    |}
            """.trimMargin(),
            LintError(4, 5, ruleId, "${MORE_THAN_ONE_STATEMENT_PER_LINE.warnText()} }; else { print(123) }", true)
        )
    }

    @Test
    @Tag(WarningNames.MORE_THAN_ONE_STATEMENT_PER_LINE)
    fun `check correct test without more than one statement`() {
        lintMethod(
            """
                    |import com.pinterest.ktlint.core.KtLint
                    |
                    |fun foo() {
                    |    if (x < -5) {
                    |       goo();
                    |    }
                    |    else {
                    |    }
                    |
                    |    when(x) {
                    |       1 -> println(1)
                    |       else -> println("3;5")
                    |    }
                    |    val a = 5
                    |    println(1)
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.MORE_THAN_ONE_STATEMENT_PER_LINE)
    fun `check semicolon with enum class expression`() {
        lintMethod(
            """
                    |enum class ProtocolState {
                    |   WAITING {
                    |       override fun signal() = TALKING
                    |   },
                    |   TALKING {
                    |       override fun signal() = WAITING
                    |   }; abstract fun signal(): ProtocolState
                    |}
            """.trimMargin(),
            LintError(7, 5, ruleId, "${MORE_THAN_ONE_STATEMENT_PER_LINE.warnText()} }; abstract fun signal(): ProtocolState", true)
        )
    }

    @Test
    @Tag(WarningNames.MORE_THAN_ONE_STATEMENT_PER_LINE)
    fun `check if expression with two wrong semincolon`() {
        lintMethod(
            """
                    |fun foo() {
                    |   if(x > 0) {
                    |       if ( y> 0){
                    |           ji()
                    |       }; gt()
                    |   }; gr()
                    |}
            """.trimMargin(),
            LintError(5, 9, ruleId, "${MORE_THAN_ONE_STATEMENT_PER_LINE.warnText()} }; gt()", true),
            LintError(6, 5, ruleId, "${MORE_THAN_ONE_STATEMENT_PER_LINE.warnText()} }; gr()", true)
        )
    }

    @Test
    @Tag(WarningNames.MORE_THAN_ONE_STATEMENT_PER_LINE)
    fun `check semicolon in the beginning of the line`() {
        lintMethod(
            """
                    |fun foo() {
                    |   ; grr()
                    |}
            """.trimMargin(),
            LintError(2, 4, ruleId, "${MORE_THAN_ONE_STATEMENT_PER_LINE.warnText()} ; grr()", true)
        )
    }
}
