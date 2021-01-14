package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.RUN_BLOCKING_INSIDE_ASYNC

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.parent
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.psiUtil.hasSuspendModifier

class AsyncAndSyncRule(private val configRules: List<RulesConfig>) : Rule("sync-in-async") {
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType
    private val asyncList = listOf("async", "launch")

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.isRunBlocking())
            checkRunBlocking(node)
    }

    private fun checkRunBlocking(node: ASTNode) {
        if (node.parent({it.isAsync() || it.isSuspend()}) != null) {
            RUN_BLOCKING_INSIDE_ASYNC.warn(configRules, emitWarn, isFixMode, node.text, node.startOffset, node)
        }
    }

    private fun ASTNode.isAsync() = this.elementType == CALL_EXPRESSION && this.findChildByType(REFERENCE_EXPRESSION)?.text in asyncList

    private fun ASTNode.isSuspend() = this.elementType == FUN && (this.psi as KtFunction).modifierList?.hasSuspendModifier() ?: false

    private fun ASTNode.isRunBlocking() = this.elementType == REFERENCE_EXPRESSION && this.text == "runBlocking"
}

