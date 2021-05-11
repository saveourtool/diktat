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
    @Tag(WarningNames.COMPLEX_BOOLEAN_EXPRESSION)
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
    @Tag(WarningNames.COMPLEX_BOOLEAN_EXPRESSION)
    fun `check laws#1`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if (some != null && (some != null || a > 5)) {
                    |    
                    |    }
                    |    
                    |    if (a > 5 || (a > 5 && b > 6)) {
                    |    
                    |    }
                    |    
                    |    if (!!(a > 5 && a > 6)) {
                    |    
                    |    }
                    |    
                    |    if ((a > 5 || b > 5) && (a > 5 || c > 5)) {
                    |    
                    |    }
                    |    
                    |    if ((a > 5 && b > 5) || (a > 5 && c > 5)) {
                    |    
                    |    }
                    |    
                    |    if (a > 5 && false) {
                    |    
                    |    }
                    |    
                    |    if (a > 5 || (!(a > 5) && b > 5)) {
                    |    
                    |    }
                    |}
            """.trimMargin(),
            LintError(2, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} some != null && (some != null || a > 5)", true),
            LintError(6, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} a > 5 || (a > 5 && b > 6)", true),
            LintError(10, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} !!(a > 5 && a > 6)", true),
            LintError(14, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} (a > 5 || b > 5) && (a > 5 || c > 5)", true),
            LintError(18, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} (a > 5 && b > 5) || (a > 5 && c > 5)", true),
            LintError(22, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} a > 5 && false", true),
            LintError(26, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} a > 5 || (!(a > 5) && b > 5)", true)
        )
    }

    @Test
    @Tag(WarningNames.COMPLEX_BOOLEAN_EXPRESSION)
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