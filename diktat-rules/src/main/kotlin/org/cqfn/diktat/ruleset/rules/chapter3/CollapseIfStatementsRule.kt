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
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.utils.getBodyLines
import org.cqfn.diktat.ruleset.utils.prettyPrint
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtClassBody
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
    private val configuration by lazy {
        CollapseIfStatementsConfiguration(
            configRules.getRuleConfig(Warnings.COLLAPSE_IF_STATEMENTS)?.configuration ?: emptyMap()
        )
    }
    override fun logic(node: ASTNode) {
        if (node.elementType == IF) {
            //println("Before\n ${node.text}")
            val startCollapseFromLevel = configuration.startCollapseFromNestedLevel
            val thenNode = (node.psi as KtIfExpression).then?.node
            val nestedIfNode = thenNode?.findChildByType(IF) ?: return
            // We monitor which types of nodes are followed before nested `if`
            // and we allow only a limited number of types to pass through.
            // Otherwise discovered `if` it is not nested
            val listOfNodesBeforeNestedIf = thenNode.getChildren(null).takeWhile { it.elementType != IF }
            val allowedTypes = listOf(LBRACE, WHITE_SPACE, RBRACE, KDOC, BLOCK_COMMENT, EOL_COMMENT)
            if (listOfNodesBeforeNestedIf.any { it.elementType !in allowedTypes }) {
                return
            }

            Warnings.COLLAPSE_IF_STATEMENTS.warnAndFix(configRules, emitWarn, true,
                "avoid using redundant nested if-statements", nestedIfNode.startOffset, nestedIfNode) {
                collapse(node, nestedIfNode)
            }
            //println("----------\nAfter\n ${node.text}")
        }
    }

    private fun collapse(parentNode : ASTNode, nestedNode : ASTNode) {
        val parentConditionNode = parentNode.findChildByType(CONDITION)!!
        val nestedConditionNode = nestedNode.findChildByType(CONDITION)!!
        val unionCondition = "${parentConditionNode.text} && ${nestedConditionNode.text}"
        val newParentConditionNode = KotlinParser().createNode(unionCondition)
        parentNode.replaceChild(parentConditionNode, newParentConditionNode)

        // TODO: fix collapsing of `THEN` parts
        //val nestedThenNode = nestedNode.findChildByType(THEN)!!
        val nestedThenNode = (nestedNode.psi as KtIfExpression).then
        nestedThenNode?.node?.removeChild(nestedThenNode.node!!.firstChildNode) // remove {
        nestedThenNode?.node?.removeChild(nestedThenNode.node!!.lastChildNode) // remove }
        val newNestedText = (nestedThenNode as KtBlockExpression).statements.joinToString("\n") { it.text }
        val newNestedThenNode = KotlinParser().createNode(newNestedText)

    }

    /**
     * [RuleConfiguration] for configuration
     */
    class CollapseIfStatementsConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val startCollapseFromNestedLevel = config["startCollapseFromNestedLevel"]?.toInt() ?: DEFAULT_NESTED_LEVEL
    }

    companion object {
        private const val DEFAULT_NESTED_LEVEL = 2
    }
}
