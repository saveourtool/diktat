package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.RANGE_TO_UNTIL
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.takeByChainOfTypes

import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.MINUS
import com.pinterest.ktlint.core.ast.ElementType.RANGE
import com.pinterest.ktlint.core.ast.isWhiteSpace
import com.pinterest.ktlint.core.ast.parent
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.psi.KtBinaryExpression

/**
 * This rule warn and fix cases when it possible to replace range with until
 */
class RangeRule(configRules: List<RulesConfig>) : DiktatRule(
    "until",
    configRules,
    listOf(RANGE_TO_UNTIL)) {
    override fun logic(node: ASTNode) {
        if (node.elementType == RANGE) {
            val binaryInExpression = (node.parent({ it.elementType == BINARY_EXPRESSION })?.psi as KtBinaryExpression?)
            (binaryInExpression
                ?.right
                ?.node
                ?.takeByChainOfTypes(BINARY_EXPRESSION)
                ?.psi as KtBinaryExpression?)
                ?.operationReference
                ?.node
                ?.findChildByType(MINUS)
                ?.let {
                    val errorNode = binaryInExpression!!.node
                    RANGE_TO_UNTIL.warnAndFix(configRules, emitWarn, isFixMode, errorNode.text, errorNode.startOffset, errorNode) {
                        val untilNode = LeafPsiElement(IDENTIFIER, "until")
                        val parent = node.treeParent
                        if (parent.treePrev?.isWhiteSpace() != true) {
                            parent.treeParent.addChild(PsiWhiteSpaceImpl(" "), parent)
                        }
                        if (parent.treeNext?.isWhiteSpace() != true) {
                            parent.treeParent.addChild(PsiWhiteSpaceImpl(" "), parent.treeNext)
                        }
                        parent.addChild(untilNode, node)
                        parent.removeChild(node)
                    }
                }
        }
    }
}
