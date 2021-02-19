package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.constants.Warnings.STRING_CONCATENATION
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.*

import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.PLUS
import com.pinterest.ktlint.core.ast.ElementType.STRING_TEMPLATE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtOperationExpression
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
    "string-concatenation",
    configRules,
    listOf(
        STRING_CONCATENATION
    )
) {
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
    private fun detectStringConcatenation(topLevelBinaryExpr: ASTNode) {
        val allBinaryExpressions = topLevelBinaryExpr.findAllDescendantsWithSpecificType(BINARY_EXPRESSION)
        val nodeWithBug = allBinaryExpressions.find { isDetectStringConcatenationInExpression(it) }

        val bugDetected = nodeWithBug != null

        if (bugDetected) {
            STRING_CONCATENATION.warnAndFix(
                configRules, emitWarn,
                this.isFixMode, topLevelBinaryExpr.text, nodeWithBug!!.startOffset, nodeWithBug
            ) {
                circle(topLevelBinaryExpr.treeParent)
            }
        }
    }

    private fun circle(parentTopLevelBinaryExpr: ASTNode) {
        val allBinaryExpressions = parentTopLevelBinaryExpr.findAllDescendantsWithSpecificType(BINARY_EXPRESSION)
        val nodeWithBug = allBinaryExpressions.find { isDetectStringConcatenationInExpression(it) }

        val bugDetected = nodeWithBug != null
        if (bugDetected) {
            fixBinaryExpressionWithConcatenation(nodeWithBug)
            circle(parentTopLevelBinaryExpr)
        }
    }

    /**
     * We can detect string concatenation by the first (left) operand in binary expression.
     * If it is of type string - then we found string concatenation.
     */
    private fun isDetectStringConcatenationInExpression(node: ASTNode): Boolean {
        assert(node.elementType == BINARY_EXPRESSION) {
            "cannot process non binary expression in the process of detecting string concatenation"
        }
        val firstChild = node.firstChildNode
        return (isPlusBinaryExpression(node) &&
                (firstChild.elementType == STRING_TEMPLATE ||
                        (firstChild.text.contains("toString()")) && firstChild.elementType == DOT_QUALIFIED_EXPRESSION)
        )
    }

    private fun isPlusBinaryExpression(node: ASTNode): Boolean {
        assert(node.elementType == BINARY_EXPRESSION)
        // binary expression
        // /        |        \
        // expr1 operationRef expr2

        val operationReference = node.getFirstChildWithType(OPERATION_REFERENCE)
        return operationReference
            ?.getFirstChildWithType(PLUS) != null
    }

    private fun fixBinaryExpressionWithConcatenation(node: ASTNode?) {
        val binaryExpressionNode = node?.psi as KtBinaryExpression
        val parentNode = node.treeParent
        val textNode = checkKtExpression(binaryExpressionNode)
        val newNode = KotlinParser().createNode("\"$textNode\"")
        parentNode.replaceChild(node, newNode)
    }

    private fun isPlusBinaryExpressionAndFirstElementString(binaryExpressionNode: KtBinaryExpression) =
            (binaryExpressionNode.left is KtStringTemplateExpression) && PLUS == binaryExpressionNode.operationToken

    @Suppress(
        "TOO_LONG_FUNCTION",
        "NESTED_BLOCK",
        "SAY_NO_TO_VAR")
    private fun checkKtExpression(binaryExpressionNode: KtBinaryExpression): String {
        var lvalueText = binaryExpressionNode.left!!.text.replace("\"", "")
        val rvalueText = binaryExpressionNode.right!!.text

        if (binaryExpressionNode.isLvalueDotQualifiedExpression() && binaryExpressionNode.firstChild.text.contains("toString()")) {
            // =========== (1 + 2).toString() -> $(1 + 2)
            val leftText = binaryExpressionNode.firstChild.firstChild.text
            lvalueText = "\$$leftText"
        }
        if (binaryExpressionNode.isLvalueReferenceExpression() || binaryExpressionNode.isLvalueConstantExpression()) {
            return binaryExpressionNode.text
        }
        if (binaryExpressionNode.isLvalueBinaryExpression()) {
            val rightValue = checkKtExpression(binaryExpressionNode.left as KtBinaryExpression)
            val rightEx = binaryExpressionNode.right
            val rightVal = if (binaryExpressionNode.isRvalueParenthesized()) {
                checkKtExpression(rightEx!!.children.get(0) as KtBinaryExpression)
            } else {
                (rightEx!!.text.replace("\"", ""))
            }
            if (binaryExpressionNode.left!!.text == rightValue) {
                return binaryExpressionNode.text
            }
            return "$rightValue$rightVal"
        } else if (binaryExpressionNode.isRvalueConstantExpression() || binaryExpressionNode.isRvalueStringTemplateExpression()) {
            // =========== "a " + "b" -> "a b"
            val rvalueTextNew = rvalueText.replace("\"", "")
            return "$lvalueText$rvalueTextNew"
        } else if (binaryExpressionNode.isRvalueReferenceExpression()) {
            // ===========  "a " + b -> "a $b"
            return "$lvalueText$$rvalueText"
        } else if (binaryExpressionNode.isRvalueDotQualifiedExpression()) {
            // ===========  "string " + "valueStr".replace("my", "") -> "string ${"valueStr".replace("my", "")}""
            return "$lvalueText\${$rvalueText}"
        } else if (binaryExpressionNode.isRvalueOperation()) {
            // ===========  "sum " + (1 + 2 + 3) * 4 -> "sum ${(1 + 2 + 3) * 4}"
            return "$lvalueText\${$rvalueText}"
        } else if (binaryExpressionNode.isRvalueParenthesized()) {
            val binExpression = binaryExpressionNode.right!!.children.get(0)
            if (binExpression is KtBinaryExpression) {
                if (isPlusBinaryExpressionAndFirstElementString(binExpression)) {
                    val rightValue = checkKtExpression(binExpression)
                    return "$lvalueText$rightValue"
                } else if (binExpression.isLvalueBinaryExpression()) {
                    val rightValue = checkKtExpression(binExpression.left as KtBinaryExpression)
                    val rightEx = binExpression.right
                    val rightVal = if (binExpression.isRvalueParenthesized()) {
                        checkKtExpression(rightEx!!.children.get(0) as KtBinaryExpression)
                    } else {
                        (rightEx!!.text.replace("\"", ""))
                    }
                    if (binExpression.left!!.text == rightValue) {
                        return "$lvalueText\${$rvalueText}"
                    }
                    return "$lvalueText$rightValue$rightVal"
                }
            }
            return "$lvalueText\${$rvalueText}"
        }
        return binaryExpressionNode.text
    }

    private fun KtBinaryExpression.isRvalueConstantExpression() =
            this.right is KtConstantExpression

    private fun KtBinaryExpression.isRvalueStringTemplateExpression() =
            this.right is KtStringTemplateExpression

    private fun KtBinaryExpression.isRvalueReferenceExpression() =
            this.right is KtReferenceExpression

    private fun KtBinaryExpression.isRvalueDotQualifiedExpression() =
            this.right is KtDotQualifiedExpression

    private fun KtBinaryExpression.isRvalueParenthesized() =
            this.right is KtParenthesizedExpression

    private fun KtBinaryExpression.isRvalueOperation() =
            this.right is KtOperationExpression

    private fun KtBinaryExpression.isLvalueDotQualifiedExpression() =
            this.left is KtDotQualifiedExpression

    private fun KtBinaryExpression.isLvalueBinaryExpression() =
            this.left is KtBinaryExpression

    private fun KtBinaryExpression.isLvalueReferenceExpression() =
            this.left is KtReferenceExpression

    private fun KtBinaryExpression.isLvalueConstantExpression() =
            this.left is KtConstantExpression
}
