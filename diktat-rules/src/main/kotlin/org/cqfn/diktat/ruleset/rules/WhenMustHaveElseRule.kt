package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
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
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.utils.appendNewlineMergingWhiteSpace
import org.cqfn.diktat.ruleset.utils.hasParent
import org.cqfn.diktat.ruleset.utils.isBeginByNewline
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
class WhenMustHaveElseRule(private val configRules: List<RulesConfig>) : Rule("no-else-in-when") {

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == ElementType.WHEN && isStatement(node)) {
            checkEntries(node)
        }
    }


    //FixMe: If a when statement of type enum or sealed contains all values of a enum - there is no need to have "else" branch.

    private fun checkEntries(node: ASTNode) {
        if (!hasElse(node)) {
            Warnings.WHEN_WITHOUT_ELSE.warnAndFix(configRules, emitWarn, isFixMode, "else was not found", node.startOffset, node) {
                val whenEntryElse = CompositeElement(WHEN_ENTRY)
                node.appendNewlineMergingWhiteSpace(node.lastChildNode.treePrev, node.lastChildNode.treePrev)
                node.addChild(whenEntryElse, node.lastChildNode)
                addChildren(whenEntryElse)
                if(!whenEntryElse.isBeginByNewline()) {
                    node.addChild(PsiWhiteSpaceImpl("\n"), whenEntryElse)
                }
            }
        }
    }

    private fun isStatement(node: ASTNode) : Boolean {
        // Checks if there is return before when
        if (node.hasParent(RETURN)) {
            return false
        }

        // Checks if `when` is the last statement in lambda body
        if (node.treeParent.elementType == BLOCK && node.treeParent.treeParent.elementType == FUNCTION_LITERAL) {
            if (node.treeParent.lastChildNode == node) {
                return false
            }
        }

        if (node.treeParent.elementType == WHEN_ENTRY && node.prevSibling { it.elementType == ARROW } != null) {
            // `when` is used as a branch in another `when`
            return false
        }

        if (node.prevSibling { it.elementType == EQ || it.elementType == OPERATION_REFERENCE && it.firstChildNode.elementType == EQ } != null) {
            // `when` is used in an assignment or in a function with expression body
            return false
        } else {
            return !node.hasParent(PROPERTY)
        }
    }

    private fun hasElse(node: ASTNode): Boolean = (node.psi as KtWhenExpression).elseExpression != null

    private fun addChildren(node: ASTNode) {
        val block = PsiBlockStatementImpl()

        node.apply {
            addChild(LeafPsiElement(ELSE_KEYWORD, "else"), null)
            addChild(PsiWhiteSpaceImpl(" "), null)
            addChild(LeafPsiElement(ARROW, "->"), null)
            addChild(PsiWhiteSpaceImpl(" "), null)
            addChild(block, null)
        }


        block.apply{
            addChild(LeafPsiElement(LBRACE, "{"), null)
            addChild(PsiWhiteSpaceImpl("\n"),null)
            addChild(LeafPsiElement(EOL_COMMENT, "// this is a generated else block"),null)
            addChild(PsiWhiteSpaceImpl("\n"),null)
            addChild(LeafPsiElement(RBRACE, "}"), null)
        }

    }
}
