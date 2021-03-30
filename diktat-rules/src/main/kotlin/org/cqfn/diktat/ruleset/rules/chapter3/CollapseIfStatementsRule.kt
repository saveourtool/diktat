package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.KotlinParser

import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.CONDITION
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.IF
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.RBRACE
import com.pinterest.ktlint.core.ast.ElementType.THEN
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtIfExpression

/**
 * Rule for redundant nested if-statements, which could be collapsed into a single one
 */
class CollapseIfStatementsRule(configRules: List<RulesConfig>) : DiktatRule(
    "collapse-if",
    configRules,
    listOf(
        Warnings.COLLAPSE_IF_STATEMENTS
    )
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == IF) {
            val thenNode = (node.psi as KtIfExpression).then?.node
            val nestedIf = thenNode?.findChildByType(IF) ?: return
            // We monitor which types of nodes are followed before nested `if`
            // and we allow only a limited number of types to pass through.
            // Otherwise discovered `if` it is not nested
            val listOfNodesBeforeNestedIf = thenNode.getChildren(null).takeWhile { it.elementType != IF }
            val allowedTypes = listOf(LBRACE, WHITE_SPACE, RBRACE, KDOC, BLOCK_COMMENT, EOL_COMMENT)
            if (listOfNodesBeforeNestedIf.any { it.elementType !in allowedTypes }) {
                return
            }

            Warnings.COLLAPSE_IF_STATEMENTS.warnAndFix(configRules, emitWarn, true,
                "avoid using redundant nested if-statements", nestedIf.startOffset, nestedIf) {
                val parentConditionNode = node.findChildByType(CONDITION)!!
                val parentThenNode = node.findChildByType(THEN)!!
                val nestedConditionNode = nestedIf.findChildByType(CONDITION)!!
                val nestedThenNode = nestedIf.findChildByType(THEN)!!

                val unionCondition = "${parentConditionNode.text} && ${nestedConditionNode.text}"
                val newParentConditionNode = KotlinParser().createNode(unionCondition)

                node.replaceChild(parentConditionNode, newParentConditionNode)
                node.replaceChild(parentThenNode, nestedThenNode)
            }
        }
    }
}
