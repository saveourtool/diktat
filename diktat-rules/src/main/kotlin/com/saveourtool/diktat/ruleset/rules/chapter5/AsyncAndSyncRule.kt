package com.saveourtool.diktat.ruleset.rules.chapter5

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.RUN_BLOCKING_INSIDE_ASYNC
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.hasChildOfType
import com.saveourtool.diktat.ruleset.utils.parent

import org.jetbrains.kotlin.KtNodeTypes.CALL_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.LAMBDA_ARGUMENT
import org.jetbrains.kotlin.KtNodeTypes.REFERENCE_EXPRESSION
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.psiUtil.hasSuspendModifier

/**
 * This rule finds if using runBlocking in asynchronous code
 */
class AsyncAndSyncRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(RUN_BLOCKING_INSIDE_ASYNC)
) {
    private val asyncList = listOf("async", "launch")

    override fun logic(node: ASTNode) {
        if (node.isRunBlocking()) {
            checkRunBlocking(node)
        }
    }

    private fun checkRunBlocking(node: ASTNode) {
        node.parent { it.isAsync() || it.isSuspend() }?.let {
            RUN_BLOCKING_INSIDE_ASYNC.warn(configRules, emitWarn, node.text, node.startOffset, node)
        }
    }

    private fun ASTNode.isAsync() = this.elementType == CALL_EXPRESSION && this.findChildByType(REFERENCE_EXPRESSION)?.text in asyncList

    private fun ASTNode.isSuspend() = this.elementType == FUN && (this.psi as KtFunction).modifierList?.hasSuspendModifier() ?: false

    private fun ASTNode.isRunBlocking() = this.elementType == REFERENCE_EXPRESSION && this.text == "runBlocking" && this.treeParent.hasChildOfType(LAMBDA_ARGUMENT)

    companion object {
        const val NAME_ID = "sync-in-async"
    }
}
