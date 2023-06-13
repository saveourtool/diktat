package com.saveourtool.diktat.ruleset.rules.chapter3

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.ANNOTATION_NEW_LINE
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.*

import org.jetbrains.kotlin.KtNodeTypes.ANNOTATION_ENTRY
import org.jetbrains.kotlin.KtNodeTypes.CLASS
import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.MODIFIER_LIST
import org.jetbrains.kotlin.KtNodeTypes.PRIMARY_CONSTRUCTOR
import org.jetbrains.kotlin.KtNodeTypes.SECONDARY_CONSTRUCTOR
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl

/**
 * This rule makes each annotation applied to a class, method or constructor is on its own line. Except: if first annotation of constructor, class or method
 */
class AnnotationNewLineRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(ANNOTATION_NEW_LINE)
) {
    override fun logic(node: ASTNode) {
        when (node.elementType) {
            CLASS, FUN, PRIMARY_CONSTRUCTOR, SECONDARY_CONSTRUCTOR -> checkAnnotation(node)
            else -> return
        }
    }

    private fun checkAnnotation(node: ASTNode) {
        node.findChildByType(MODIFIER_LIST)?.let { modList ->
            fixAnnotation(modList)
        }
    }

    private fun fixAnnotation(node: ASTNode) {
        if (node.getAllChildrenWithType(ANNOTATION_ENTRY).size <= 1) {
            return
        }

        node.getAllChildrenWithType(ANNOTATION_ENTRY).forEach {
            if (!it.isFollowedByNewlineWithComment() || !it.isBeginNewLineWithComment()) {
                deleteSpaces(it, !it.isFollowedByNewlineWithComment())
            }
        }
    }

    private fun deleteSpaces(node: ASTNode,
                             rightSide: Boolean) {
        ANNOTATION_NEW_LINE.warnAndFix(configRules, emitWarn, isFixMode, "${node.text} not on a single line",
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
                val hasSpaceBeforeGrandparent = node
                    .treeParent
                    .treeParent
                    .treePrev
                    .isWhiteSpace()
                if (hasSpaceBeforeGrandparent) {
                    (node.treeParent.treeParent.treePrev as LeafPsiElement).rawReplaceWithText("\n")
                }
            }
        }
    }

    companion object {
        const val NAME_ID = "annotation-new-line"
    }
}
