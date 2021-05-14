package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter3.BooleanExpressionsRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.ruleset.utils.KotlinParser
import org.junit.jupiter.api.Assertions
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
                    |    
                    |    if (a && a && b > 4) {
                    |    
                    |    }
                    |}
            """.trimMargin(),
            LintError(2, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} some != null && some != null && some == null", true),
            LintError(10, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} a > 3 && b > 3 && a > 3", true),
            LintError(14, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} a && a && b > 4", true)
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
                    |    if (!!(a > 5 && q > 6)) {
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
            LintError(10, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} !!(a > 5 && q > 6)", true),
            LintError(14, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} a > 5 && false", true),
            LintError(18, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} a > 5 || (!(a > 5) && b > 5)", true)
        )
    }

    @Test
    @Tag(WarningNames.COMPLEX_BOOLEAN_EXPRESSION)
    fun `check distributive laws`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if ((a > 5 || b > 5) && (a > 5 || c > 5)) {
                    |    
                    |    }
                    |    
                    |    if (a > 5 && b > 5 || a > 5 && c > 5) {
                    |    
                    |    }
                    |}
            """.trimMargin(),
            LintError(2, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} (a > 5 || b > 5) && (a > 5 || c > 5)", true),
            LintError(6, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} a > 5 && b > 5 || a > 5 && c > 5", true)
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

    @Test
    fun `test makeCorrectExpressionString method #1`() {
        val firstCondition = KotlinParser().createNode("a > 5 && b < 6")
        val returnedString = BooleanExpressionsRule(emptyList()).makeCorrectExpressionString(firstCondition, HashMap())
        Assertions.assertEquals(returnedString, "(A & B)")
    }

    @Test
    fun `test makeCorrectExpressionString method #2`() {
        val firstCondition = KotlinParser().createNode("a > 5 && b < 6 && c > 7 || a > 5")
        val returnedString = BooleanExpressionsRule(emptyList()).makeCorrectExpressionString(firstCondition, HashMap())
        Assertions.assertEquals(returnedString, "(A & B & C | A)")
    }

    @Test
    fun `test makeCorrectExpressionString method #3`() {
        val firstCondition = KotlinParser().createNode("a > 5 && b < 6 && (c > 3 || b < 6) && a > 5")
        val returnedString = BooleanExpressionsRule(emptyList()).makeCorrectExpressionString(firstCondition, HashMap())
        Assertions.assertEquals(returnedString, "(A & B & (C | B) & A)")
    }

    @Test
    fun `test makeCorrectExpressionString method #4`() {
        val firstCondition = KotlinParser().createNode("a > 5 && b < 6 && (c > 3 || b < 6) && a > 666")
        val returnedString = BooleanExpressionsRule(emptyList()).makeCorrectExpressionString(firstCondition, HashMap())
        Assertions.assertEquals(returnedString, "(A & B & (C | B) & D)")
    }

    @Test
    fun `test makeCorrectExpressionString method #5`() {
        val firstCondition = KotlinParser().createNode("a.and(b)")
        val returnedString = BooleanExpressionsRule(emptyList()).makeCorrectExpressionString(firstCondition, HashMap())
        Assertions.assertEquals(returnedString, "(a.and(b))")
    }
}
