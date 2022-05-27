package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DiktatRule

import com.pinterest.ktlint.core.ast.ElementType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet

/**
 * This rule detects `print()` or `println()`.
 * Assumption that it's a debug logging
 *
 */
class DebugPrintRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(Warnings.DEBUG_PRINT)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == ElementType.CALL_EXPRESSION) {
            val referenceExpression = node.findChildByType(ElementType.REFERENCE_EXPRESSION)?.text
            val valueArgumentList = node.findChildByType(ElementType.VALUE_ARGUMENT_LIST)
            if (referenceExpression in setOf("print", "println") &&
                node.treePrev?.elementType != ElementType.DOT &&
                valueArgumentList?.getChildren(TokenSet.create(ElementType.VALUE_ARGUMENT))?.size == 1 &&
                node.findChildByType(ElementType.LAMBDA_ARGUMENT) == null) {
                Warnings.DEBUG_PRINT.warn(
                    configRules, emitWarn, isFixMode,
                    "found $referenceExpression()", node.startOffset, node,
                )
            }
        }
    }

    internal companion object {
        const val NAME_ID = "debug-print"
    }
}
