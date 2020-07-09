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
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.WHEN
import com.pinterest.ktlint.core.ast.ElementType.WHILE
import com.pinterest.ktlint.core.ast.prevSibling
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.NO_BRACES_IN_CONDITIONALS_AND_LOOPS
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtDoWhileExpression
import org.jetbrains.kotlin.psi.KtForExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.psi.KtWhileExpression

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
            FOR -> checkLoop(node)
            DO_WHILE -> checkLoop(node)
            WHILE -> checkLoop(node)
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
        val elseNode = ifPsi.`else`?.node

        // check if it is a single-line `if` statement
        if (node.treeParent.let { it.elementType != IF && it.elementType != ELSE }) {
            val hasSingleElse = elseNode != null && elseNode?.elementType != IF
            if (hasSingleElse && node.text.lines().size == 1) return
        }

        val hasBraceInThen = thenNode?.elementType == BLOCK && thenNode.firstChildNode.elementType == LBRACE
        if (!hasBraceInThen) {
            NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnAndFix(configRules, emitWarn, isFixMode, "IF",
                    (thenNode?.prevSibling { it.elementType == IF_KEYWORD } ?: node).startOffset) {
                // todo
            }
        }

        if (elseNode != null && elseNode.elementType != IF) {
            val hasBraceInElseBlock = elseNode.elementType == BLOCK && elseNode.firstChildNode.elementType == LBRACE
            if (!hasBraceInElseBlock) {
                NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnAndFix(configRules, emitWarn, isFixMode, "ELSE",
                        (elseNode.treeParent.prevSibling { it.elementType == ELSE_KEYWORD } ?: node).startOffset) {
                    // todo
                }
            }
        }
    }

    private fun checkLoop(node: ASTNode) {
        require(node.elementType in arrayOf(FOR, WHILE, DO_WHILE))
        val loopBodyNode = when (node.elementType) {
            FOR -> (node.psi as KtForExpression).body?.node
            WHILE -> (node.psi as KtWhileExpression).body?.node
            DO_WHILE -> (node.psi as KtDoWhileExpression).body?.node
            else -> error("Invalid element type ${node.elementType}")
        }
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
