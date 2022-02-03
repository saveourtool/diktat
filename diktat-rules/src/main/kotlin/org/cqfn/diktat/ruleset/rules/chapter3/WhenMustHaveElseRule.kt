package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WHEN_WITHOUT_ELSE
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.appendNewlineMergingWhiteSpace
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.cqfn.diktat.ruleset.utils.hasParent
import org.cqfn.diktat.ruleset.utils.isBeginByNewline

import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.ARROW
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.ELSE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.EQ
import com.pinterest.ktlint.core.ast.ElementType.FUNCTION_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.RBRACE
import com.pinterest.ktlint.core.ast.ElementType.RETURN
import com.pinterest.ktlint.core.ast.ElementType.WHEN_ENTRY
import com.pinterest.ktlint.core.ast.prevSibling
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.java.PsiBlockStatementImpl
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
    "no-else-in-when",
    configRules,
    listOf(WHEN_WITHOUT_ELSE)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == ElementType.WHEN && isStatement(node)) {
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
        val hasConditionsIsPattern = whenEntries.any { it.hasChildOfType(ElementType.WHEN_CONDITION_IS_PATTERN) }
        if (hasConditionsIsPattern) {
            return false
        }

        val conditionsInRange = whenEntries.map {
            it.getAllChildrenWithType(ElementType.WHEN_CONDITION_IN_RANGE)
        }.flatten()

        val conditionsWithExpression = whenEntries.map {
            it.getAllChildrenWithType(ElementType.WHEN_CONDITION_WITH_EXPRESSION)
        }.flatten()

        val hasCallExpressions = (conditionsWithExpression + conditionsInRange).any { it.hasChildOfType(ElementType.CALL_EXPRESSION) }
        if (hasCallExpressions) {
            return false
        }

        val dotQualifierExpressions = conditionsWithExpression.map {
            it.getAllChildrenWithType(ElementType.DOT_QUALIFIED_EXPRESSION)
        }.flatten()

        val amountOfDifferentEnums = dotQualifierExpressions.map { it.getFirstChildWithType(ElementType.REFERENCE_EXPRESSION)?.text }
            .distinct()
            .count()

        if (amountOfDifferentEnums != 1) {
            return false
        }
        return true
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
}
