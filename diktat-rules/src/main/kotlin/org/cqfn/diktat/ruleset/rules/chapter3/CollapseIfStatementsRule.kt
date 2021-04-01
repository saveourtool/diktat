package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.KotlinParser

import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.IF
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.RBRACE
import com.pinterest.ktlint.core.ast.ElementType.THEN
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtBlockExpression
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
            process(node)
        }
    }

    private fun process(node: ASTNode)  {
        val startCollapseFromLevel = configuration.startCollapseFromNestedLevel

        var currNode = findNestedIf(node) ?: return
        var nestedLevel = 2
        var nestedIfNode :ASTNode?
        while (true) {
            nestedIfNode = findNestedIf(currNode)
            if (nestedIfNode != null) {
                nestedLevel++
                currNode = nestedIfNode
            } else {
                break
            }
        }
        if (nestedLevel < startCollapseFromLevel) {
            return
        }
        // Since the external `if` statement is not the direct parent,
        // we need multiple steps to take the required one
        // BLOCK -> THEN -> IF
        var currParentNode = currNode.treeParent.treeParent.treeParent
        // Now collapse in reverse order
        while (nestedLevel != 1 && isFixMode) {
            Warnings.COLLAPSE_IF_STATEMENTS.warnAndFix(
                configRules, emitWarn, isFixMode,
                "avoid using redundant nested if-statements", currNode.startOffset, currNode
            ) {
                collapse(currParentNode, currNode)
                currNode = currParentNode
                currParentNode = currParentNode.treeParent.treeParent.treeParent
                nestedLevel--
            }
        }
    }

    private fun findNestedIf(parentNode: ASTNode) : ASTNode? {
        val parentThenNode = (parentNode.psi as KtIfExpression).then?.node
        val nestedIfNode = parentThenNode?.findChildByType(IF) ?: return null
        // We monitor which types of nodes are followed before nested `if`
        // and we allow only a limited number of types to pass through.
        // Otherwise discovered `if` it is not nested
        val listOfNodesBeforeNestedIf = parentThenNode.getChildren(null).takeWhile { it.elementType != IF }
        val allowedTypes = listOf(LBRACE, WHITE_SPACE, RBRACE, KDOC, BLOCK_COMMENT, EOL_COMMENT)
        if (listOfNodesBeforeNestedIf.any { it.elementType !in allowedTypes }) {
            return null
        }
        return nestedIfNode
    }

    private fun collapse(parentNode : ASTNode, nestedNode : ASTNode) {
        // Merge parent and nested conditions
        val parentCondition = (parentNode.psi as KtIfExpression).condition?.text
        val nestedCondition = (nestedNode.psi as KtIfExpression).condition?.text
        val mergeCondition = "if ($parentCondition && $nestedCondition) {}"
        val newParentIfNode = KotlinParser().createNode(mergeCondition)
        // Remove THEN block
        newParentIfNode.removeChild(newParentIfNode.lastChildNode)
        // Remove old `if` from parent
        parentNode.removeRange(parentNode.firstChildNode, parentNode.findChildByType(THEN)!!)
        // Add to parent all child from new `if` node
        var addAfter = parentNode.firstChildNode
        newParentIfNode.getChildren(null).forEachIndexed {
            index, child ->
            parentNode.addChild(child, addAfter)
            var shift = index + 1
            addAfter = parentNode.firstChildNode
            while (shift > 0) {
                addAfter = addAfter.treeNext
                shift--
            }
        }
        // Merge parent and nested `THEN` blocks
        val nestedThenNode = (nestedNode.psi as KtIfExpression).then
        val nestedThenText = (nestedThenNode as KtBlockExpression).statements.joinToString("\n") { it.text }
        val newNestedNode = KotlinParser().createNode(nestedThenText)
        val parentThenNode = (parentNode.psi as KtIfExpression).then?.node
        parentThenNode?.replaceChild(nestedNode, newNestedNode)
    }

    /**
     * [RuleConfiguration] for configuration
     */
    class CollapseIfStatementsConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         *  Collapse statements only if nested level more than this value
         */
        val startCollapseFromNestedLevel = config["startCollapseFromNestedLevel"]?.toInt() ?: DEFAULT_NESTED_LEVEL
    }

    companion object {
        private const val DEFAULT_NESTED_LEVEL = 2
    }
}
