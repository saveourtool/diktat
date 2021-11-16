package org.cqfn.diktat.ruleset.rules.chapter5

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.RUN_BLOCKING_INSIDE_ASYNC
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.hasChildOfType

import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.LAMBDA_ARGUMENT
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.parent
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.psiUtil.hasSuspendModifier

/**
 * This rule finds if using runBlocking in asynchronous code
 */
class AsyncAndSyncRule(configRules: List<RulesConfig>) : DiktatRule(
    "sync-in-async",
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
        node.parent({ it.isAsync() || it.isSuspend() })?.let {
            RUN_BLOCKING_INSIDE_ASYNC.warn(configRules, emitWarn, isFixMode, node.text, node.startOffset, node)
        }
    }

    private fun ASTNode.isAsync() = this.elementType == CALL_EXPRESSION && this.findChildByType(REFERENCE_EXPRESSION)?.text in asyncList

    private fun ASTNode.isSuspend() = this.elementType == FUN && (this.psi as KtFunction).modifierList?.hasSuspendModifier() ?: false

    private fun ASTNode.isRunBlocking() = this.elementType == REFERENCE_EXPRESSION && this.text == "runBlocking" && this.treeParent.hasChildOfType(LAMBDA_ARGUMENT)
}
