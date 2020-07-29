package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.constants.Warnings.MORE_ONE_STATEMENT_PER_LINE
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.Statement
import org.cqfn.diktat.util.lintMethod
import org.junit.Test

class StatementWarnTest {

    private val ruleId = "$DIKTAT_RULE_SET_ID:statement"


    @Test
    fun `check two statement per line`() {
        lintMethod(Statement(),
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
                LintError(1,40,ruleId,"${MORE_ONE_STATEMENT_PER_LINE.warnText()} No more than one statement per line", false),
                LintError(5,13,ruleId,"${MORE_ONE_STATEMENT_PER_LINE.warnText()} No more than one statement per line", false),
                LintError(14,14,ruleId,"${MORE_ONE_STATEMENT_PER_LINE.warnText()} No more than one statement per line", false),
                LintError(15,15,ruleId,"${MORE_ONE_STATEMENT_PER_LINE.warnText()} No more than one statement per line", false)
        )
    }

    @Test
    fun `check two statement in one line without space`() {
        lintMethod(Statement(),
                """
                    |fun foo() {
                    |    val a = 5;val b = 10
                    |    println(1);println(1)
                    |}
                """.trimMargin(),
                LintError(2,14,ruleId,"${MORE_ONE_STATEMENT_PER_LINE.warnText()} No more than one statement per line", false),
                LintError(3,15,ruleId,"${MORE_ONE_STATEMENT_PER_LINE.warnText()} No more than one statement per line", false)
        )
    }

    @Test
    fun `check correct test without more than one statement`() {
        lintMethod(Statement(),
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
}
