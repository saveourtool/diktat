package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter3.BooleanExpressionsRule
import org.cqfn.diktat.ruleset.utils.KotlinParser
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
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
    fun `check distributive laws #2`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if ((a > 5 || b > 5) && (a > 5 || c > 5) && (a > 5 || d > 5)) {
                    |    
                    |    }
                    |    
                    |    if (a > 5 && b > 5 || a > 5 && c > 5 || a > 5 || d > 5) {
                    |    
                    |    }
                    |}
            """.trimMargin(),
            LintError(2, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} (a > 5 || b > 5) && (a > 5 || c > 5) && (a > 5 || d > 5)", true),
            LintError(6, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} a > 5 && b > 5 || a > 5 && c > 5 || a > 5 || d > 5", true)
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
        val returnedString = BooleanExpressionsRule(emptyList()).formatBooleanExpressionAsString(firstCondition, HashMap())
        Assertions.assertEquals("(A & B)", returnedString)
    }

    @Test
    fun `test makeCorrectExpressionString method #2`() {
        val firstCondition = KotlinParser().createNode("a > 5 && b < 6 && c > 7 || a > 5")
        val returnedString = BooleanExpressionsRule(emptyList()).formatBooleanExpressionAsString(firstCondition, HashMap())
        Assertions.assertEquals("(A & B & C | A)", returnedString)
    }

    @Test
    fun `test makeCorrectExpressionString method #3`() {
        val firstCondition = KotlinParser().createNode("a > 5 && b < 6 && (c > 3 || b < 6) && a > 5")
        val returnedString = BooleanExpressionsRule(emptyList()).formatBooleanExpressionAsString(firstCondition, HashMap())
        Assertions.assertEquals("(A & B & (C | B) & A)", returnedString)
    }

    @Test
    fun `test makeCorrectExpressionString method #4`() {
        val firstCondition = KotlinParser().createNode("a > 5 && b < 6 && (c > 3 || b < 6) && a > 666")
        val returnedString = BooleanExpressionsRule(emptyList()).formatBooleanExpressionAsString(firstCondition, HashMap())
        Assertions.assertEquals("(A & B & (C | B) & D)", returnedString)
    }

    @Test
    fun `test makeCorrectExpressionString method #5`() {
        val firstCondition = KotlinParser().createNode("a.and(b)")
        val returnedString = BooleanExpressionsRule(emptyList()).formatBooleanExpressionAsString(firstCondition, HashMap())
        Assertions.assertEquals("(a.and(b))", returnedString)
    }

    @Test
    fun `test makeCorrectExpressionString method #6 - should not convert single expressions`() {
        val firstCondition = KotlinParser().createNode("x.isFoo()")
        val map: java.util.HashMap<String, Char> = HashMap()
        val returnedString = BooleanExpressionsRule(emptyList()).formatBooleanExpressionAsString(firstCondition, map)
        Assertions.assertEquals("(x.isFoo())", returnedString)
        Assertions.assertTrue(map.isEmpty())
    }

    @Test
    fun `test makeCorrectExpressionString method #7`() {
        val firstCondition = KotlinParser().createNode("x.isFoo() && true")
        val returnedString = BooleanExpressionsRule(emptyList()).formatBooleanExpressionAsString(firstCondition, HashMap())
        Assertions.assertEquals("(A & true)", returnedString)
    }

    @Test
    fun `test makeCorrectExpressionString method #8`() {
        val firstCondition = KotlinParser().createNode("a > 5 && b > 6 || c > 7 && a > 5")
        val map: java.util.HashMap<String, Char> = HashMap()
        val returnedString = BooleanExpressionsRule(emptyList()).formatBooleanExpressionAsString(firstCondition, map)
        Assertions.assertEquals("(A & B | C & A)", returnedString)
        Assertions.assertEquals(3, map.size)
    }

    @Test
    fun `regression - should not try to parse certain expressions - NB should check stderr of this test`() {
        lintMethod(
            """
                fun foo() {
                    // single variable in condition
                    if (::testContainerId.isInitialized) {
                        containerManager.dockerClient.removeContainerCmd(testContainerId).exec()
                    }
                    
                    // single variable and binary expression
                    if (::testContainerId.isInitialized || a > 2) {
                        containerManager.dockerClient.removeContainerCmd(testContainerId).exec()
                    }
                    
                    // nested boolean expressions in lambdas
                    if (currentProperty.nextSibling { it.elementType == PROPERTY } == nextProperty) {}
                    
                    if (!(rightSide == null || leftSide == null) &&
                        rightSide.size == leftSide.size &&
                        rightSide.zip(leftSide).all { (first, second) -> first.text == second.text }) {}
                    
                    // nested lambda with if-else
                    if (currentProperty.nextSibling { if (it.elementType == PROPERTY) true else false } == nextProperty) {}
                    
                    // nested boolean expressions in lambdas with multi-line expressions
                    if (node.elementType == TYPE_REFERENCE && node
                        .parents()
                        .map { it.elementType }
                        .none { it == SUPER_TYPE_LIST || it == TYPEALIAS }) {}
                }
            """.trimIndent()
        )
    }
}
