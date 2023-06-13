package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter3.BooleanExpressionsRule
import com.saveourtool.diktat.ruleset.utils.KotlinParser
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class BooleanExpressionsRuleWarnTest : LintTestBase(::BooleanExpressionsRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${BooleanExpressionsRule.NAME_ID}"

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
            DiktatError(2, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} some != null && some != null && some == null", true),
            DiktatError(10, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} a > 3 && b > 3 && a > 3", true),
            DiktatError(14, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} a && a && b > 4", true)
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
            DiktatError(2, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} some != null && (some != null || a > 5)", true),
            DiktatError(6, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} a > 5 || (a > 5 && b > 6)", true),
            DiktatError(10, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} !!(a > 5 && q > 6)", true),
            DiktatError(14, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} a > 5 && false", true),
            DiktatError(18, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} a > 5 || (!(a > 5) && b > 5)", true)
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
            DiktatError(2, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} (a > 5 || b > 5) && (a > 5 || c > 5)", true),
            DiktatError(6, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} a > 5 && b > 5 || a > 5 && c > 5", true)
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
            DiktatError(2, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} (a > 5 || b > 5) && (a > 5 || c > 5) && (a > 5 || d > 5)", true),
            DiktatError(6, 9, ruleId, "${Warnings.COMPLEX_BOOLEAN_EXPRESSION.warnText()} a > 5 && b > 5 || a > 5 && c > 5 || a > 5 || d > 5", true)
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
        checkExpressionFormatter("a > 5 && b < 6", "(A & B)", 2)
    }

    @Test
    fun `test makeCorrectExpressionString method #2`() {
        checkExpressionFormatter("a > 5 && b < 6 && c > 7 || a > 5", "(A & B & C | A)", 3)
    }

    @Test
    fun `test makeCorrectExpressionString method #3`() {
        checkExpressionFormatter("a > 5 && b < 6 && (c > 3 || b < 6) && a > 5", "(A & B & (C | B) & A)", 3)
    }

    @Test
    fun `test makeCorrectExpressionString method #4`() {
        checkExpressionFormatter("a > 5 && b < 6 && (c > 3 || b < 6) && a > 666", "(A & B & (C | B) & D)", 4)
    }

    @Test
    fun `test makeCorrectExpressionString method #5 - should not convert single expressions`() {
        checkExpressionFormatter("a.and(b)", "(a.and(b))", 0)
    }

    @Test
    fun `test makeCorrectExpressionString method #6 - should not convert single expressions`() {
        checkExpressionFormatter("x.isFoo()", "(x.isFoo())", 0)
    }

    @Test
    fun `test makeCorrectExpressionString method #7`() {
        checkExpressionFormatter("x.isFoo() && true", "(A & true)", 1)
    }

    @Test
    fun `test makeCorrectExpressionString method #8`() {
        checkExpressionFormatter(
            "a > 5 && b > 6 || c > 7 && a > 5",
            "(A & B | C & A)",
            3
        )
    }

    @Test
    fun `test makeCorrectExpressionString method #9 - should not account for boolean operators in nested lambdas`() {
        checkExpressionFormatter(
            """
                nextNode != null && nextNode.findChildByType(CALL_EXPRESSION)?.text?.let {
                    it == "trimIndent()" || it == "trimMargin()"
                } == true
            """.trimIndent(),
            "(A & B)",
            2
        )
    }

    @Test
    fun `test makeCorrectExpressionString method #10 - single variable in condition`() {
        checkExpressionFormatter(
            "::testContainerId.isInitialized",
            "(::testContainerId.isInitialized)",
            0
        )
    }

    @Test
    fun `test makeCorrectExpressionString method #11 - variable in condition and binary expression`() {
        checkExpressionFormatter(
            "::testContainerId.isInitialized || a > 2",
            "(B | A)",
            2
        )
    }

    @Test
    fun `test makeCorrectExpressionString method - comment should be removed`() {
        checkExpressionFormatter(
            """
                foo && bar &&
                // FixMe: isLetterOrDigit is not supported in Kotlin 1.4, but 1.5 is not compiling right now
                setOf('_', '-', '.', '"', '\'').baz() && ch.isLetterOrDigit()
            """.trimIndent(),
            "(C & D &  B & A)",
            4
        )
    }

    @Test
    @Suppress("TOO_LONG_FUNCTION", "LongMethod")
    fun `regression - should not log ANTLR errors when parsing is not required`() {
        val stream = ByteArrayOutputStream()
        System.setErr(PrintStream(stream))
        lintMethod(
            """
                fun foo() {
                    // nested boolean expressions in lambdas
                    if (currentProperty.nextSibling { it.elementType == PROPERTY } == nextProperty) {}

                    if (rightSide != null && leftSide != null &&
                        rightSide.size == leftSide.size &&
                        rightSide.zip(leftSide).all { (first, second) -> first.text == second.text }) {}

                    // nested lambda with if-else
                    if (currentProperty.nextSibling { if (it.elementType == PROPERTY) true else false } == nextProperty) {}

                    // nested boolean expressions in lambdas with multi-line expressions
                    if (node.elementType == TYPE_REFERENCE && node
                        .parents()
                        .map { it.elementType }
                        .none { it == SUPER_TYPE_LIST || it == TYPEALIAS }) {}

                    // binary expression with boolean literal
                    if (result?.flag == true) {}

                    if (leftOffset + binaryText.length > wrongBinaryExpression.maximumLineLength && index != 0) {}

                    // with in and !in
                    if (!isImportOrPackage && previousNonWhiteSpaceNode in acc.last()) {}
                    if (node.elementType == LABEL_QUALIFIER && node.text !in labels && node.treeParent.elementType in stopWords) {}

                    if ((node.treeNext.elementType == RBRACE) xor (node.treePrev.elementType == LBRACE)) {}

                    if (listOfNodesBeforeNestedIf.any { it.elementType !in allowedTypes } ||
                        listOfNodesAfterNestedIf.any { it.elementType !in allowedTypes }) {
                            return null
                    }
                    if ((parentNode.psi as KtIfExpression).`else` != null ||
                        (nestedIfNode.psi as KtIfExpression).`else` != null) {}
                    if (listOfWarnings.add(currNode.startOffset to currNode)) {}
                }
            """.trimIndent()
        )
        System.setErr(System.err)
        val stderr = stream.toString()
        Assertions.assertTrue(stderr.isEmpty()) {
            "stderr should be empty, but got the following:${System.lineSeparator()}$stderr"
        }
    }

    private fun checkExpressionFormatter(
        expression: String,
        expectedRepresentation: String,
        expectedMapSize: Int
    ) {
        val stream = ByteArrayOutputStream()
        System.setErr(PrintStream(stream))
        val node = KotlinParser().createNode(expression)
        val rule = BooleanExpressionsRule(emptyList())
        val map: BooleanExpressionsRule.ExpressionsReplacement = rule.ExpressionsReplacement()
        val result = rule.formatBooleanExpressionAsString(node, map)
        Assertions.assertEquals(expectedRepresentation, result)
        Assertions.assertEquals(expectedMapSize, map.size())
        System.setErr(System.err)
        val stderr = stream.toString()
        Assertions.assertTrue(stderr.isEmpty()) {
            "stderr should be empty, but got the following:${System.lineSeparator()}$stderr"
        }
    }
}
