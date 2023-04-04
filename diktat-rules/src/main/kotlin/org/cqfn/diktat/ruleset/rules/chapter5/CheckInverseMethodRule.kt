package org.cqfn.diktat.ruleset.rules.chapter5

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.INVERSE_FUNCTION_PREFERRED
import org.cqfn.diktat.ruleset.rules.DiktatRule

import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.LPAR
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.RPAR
import com.pinterest.ktlint.core.ast.ElementType.VALUE_ARGUMENT_LIST
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.psi.psiUtil.siblings

/**
 * This rule checks if inverse method can be used.
 * For example if there is !isEmpty() on collection call that it changes it to isNotEmpty()
 */
class CheckInverseMethodRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(INVERSE_FUNCTION_PREFERRED)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == CALL_EXPRESSION && node.text in methodMap.keys) {
            checkCallExpressionName(node)
        }
    }

    private fun checkCallExpressionName(node: ASTNode) {
        val operationRef = node
            .treeParent
            .siblings(forward = false)
            .takeWhile { it.elementType in intermediateTokens }
            .firstOrNull { it.elementType == OPERATION_REFERENCE }
        if (operationRef?.text == "!") {
            INVERSE_FUNCTION_PREFERRED.warnAndFix(configRules, emitWarn, isFixMode, "${methodMap[node.text]} instead of !${node.text}", node.startOffset, node) {
                val callExpression = CompositeElement(CALL_EXPRESSION)
                val referenceExp = CompositeElement(REFERENCE_EXPRESSION)
                val argList = CompositeElement(VALUE_ARGUMENT_LIST)
                node.treeParent.addChild(callExpression, node)
                callExpression.addChild(referenceExp)
                callExpression.addChild(argList)
                referenceExp.addChild(LeafPsiElement(IDENTIFIER, "${methodMap[node.text]}".dropLast(2)))
                argList.addChild(LeafPsiElement(LPAR, "("))
                argList.addChild(LeafPsiElement(RPAR, ")"))
                node.treeParent.treeParent.removeChild(node.treeParent.treePrev)  // removing OPERATION_EXPRESSION - !
                node.treeParent.removeChild(node)
            }
        }
    }

    companion object {
        const val NAME_ID = "inverse-method"
        val methodMap = mapOf(
            "isEmpty()" to "isNotEmpty()",
            "isBlank()" to "isNotBlank()",
            "isNotEmpty()" to "isEmpty()",
            "isNotBlank()" to "isBlank()"
        )
        val intermediateTokens = listOf(WHITE_SPACE, OPERATION_REFERENCE, BLOCK_COMMENT)
    }
}
