package org.cqfn.diktat.ruleset.rules.chapter3

import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.PLUS
import com.pinterest.ktlint.core.ast.ElementType.STRING_TEMPLATE
import org.cqfn.diktat.ruleset.constants.Warnings.STRING_CONCATENATION
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.*
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.*


/**
 * This rule covers checks and fixes related to string concatenation.
 * Rule 3.8 prohibits string concatenation and suggests to use string templates instead
 * if this expressions fits one line. For example:
 * """ string """ + "string" will be converted to "string string"
 * "string " + 1 will be converted to "string 1"
 * "string one " + "string two "
 * FixMe: .toString() method and functions that return strings are not supported
 */
class StringConcatenationRule(configRules: List<RulesConfig>) : DiktatRule(
    "string-concatenation", configRules, listOf(
        Warnings.STRING_CONCATENATION
    )
) {
class StringConcatenationRule(configRules: List<RulesConfig>) : DiktatRule(
    "string-concatenation",
    configRules,
    listOf(STRING_CONCATENATION)) {
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
        val nodeWithBug = allBinaryExpressions.find { detectStringConcatenationInExpression(it) }

        val bugDetected = nodeWithBug != null

        if (bugDetected) {
            STRING_CONCATENATION.warnAndFix(
                configRules, emitWarn,
                this.isFixMode, topLevelBinaryExpr.text, nodeWithBug!!.startOffset, nodeWithBug
            ) {
                fixBinaryExpressionWithConcatenation(nodeWithBug)
            }
        }
    }

    /**
     * We can detect string concatenation by the first (left) operand in binary expression.
     * If it is of type string - then we found string concatenation.
     */
    private fun detectStringConcatenationInExpression(node: ASTNode): Boolean {
        assert(node.elementType == BINARY_EXPRESSION) {
            "cannot process non binary expression in the process of detecting string concatenation"
        }
        val firstChild = node.firstChildNode
        return (isPlusBinaryExpression(node) && firstChild.elementType == STRING_TEMPLATE)
    }

    private fun isPlusBinaryExpression(node: ASTNode): Boolean {
        assert(node.elementType == BINARY_EXPRESSION)
        //     binary expression
        //    /        |        \
        //  expr1 operationRef expr2

        val operationReference = node.getFirstChildWithType(OPERATION_REFERENCE)
        return operationReference
            ?.getFirstChildWithType(PLUS) != null
    }

    private fun fixBinaryExpressionWithConcatenation(node: ASTNode?): ASTNode? {
        return node?.let {
            val binaryExpressionNode = node.psi as KtBinaryExpression

            val parentNode = node.treeParent

            // this is a special very tricky hack: we have special utility methods (KotlinParser)
            // that let us to create a node from a string automatically by this utility
            // instead of creating a node in AST manually by a programmer and creating an hierarchy of nodes in a tree

            // FixMe: code duplication here is temporary until fix will work properly
            if (binaryExpressionNode.isRvalueConstantExpression() || binaryExpressionNode.isRvalueStringTemplateExpression()) {
                // =========== "a " + "b" -> "a b"
                val lvalueText = binaryExpressionNode.left!!.text.replace("\"", "")
                val rvalueText = binaryExpressionNode.right!!.text.replace("\"", "")
                val newNode = KotlinParser().createNode("\"$lvalueText$rvalueText\"")
                parentNode.replaceChild(node, newNode)
                return newNode
            } else if (binaryExpressionNode.isRvalueReferenceExpression()) {
                // ===========  "a " + b -> "a $b"
                val lvalueText = binaryExpressionNode.left!!.text.replace("\"", "")
                val rvalueText = binaryExpressionNode.right!!.text
                val newNode = KotlinParser().createNode("\"$lvalueText$$rvalueText\"")
                parentNode.replaceChild(node, newNode)
                return newNode
            } else if (binaryExpressionNode.isRvalueDotQualifiedExpression()) {
                // ===========  "string " + "valueStr".replace("my", "") -> "string ${"valueStr".replace("my", "")}""
                val lvalueText = binaryExpressionNode.left!!.text.replace("\"", "")
                val rvalueText = binaryExpressionNode.right!!.text
                val newNode = KotlinParser().createNode("\"$lvalueText\${$rvalueText}\"")
                parentNode.replaceChild(node, newNode)
                return newNode
            } else if (binaryExpressionNode.isRvalueParenthesized()) {
                // ===========  "string " + (1 + 2) -> "string ${(1 + 2)}"
                val lvalueText = binaryExpressionNode.left!!.text.replace("\"", "")
                val rvalueText = binaryExpressionNode.right!!.text
                val newNode = KotlinParser().createNode("\"$lvalueText\${$rvalueText}\"")
                parentNode.replaceChild(node, newNode)
                return newNode
            }
            return null
        }
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
}
