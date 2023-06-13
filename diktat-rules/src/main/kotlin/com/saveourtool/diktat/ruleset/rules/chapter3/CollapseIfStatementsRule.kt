package com.saveourtool.diktat.ruleset.rules.chapter3

import com.saveourtool.diktat.common.config.rules.RuleConfiguration
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getRuleConfig
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.KotlinParser

import org.jetbrains.kotlin.KtNodeTypes.BINARY_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.IF
import org.jetbrains.kotlin.KtNodeTypes.OPERATION_REFERENCE
import org.jetbrains.kotlin.KtNodeTypes.THEN
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens.BLOCK_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.EOL_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.LBRACE
import org.jetbrains.kotlin.lexer.KtTokens.LPAR
import org.jetbrains.kotlin.lexer.KtTokens.RBRACE
import org.jetbrains.kotlin.lexer.KtTokens.RPAR
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.psiUtil.children

import java.util.Stack

typealias PlaceOfWarningForCurrentNode = Pair<Int, ASTNode>

/**
 * Rule for redundant nested if-statements, which could be collapsed into a single one
 */
class CollapseIfStatementsRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
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

    // We hold the warnings, which we raised, since in case of multi nested if-statement,
    // there are could be several identical warning for one line
    private val listOfWarnings: MutableSet<PlaceOfWarningForCurrentNode> = mutableSetOf()

    override fun logic(node: ASTNode) {
        if (node.elementType == IF) {
            process(node)
        }
    }

    private fun process(node: ASTNode) {
        val startCollapseFromLevel = configuration.startCollapseFromNestedLevel
        val listOfNestedNodes: Stack<ASTNode> = Stack()

        var nestedIfNode = findNestedIf(node)
        while (nestedIfNode != null) {
            listOfNestedNodes.push(nestedIfNode)
            nestedIfNode = findNestedIf(nestedIfNode)
        }
        val nestedLevel = listOfNestedNodes.size + 1
        if (nestedLevel < startCollapseFromLevel) {
            return
        }
        while (listOfNestedNodes.isNotEmpty()) {
            val currNode = listOfNestedNodes.pop()
            // Since the external `if` statement is not the direct parent,
            // we need multiple steps to take the required one
            // BLOCK -> THEN -> IF
            val currParentNode = currNode.treeParent.treeParent.treeParent
            if (listOfWarnings.add(currNode.startOffset to currNode)) {
                Warnings.COLLAPSE_IF_STATEMENTS.warnAndFix(
                    configRules, emitWarn, isFixMode,
                    "avoid using redundant nested if-statements", currNode.startOffset, currNode
                ) {
                    collapse(currParentNode, currNode)
                }
            }
        }
    }

    private fun findNestedIf(parentNode: ASTNode): ASTNode? {
        val parentThenNode = (parentNode.psi as KtIfExpression).then?.node ?: return null
        val nestedIfNode = parentThenNode.findChildByType(IF) ?: return null
        // We won't collapse if-statements, if some of them have `else` node
        if ((parentNode.psi as KtIfExpression).`else` != null ||
                (nestedIfNode.psi as KtIfExpression).`else` != null) {
            return null
        }
        // We monitor which types of nodes are followed before and after nested `if`
        // and we allow only a limited number of types to pass through.
        // Otherwise discovered `if` is not nested
        // We don't expect KDOC in `if-statements`, since it's a bad practise, and such code by meaning of our
        // code analyzer is invalid
        // However, if in some case we will hit the KDOC, than we won't collapse statements
        val listOfNodesBeforeNestedIf = parentThenNode.getChildren(null).takeWhile { it.elementType != IF }
        val listOfNodesAfterNestedIf = parentThenNode.getChildren(null).takeLastWhile { it != parentThenNode.findChildByType(IF) }
        val allowedTypes = listOf(LBRACE, WHITE_SPACE, RBRACE, BLOCK_COMMENT, EOL_COMMENT)
        if (listOfNodesBeforeNestedIf.any { it.elementType !in allowedTypes } ||
                listOfNodesAfterNestedIf.any { it.elementType !in allowedTypes }) {
            return null
        }
        return nestedIfNode
    }

    private fun takeCommentsBeforeNestedIf(node: ASTNode): List<ASTNode> {
        val thenNode = (node.psi as KtIfExpression).then?.node
        return thenNode?.children()
            ?.takeWhile { it.elementType != IF }
            ?.filter {
                it.elementType == EOL_COMMENT || it.elementType == BLOCK_COMMENT
            }
            ?.toList() ?: emptyList()
    }

    private fun collapse(parentNode: ASTNode, nestedNode: ASTNode) {
        collapseConditions(parentNode, nestedNode)
        collapseThenBlocks(parentNode, nestedNode)
    }

    private fun collapseConditions(parentNode: ASTNode, nestedNode: ASTNode) {
        // If there are comments before nested if, we will move them into parent condition
        val comments = takeCommentsBeforeNestedIf(parentNode)
        val commentsText = if (comments.isNotEmpty()) {
            comments.joinToString(prefix = "\n", postfix = "\n", separator = "\n") { it.text }
        } else {
            " "
        }
        // Merge parent and nested conditions
        val parentConditionText = extractConditions(parentNode)
        val nestedCondition = (nestedNode.psi as KtIfExpression).condition
        val nestedConditionText = extractConditions(nestedNode)
        // If the nested condition is compound,
        // we need to put it to the brackets, according algebra of logic
        val mergeCondition =
            if (nestedCondition?.node?.elementType == BINARY_EXPRESSION &&
                    nestedCondition?.node?.findChildByType(OPERATION_REFERENCE)?.text == "||"
            ) {
                "if ($parentConditionText &&$commentsText($nestedConditionText)) {}"
            } else {
                "if ($parentConditionText &&$commentsText$nestedConditionText) {}"
            }
        val newParentIfNode = KotlinParser().createNode(mergeCondition)
        // Remove THEN block
        newParentIfNode.removeChild(newParentIfNode.lastChildNode)
        // Remove old `if` from parent
        parentNode.removeRange(parentNode.firstChildNode, parentNode.findChildByType(THEN))
        // Add to parent all child from new `if` node
        var addAfter = parentNode.firstChildNode
        newParentIfNode.getChildren(null).forEachIndexed { index, child ->
            parentNode.addChild(child, addAfter)
            addAfter = parentNode.children().drop(index + 1).first()
        }
    }

    // If condition contains comments, we need additional actions
    // Because of `node.condition` will ignore comments
    private fun extractConditions(node: ASTNode): String {
        val condition = node.getChildren(null)
            .takeLastWhile { it != node.findChildByType(LPAR) }
            .takeWhile { it != node.findChildByType(RPAR) }
        return condition.joinToString("") { it.text }
    }

    private fun collapseThenBlocks(parentNode: ASTNode, nestedNode: ASTNode) {
        // Remove comments from parent node, since we already moved them into parent condition
        val comments = takeCommentsBeforeNestedIf(parentNode)
        comments.forEach {
            if (it.treeNext.elementType == WHITE_SPACE &&
                    it.treePrev.elementType == WHITE_SPACE) {
                parentNode.removeChild(it.treePrev)
            }
            parentNode.removeChild(it)
        }
        // Merge parent and nested `THEN` blocks
        val nestedThenNode = (nestedNode.psi as KtIfExpression).then
        val nestedContent = (nestedThenNode as KtBlockExpression).children().toMutableList()
        // Remove {, }, and white spaces
        repeat(2) {
            val firstElType = nestedContent.first().elementType
            if (firstElType == WHITE_SPACE ||
                    firstElType == LBRACE) {
                nestedContent.removeFirst()
            }
            val lastElType = nestedContent.last().elementType
            if (lastElType == WHITE_SPACE ||
                    lastElType == RBRACE) {
                nestedContent.removeLast()
            }
        }
        val nestedThenText = nestedContent.joinToString("") { it.text }
        val newNestedNode = KotlinParser().createNode(nestedThenText).treeParent
        val parentThenNode = (parentNode.psi as KtIfExpression).then?.node
        newNestedNode.getChildren(null).forEach {
            parentThenNode?.addChild(it, nestedNode)
        }
        parentThenNode?.removeChild(nestedNode)
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
        const val NAME_ID = "collapse-if"
    }
}
