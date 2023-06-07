package com.saveourtool.diktat.ruleset.rules.chapter3

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.WHEN_WITHOUT_ELSE
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.appendNewlineMergingWhiteSpace
import com.saveourtool.diktat.ruleset.utils.getAllChildrenWithType
import com.saveourtool.diktat.ruleset.utils.getFirstChildWithType
import com.saveourtool.diktat.ruleset.utils.hasChildOfType
import com.saveourtool.diktat.ruleset.utils.hasParent
import com.saveourtool.diktat.ruleset.utils.isBeginByNewline
import com.saveourtool.diktat.ruleset.utils.prevSibling

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.KtNodeTypes.BINARY_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.BLOCK
import org.jetbrains.kotlin.KtNodeTypes.DOT_QUALIFIED_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.FUNCTION_LITERAL
import org.jetbrains.kotlin.KtNodeTypes.OPERATION_REFERENCE
import org.jetbrains.kotlin.KtNodeTypes.PROPERTY
import org.jetbrains.kotlin.KtNodeTypes.REFERENCE_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.RETURN
import org.jetbrains.kotlin.KtNodeTypes.WHEN_CONDITION_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.WHEN_CONDITION_IN_RANGE
import org.jetbrains.kotlin.KtNodeTypes.WHEN_CONDITION_IS_PATTERN
import org.jetbrains.kotlin.KtNodeTypes.WHEN_ENTRY
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.java.PsiBlockStatementImpl
import org.jetbrains.kotlin.lexer.KtTokens.ARROW
import org.jetbrains.kotlin.lexer.KtTokens.ELSE_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.EOL_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.EQ
import org.jetbrains.kotlin.lexer.KtTokens.LBRACE
import org.jetbrains.kotlin.lexer.KtTokens.RBRACE
import org.jetbrains.kotlin.psi.KtWhenExpression

/**
 * Rule 3.10: 'when' statement must have else branch, unless when condition variable is enumerated or sealed type
 *
 * Current limitations and FixMe:
 * If a when statement of type enum or sealed contains all values of a enum - there is no need to have "else" branch.
 * The compiler can issue a warning when it is missing.
 */
@Suppress("ForbiddenComment")
class WhenMustHaveElseRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(WHEN_WITHOUT_ELSE)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == KtNodeTypes.WHEN && isStatement(node)) {
            checkEntries(node)
        }
    }

    private fun checkEntries(node: ASTNode) {
        if (!hasElse(node) && !isOnlyEnumEntries(node)) {
            WHEN_WITHOUT_ELSE.warnAndFix(configRules, emitWarn, isFixMode, "else was not found", node.startOffset, node) {
                val whenEntryElse = CompositeElement(WHEN_ENTRY)
                if (!node.lastChildNode.isBeginByNewline()) {
                    node.appendNewlineMergingWhiteSpace(node.lastChildNode.treePrev, node.lastChildNode)
                }
                node.addChild(whenEntryElse, node.lastChildNode)
                addChildren(whenEntryElse)
                if (!whenEntryElse.isBeginByNewline()) {
                    node.addChild(PsiWhiteSpaceImpl("\n"), whenEntryElse)
                }
            }
        }
    }

    private fun isOnlyEnumEntries(node: ASTNode): Boolean {
        val whenEntries = node.getAllChildrenWithType(WHEN_ENTRY)
        val hasConditionsIsPattern = whenEntries.any { it.hasChildOfType(WHEN_CONDITION_IS_PATTERN) }
        if (hasConditionsIsPattern) {
            return false
        }

        val conditionsInRange = whenEntries.map {
            it.getAllChildrenWithType(WHEN_CONDITION_IN_RANGE)
        }.flatten()

        val conditionsWithExpression = whenEntries.map {
            it.getAllChildrenWithType(WHEN_CONDITION_EXPRESSION)
        }.flatten()

        val areOnlyEnumEntriesWithExpressions = if (conditionsWithExpression.isNotEmpty()) {
            conditionsWithExpression.all {
                it.hasChildOfType(DOT_QUALIFIED_EXPRESSION) || it.hasChildOfType(REFERENCE_EXPRESSION)
            }
        } else {
            true
        }

        val areOnlyEnumEntriesInRanges = if (conditionsInRange.isNotEmpty()) {
            conditionsInRange.map { it.getFirstChildWithType(BINARY_EXPRESSION) }
                .all {
                    val dotExpressionsCount = it?.getAllChildrenWithType(DOT_QUALIFIED_EXPRESSION)?.size ?: 0
                    val referenceExpressionsCount = it?.getAllChildrenWithType(REFERENCE_EXPRESSION)?.size ?: 0
                    dotExpressionsCount + referenceExpressionsCount == 2
                }
        } else {
            true
        }

        if (areOnlyEnumEntriesWithExpressions && areOnlyEnumEntriesInRanges) {
            return true
        }
        return false
    }

    private fun isStatement(node: ASTNode): Boolean {
        // Checks if there is return before when
        if (node.hasParent(RETURN)) {
            return false
        }

        // Checks if `when` is the last statement in lambda body
        if (node.treeParent.elementType == BLOCK && node.treeParent.treeParent.elementType == FUNCTION_LITERAL &&
                node.treeParent.lastChildNode == node) {
            return false
        }

        if (node.treeParent.elementType == WHEN_ENTRY && node.prevSibling { it.elementType == ARROW } != null) {
            // `when` is used as a branch in another `when`
            return false
        }

        return node.prevSibling { it.elementType == EQ || it.elementType == OPERATION_REFERENCE && it.firstChildNode.elementType == EQ }
            ?.let {
                // `when` is used in an assignment or in a function with expression body
                false
            }
            ?: !node.hasParent(PROPERTY)
    }

    /**
     * Check if this `when` has `else` branch. If `else` branch is empty, `(node.psi as KtWhenExpression).elseExpression` returns `null`,
     * so we need to manually check if any entry contains `else` keyword.
     */
    private fun hasElse(node: ASTNode): Boolean = (node.psi as KtWhenExpression).entries.any { it.isElse }

    private fun addChildren(node: ASTNode) {
        val block = PsiBlockStatementImpl()

        node.apply {
            addChild(LeafPsiElement(ELSE_KEYWORD, "else"), null)
            addChild(PsiWhiteSpaceImpl(" "), null)
            addChild(LeafPsiElement(ARROW, "->"), null)
            addChild(PsiWhiteSpaceImpl(" "), null)
            addChild(block, null)
        }

        block.apply {
            addChild(LeafPsiElement(LBRACE, "{"), null)
            addChild(PsiWhiteSpaceImpl("\n"), null)
            addChild(LeafPsiElement(EOL_COMMENT, "// this is a generated else block"), null)
            addChild(PsiWhiteSpaceImpl("\n"), null)
            addChild(LeafPsiElement(RBRACE, "}"), null)
        }
    }

    companion object {
        const val NAME_ID = "no-else-in-when"
    }
}
