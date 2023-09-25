package com.saveourtool.diktat.ruleset.rules.chapter3

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.DiktatRule

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.lexer.KtTokens

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
        checkPrintln(node)
        checkJsConsole(node)
    }

    // check kotlin.io.print()/kotlin.io.println()
    private fun checkPrintln(node: ASTNode) {
        if (node.elementType == KtNodeTypes.CALL_EXPRESSION) {
            val referenceExpression = node.findChildByType(KtNodeTypes.REFERENCE_EXPRESSION)?.text
            val valueArgumentList = node.findChildByType(KtNodeTypes.VALUE_ARGUMENT_LIST)
            if (referenceExpression in setOf("print", "println") &&
                    node.treePrev?.elementType != KtTokens.DOT &&
                    valueArgumentList?.getChildren(TokenSet.create(KtNodeTypes.VALUE_ARGUMENT))?.size?.let { it <= 1 } == true &&
                    node.findChildByType(KtNodeTypes.LAMBDA_ARGUMENT) == null) {
                Warnings.DEBUG_PRINT.warn(
                    configRules, emitWarn,
                    "found $referenceExpression()", node.startOffset, node,
                )
            }
        }
    }

    // check kotlin.js.console.*()
    private fun checkJsConsole(node: ASTNode) {
        if (node.elementType == KtNodeTypes.DOT_QUALIFIED_EXPRESSION) {
            val isConsole = node.firstChildNode.let { referenceExpression ->
                referenceExpression.elementType == KtNodeTypes.REFERENCE_EXPRESSION &&
                        referenceExpression.firstChildNode.let { it.elementType == KtTokens.IDENTIFIER && it.text == "console" }
            }
            if (isConsole) {
                val logMethod = node.lastChildNode
                    .takeIf { it.elementType == KtNodeTypes.CALL_EXPRESSION }
                    ?.takeIf { it.findChildByType(KtNodeTypes.LAMBDA_ARGUMENT) == null }
                    ?.firstChildNode
                    ?.takeIf { it.elementType == KtNodeTypes.REFERENCE_EXPRESSION }
                    ?.text
                if (logMethod in setOf("error", "info", "log", "warn")) {
                    Warnings.DEBUG_PRINT.warn(
                        configRules, emitWarn,
                        "found console.$logMethod()", node.startOffset, node,
                    )
                }
            }
        }
    }

    internal companion object {
        const val NAME_ID = "debug-print"
    }
}
