package org.cqfn.diktat.ruleset.chapter4

import com.pinterest.ktlint.core.LintError
import generated.WarningNames.SAY_NO_TO_VAR
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.ImmutableValNoVarRule
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class NoVarRuleWarnTest : LintTestBase(::ImmutableValNoVarRule) {

    private val ruleId = "$DIKTAT_RULE_SET_ID:no-var-rule"

    @Test
    @Tag(SAY_NO_TO_VAR)
    fun `1`() {
        lintMethod(
                """
                    | fun foo() { 
                    |     var x = 0
                    |     while (x < 10) {
                    |        val f: (Int -> Int?) = x -> null
                    |        x++
                    |     }
                    | }
                """.trimMargin()
        )
    }

    @Test
    @Tag(SAY_NO_TO_VAR)
    fun `2`() {
        lintMethod(
                """
                    | fun foo() { 
                    |     for (x in 0..10) println(x)
                    | }
                """.trimMargin()
        )
    }
}
