package com.saveourtool.diktat.ruleset.rules.chapter6

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.RUN_IN_SCRIPT
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.*
import com.saveourtool.diktat.util.isKotlinScript

import org.jetbrains.kotlin.KtNodeTypes.CALL_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.DOT_QUALIFIED_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.LAMBDA_ARGUMENT
import org.jetbrains.kotlin.KtNodeTypes.LAMBDA_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.PARENTHESIZED
import org.jetbrains.kotlin.KtNodeTypes.SCRIPT_INITIALIZER
import org.jetbrains.kotlin.KtNodeTypes.VALUE_ARGUMENT
import org.jetbrains.kotlin.KtNodeTypes.VALUE_ARGUMENT_LIST
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement

/**
 * Rule that checks if kts script contains other functions except run code
 * In .kts files allow use only property declaration, function, classes, and code inside `run` block
 * In gradle.kts files allow to call binary expression with EQ, expression and dot qualified expression in addition to everything used in .kts files
 */
class RunInScript(
    configRules: List<RulesConfig>,
) : DiktatRule(
    id = NAME_ID,
    configRules = configRules,
    inspections = listOf(RUN_IN_SCRIPT),
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == SCRIPT_INITIALIZER && node.getFilePath().isKotlinScript()) {
            if (node.getFilePath().isGradleScript()) {
                checkGradleNode(node)
            } else {
                checkScript(node)
            }
        }
    }

    private fun checkGradleNode(node: ASTNode) {
        val astNode = if (node.hasEqBinaryExpression()) {
            return
        } else {
            when (node.firstChildNode.elementType) {
                PARENTHESIZED -> node.firstChildNode
                else -> node
            }
        }
        if (!astNode.hasChildOfType(CALL_EXPRESSION) && !astNode.hasChildOfType(DOT_QUALIFIED_EXPRESSION)) {
            warnRunInScript(astNode)
        }
    }

    private fun checkScript(node: ASTNode) {
        val isLambdaArgument = node.firstChildNode.hasChildOfType(LAMBDA_ARGUMENT)
        val isLambdaInsideValueArgument = node.firstChildNode
            .findChildByType(VALUE_ARGUMENT_LIST)
            ?.findChildByType(VALUE_ARGUMENT)
            ?.findChildByType(LAMBDA_EXPRESSION) != null
        if (!isLambdaArgument && !isLambdaInsideValueArgument) {
            warnRunInScript(node)
        }
    }

    private fun warnRunInScript(node: ASTNode) {
        RUN_IN_SCRIPT.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
            if (node.firstChildNode.elementType != DOT_QUALIFIED_EXPRESSION) {
                val parent = node.treeParent
                val newNode = KotlinParser().createNode("run {\n ${node.text}\n} \n")
                val newScript = CompositeElement(SCRIPT_INITIALIZER)
                parent.addChild(newScript, node)
                newScript.addChild(newNode)
                parent.removeChild(node)
            }
        }
    }

    companion object {
        const val NAME_ID = "run-script"
    }
}
