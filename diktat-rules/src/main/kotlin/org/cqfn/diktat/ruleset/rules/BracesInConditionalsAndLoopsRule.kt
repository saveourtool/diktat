package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.DO_WHILE
import com.pinterest.ktlint.core.ast.ElementType.ELSE
import com.pinterest.ktlint.core.ast.ElementType.ELSE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.FOR
import com.pinterest.ktlint.core.ast.ElementType.IF
import com.pinterest.ktlint.core.ast.ElementType.IF_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.WHEN
import com.pinterest.ktlint.core.ast.ElementType.WHILE
import com.pinterest.ktlint.core.ast.prevSibling
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.NO_BRACES_IN_CONDITIONALS_AND_LOOPS
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtLoopExpression
import org.jetbrains.kotlin.psi.KtWhenExpression

class BracesInConditionalsAndLoopsRule : Rule("braces-rule") {

    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        configRules = params.getDiktatConfigRules()
        emitWarn = emit
        isFixMode = autoCorrect

        when (node.elementType) {
            IF -> checkIfNode(node)
            WHEN -> checkWhenBranches(node)
            FOR, WHILE, DO_WHILE -> checkLoop(node)
        }
    }

    /**
     * Check braces in if-else statements. Check for both IF and ELSE needs to be done in one method to discover single-line if-else statements correctly.
     * There is KtIfExpression class which can be used to access `then` and `else` body.
     */
    private fun checkIfNode(node: ASTNode) {
        require(node.elementType == IF)

        val ifPsi = node.psi as KtIfExpression
        val thenNode = ifPsi.then?.node
        val hasElseBranch = ifPsi.elseKeyword != null
        val elseNode = ifPsi.`else`?.node

        if (isSingleLineIfElse(node, elseNode)) return

        if (thenNode?.elementType != BLOCK) {
            NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnAndFix(configRules, emitWarn, isFixMode, "IF",
                    (thenNode?.prevSibling { it.elementType == IF_KEYWORD } ?: node).startOffset) {
                // todo
            }
        }

        if (hasElseBranch && elseNode?.elementType != IF && elseNode?.elementType != BLOCK) {
            NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnAndFix(configRules, emitWarn, isFixMode, "ELSE",
                    (elseNode?.treeParent?.prevSibling { it.elementType == ELSE_KEYWORD } ?: node).startOffset) {
                // todo
            }
        }
    }

    private fun isSingleLineIfElse(node: ASTNode, elseNode: ASTNode?): Boolean {
        val hasSingleElse = elseNode != null && elseNode.elementType != IF
        return node.treeParent.elementType != ELSE && hasSingleElse && node.text.lines().size == 1
    }

    private fun checkLoop(node: ASTNode) {
        require(node.elementType in arrayOf(FOR, WHILE, DO_WHILE))
        val loopBodyNode = (node.psi as KtLoopExpression).body?.node
        if (loopBodyNode == null || loopBodyNode.elementType != BLOCK) {
            NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnAndFix(configRules, emitWarn, isFixMode, node.elementType.toString(), node.startOffset) {
                // todo
            }
        }
    }

    private fun checkWhenBranches(node: ASTNode) {
        require(node.elementType == WHEN)
        (node.psi as KtWhenExpression).entries.forEach {
            if (it.expression?.node?.elementType == BLOCK && it.expression?.text?.lines()?.size == 1) {
                NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnAndFix(configRules, emitWarn, isFixMode, "WHEN", it.node.startOffset) {
                    // todo
                }
            }
        }
    }
}
