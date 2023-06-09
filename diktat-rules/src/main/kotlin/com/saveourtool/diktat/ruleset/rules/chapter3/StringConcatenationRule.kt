package com.saveourtool.diktat.ruleset.rules.chapter3

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.STRING_CONCATENATION
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.*

import org.jetbrains.kotlin.KtNodeTypes.BINARY_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.DOT_QUALIFIED_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.OPERATION_REFERENCE
import org.jetbrains.kotlin.KtNodeTypes.STRING_TEMPLATE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens.PLUS
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

/**
 * This rule covers checks and fixes related to string concatenation.
 * Rule 3.8 prohibits string concatenation and suggests to use string templates instead
 * if this expressions fits one line. For example:
 * """ string """ + "string" will be converted to "string string"
 * "string " + 1 will be converted to "string 1"
 * "string one " + "string two "
 */
class StringConcatenationRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(
        STRING_CONCATENATION
    )
) {
    @Suppress("COLLAPSE_IF_STATEMENTS")
    override fun logic(node: ASTNode) {
        if (node.elementType == BINARY_EXPRESSION) {
            // searching top-level binary expression to detect any operations with "plus" (+)
            // string concatenation is prohibited only for single line statements
            if (node.findParentNodeWithSpecificType(BINARY_EXPRESSION) == null && isSingleLineStatement(node)) {
                detectStringConcatenation(node)
            }
        }
    }

    private fun isSingleLineStatement(node: ASTNode): Boolean =
        !node.text.contains("\n")

    /**
     * This method works only with top-level binary expressions. It should be checked before the call.
     */
    @Suppress("AVOID_NULL_CHECKS")
    private fun detectStringConcatenation(topLevelBinaryExpr: ASTNode) {
        val allBinaryExpressions = topLevelBinaryExpr.findAllDescendantsWithSpecificType(BINARY_EXPRESSION)
        val nodeWithBug = allBinaryExpressions.find { isDetectStringConcatenationInExpression(it) }

        if (nodeWithBug != null) {
            STRING_CONCATENATION.warnAndFix(
                configRules, emitWarn,
                this.isFixMode, topLevelBinaryExpr.text.lines().first(), nodeWithBug.startOffset, nodeWithBug
            ) {
                fixBinaryExpressionWithConcatenation(nodeWithBug)
                loop(topLevelBinaryExpr.treeParent)
            }
        }
    }

    private fun loop(parentTopLevelBinaryExpr: ASTNode) {
        val allBinaryExpressions = parentTopLevelBinaryExpr.findAllDescendantsWithSpecificType(BINARY_EXPRESSION)
        val nodeWithBug = allBinaryExpressions.find { isDetectStringConcatenationInExpression(it) }

        val bugDetected = nodeWithBug != null
        if (bugDetected) {
            fixBinaryExpressionWithConcatenation(nodeWithBug)
            loop(parentTopLevelBinaryExpr)
        }
    }

    /**
     * We can detect string concatenation by the first (left) operand in binary expression.
     * If it is of type string - then we found string concatenation.
     * If the right value is not a constant string then don't change them to template.
     */
    private fun isDetectStringConcatenationInExpression(node: ASTNode): Boolean {
        require(node.elementType == BINARY_EXPRESSION) {
            "cannot process non binary expression in the process of detecting string concatenation"
        }
        val firstChild = node.firstChildNode
        val lastChild = node.lastChildNode
        return isPlusBinaryExpression(node) && isStringVar(firstChild, lastChild)
    }

    private fun isStringVar(firstChild: ASTNode, lastChild: ASTNode) = firstChild.elementType == STRING_TEMPLATE ||
            ((firstChild.text.endsWith("toString()")) && firstChild.elementType == DOT_QUALIFIED_EXPRESSION && lastChild.elementType == STRING_TEMPLATE)

    @Suppress("COMMENT_WHITE_SPACE")
    private fun isPlusBinaryExpression(node: ASTNode): Boolean {
        require(node.elementType == BINARY_EXPRESSION)
        //     binary expression
        //    /        |        \
        //  expr1 operationRef expr2

        val operationReference = node.getFirstChildWithType(OPERATION_REFERENCE)
        return operationReference
            ?.getFirstChildWithType(PLUS) != null
    }

    private fun fixBinaryExpressionWithConcatenation(node: ASTNode?) {
        val binaryExpressionPsi = node?.psi as KtBinaryExpression
        val parentNode = node.treeParent
        val textNode = checkKtExpression(binaryExpressionPsi)
        val newNode = KotlinParser().createNode("\"$textNode\"")
        parentNode.replaceChild(node, newNode)
    }

    private fun isPlusBinaryExpressionAndFirstElementString(binaryExpressionNode: KtBinaryExpression) =
        (binaryExpressionNode.left is KtStringTemplateExpression) && PLUS == binaryExpressionNode.operationToken

    @Suppress(
        "TOO_LONG_FUNCTION",
        "NESTED_BLOCK",
        "SAY_NO_TO_VAR",
        "ComplexMethod"
    )
    private fun checkKtExpression(binaryExpressionPsi: KtBinaryExpression): String {
        var lvalueText = binaryExpressionPsi.left?.text?.trim('"')
        val rvalueText = binaryExpressionPsi.right?.text

        if (binaryExpressionPsi.isLvalueDotQualifiedExpression() && binaryExpressionPsi.firstChild.text.endsWith("toString()")) {
            // =========== (1 + 2).toString() -> ${(1 + 2)}
            val leftText = binaryExpressionPsi.firstChild.firstChild.text
            lvalueText = "\${$leftText}"
        }
        if (binaryExpressionPsi.isLvalueReferenceExpression() || binaryExpressionPsi.isLvalueConstantExpression()) {
            return binaryExpressionPsi.text
        }
        if (binaryExpressionPsi.isLvalueBinaryExpression()) {
            val rightValue = checkKtExpression(binaryExpressionPsi.left as KtBinaryExpression)
            val rightEx = binaryExpressionPsi.right
            val rightVal = if (binaryExpressionPsi.isRvalueParenthesized()) {
                checkKtExpression(rightEx?.children?.get(0) as KtBinaryExpression)
            } else {
                (rightEx?.text?.trim('"'))
            }
            if (binaryExpressionPsi.left?.text == rightValue) {
                return binaryExpressionPsi.text
            }
            return "$rightValue$rightVal"
        } else if (binaryExpressionPsi.isRvalueConstantExpression() || binaryExpressionPsi.isRvalueStringTemplateExpression()) {
            // =========== "a " + "b" -> "a b"
            val rvalueTextNew = rvalueText?.trim('"')
            return "$lvalueText$rvalueTextNew"
        } else if (binaryExpressionPsi.isRvalueCallExpression()) {
            // ===========  "a " + foo() -> "a ${foo()}}"
            return "$lvalueText\${$rvalueText}"
        } else if (binaryExpressionPsi.isRvalueReferenceExpression()) {
            // ===========  "a " + b -> "a $b"
            return "$lvalueText$$rvalueText"
        } else if (!binaryExpressionPsi.isRvalueParenthesized() && binaryExpressionPsi.isRvalueExpression()) {
            return "$lvalueText\${$rvalueText}"
        } else if (binaryExpressionPsi.isRvalueParenthesized()) {
            val binExpression = binaryExpressionPsi.right?.children?.first()
            if (binExpression is KtBinaryExpression) {
                if (isPlusBinaryExpressionAndFirstElementString(binExpression)) {
                    val rightValue = checkKtExpression(binExpression)
                    return "$lvalueText$rightValue"
                } else if (binExpression.isLvalueBinaryExpression()) {
                    val rightValue = checkKtExpression(binExpression.left as KtBinaryExpression)
                    val rightEx = binExpression.right
                    val rightVal = if (binExpression.isRvalueParenthesized()) {
                        checkKtExpression(rightEx?.children?.get(0) as KtBinaryExpression)
                    } else {
                        (rightEx?.text?.trim('"'))
                    }
                    if (binExpression.left?.text == rightValue) {
                        return "$lvalueText\${$rvalueText}"
                    }
                    return "$lvalueText$rightValue$rightVal"
                }
            }
            return "$lvalueText\${$rvalueText}"
        }
        return binaryExpressionPsi.text
    }

    private fun KtBinaryExpression.isRvalueConstantExpression() =
        this.right is KtConstantExpression

    private fun KtBinaryExpression.isRvalueStringTemplateExpression() =
        this.right is KtStringTemplateExpression

    private fun KtBinaryExpression.isRvalueCallExpression() =
        this.right is KtCallExpression

    private fun KtBinaryExpression.isRvalueReferenceExpression() =
        this.right is KtReferenceExpression

    private fun KtBinaryExpression.isRvalueParenthesized() =
        this.right is KtParenthesizedExpression

    private fun KtBinaryExpression.isLvalueDotQualifiedExpression() =
        this.left is KtDotQualifiedExpression

    private fun KtBinaryExpression.isLvalueBinaryExpression() =
        this.left is KtBinaryExpression

    private fun KtBinaryExpression.isLvalueReferenceExpression() =
        this.left is KtReferenceExpression

    private fun KtBinaryExpression.isLvalueConstantExpression() =
        this.left is KtConstantExpression

    private fun KtBinaryExpression.isRvalueExpression() =
        this.right is KtExpression

    companion object {
        const val NAME_ID = "string-concatenation"
    }
}
