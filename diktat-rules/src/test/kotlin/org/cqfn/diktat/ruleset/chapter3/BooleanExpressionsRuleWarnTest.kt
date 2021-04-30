package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter3.BooleanExpressionsRule
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class BooleanExpressionsRuleWarnTest : LintTestBase(::BooleanExpressionsRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:boolean-expressions-rule"

    @Test
//    @Tag(WarningNames) TODO
    fun `check boolean expression`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if (some != null && some != null && some == null) {
                    |       goo()
                    |    }
                    |    
                    |    if (some != null && some == 7) {
                    |    
                    |    }
                    |    
                    |    if (a > 3 && b > 3 && a > 3) {
                    |    
                    |    }
                    |}
            """.trimMargin(),
            LintError(2, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} some != null && some != null && some == null", true),
            LintError(10, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} a > 3 && b > 3 && a > 3", true)
        )
    }

    @Test
    fun `should not trigger on method calls`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if (a.and(b)) {
                    |    
                    |    }
                    |}
            """.trimMargin()
        )
    }
}