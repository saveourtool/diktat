package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.WhenMustHaveElseRule
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Test

class WhenMustHaveElseWarnTest {

    private val ruleId = "$DIKTAT_RULE_SET_ID:no-else-in-when"

    @Test
    fun `else in when test good`(){
        lintMethod(WhenMustHaveElseRule(),
                """
                    |fun foo() {
                    |    when(a) {
                    |       1 -> print("x is neither 1 nor 2")
                    |       else -> print("x is neither 1 nor 2")
                    |    }
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `else in when test bad`(){
        lintMethod(WhenMustHaveElseRule(),
                """
                    |fun foo() {
                    |    when(a) {
                    |       1 -> print("x is neither 1 nor 2")
                    |    }
                    |}
                """.trimMargin(),
                LintError(2,5,ruleId, "${Warnings.WHEN_WITHOUT_ELSE.warnText()} else was not found")
        )
    }
}