package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.ANNOTATION_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.PRIMARY_CONSTRUCTOR
import com.pinterest.ktlint.core.ast.ElementType.SECONDARY_CONSTRUCTOR
import com.pinterest.ktlint.core.ast.isWhiteSpace
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.cqfn.diktat.ruleset.utils.isBeginByNewline
import org.cqfn.diktat.ruleset.utils.isFollowedByNewline
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl

/**
 * This rule makes each annotation applied to a class, method or constructor is on its own line. Except: if first annotation of constructor, class or method
 */
class AnnotationNewLineRule(private val configRules: List<RulesConfig>) : Rule("annotation-new-line") {

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        when (node.elementType) {
            CLASS, FUN, PRIMARY_CONSTRUCTOR, SECONDARY_CONSTRUCTOR -> checkAnnotation(node)
        }
    }


    private fun checkAnnotation(node: ASTNode) {
        if (node.hasChildOfType(MODIFIER_LIST)) {
            val modList = node.findChildByType(MODIFIER_LIST)
            fixAnnotation(modList!!)
        }
    }

    private fun fixAnnotation(node: ASTNode) {
        if (node.getAllChildrenWithType(ANNOTATION_ENTRY).size <= 1)
            return

        node.getAllChildrenWithType(ANNOTATION_ENTRY).forEach {
            if (!it.isFollowedByNewline() || !it.isBeginByNewline())
                deleteSpaces(it, !it.isFollowedByNewline(), !it.isBeginByNewline())
        }

    }

    private fun deleteSpaces(node: ASTNode, rightSide: Boolean, leftSide: Boolean) {
        Warnings.ANNOTATION_NEW_LINE.warnAndFix(configRules, emitWarn, isFixMode, "${node.text} not on a single line",
                node.startOffset, node) {
            if (rightSide) {
                if (node.treeNext?.isWhiteSpace() == true) {
                    node.removeChild(node.treeNext)
                }
                node.treeParent.addChild(PsiWhiteSpaceImpl("\n"), node.treeNext)
            }

            if (node == node.treeParent.getFirstChildWithType(node.elementType)) {
                // Current node is ANNOTATION_ENTRY. treeParent(ModifierList) -> treeParent(PRIMARY_CONSTRUCTOR)
                // Checks if there is a white space before grandparent node
                if (node.treeParent.treeParent.treePrev.isWhiteSpace()) {
                    (node.treeParent.treeParent.treePrev as LeafPsiElement).replaceWithText("\n")
                }
            }
        }
    }
}
