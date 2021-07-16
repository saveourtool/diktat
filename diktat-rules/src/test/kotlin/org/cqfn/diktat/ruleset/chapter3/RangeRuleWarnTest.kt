package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter3.RangeRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import org.junit.jupiter.api.Test

class RangeRuleWarnTest : LintTestBase(::RangeRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:until"

    @Test
    fun `check simple examples`() {
        lintMethod(
            """
                    |fun foo() {   
                    |    for (i in 1..4) print(i)
                    |    for (i in 4 downTo 1) print(i)
                    |    for (i in 1 until 4) print(i)
                    |    for (i in 1..4 step 2) print(i)
                    |    for (i in 4 downTo 1 step 3) print(i)
                    |    if (6 in (1..10) && true) {}
                    |}
                """.trimMargin(),
            LintError(2, 16, ruleId, "${Warnings.RANGE_TO_UNTIL.warnText()} for (i in 1..4) print(i)", true),
            LintError(5, 16, ruleId, "${Warnings.RANGE_TO_UNTIL.warnText()} for (i in 1..4 step 2) print(i)", true)
        )
    }
}
