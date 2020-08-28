package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.WhenMustHaveElseRule
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class WhenMustHaveElseWarnTest {

    private val ruleId = "$DIKTAT_RULE_SET_ID:no-else-in-when"

    @Test
    @Tag(WarningNames.WHEN_WITHOUT_ELSE)
    fun `when in func test good`(){
        lintMethod(WhenMustHaveElseRule(),
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
    fun `when in func test bad`(){
        lintMethod(WhenMustHaveElseRule(),
                """
                    |fun foo() {
                    |    when(a) {
                    |       1 -> print("x is neither 1 nor 2")
                    |    }
                    |}
                """.trimMargin(),
                LintError(2,5,ruleId, "${Warnings.WHEN_WITHOUT_ELSE.warnText()} else was not found", true)
        )
    }

    @Test
    @Tag(WarningNames.WHEN_WITHOUT_ELSE)
    fun `when expression in func test good`(){
        lintMethod(WhenMustHaveElseRule(),
                """
                    |fun foo() {
                    |    val obj = when(a) {
                    |       1 -> print("x is neither 1 nor 2")
                    |    }
                    |}
                """.trimMargin()
        )
    }
}
