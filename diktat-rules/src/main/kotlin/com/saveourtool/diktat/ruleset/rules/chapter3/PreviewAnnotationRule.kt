package com.saveourtool.diktat.ruleset.rules.chapter3

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.constants.Warnings.PREVIEW_ANNOTATION
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.*

import org.jetbrains.kotlin.KtNodeTypes.ANNOTATION_ENTRY
import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.MODIFIER_LIST
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl

/**
 * This rule checks, whether the method has `@Preview` annotation (Jetpack Compose)
 * If so, such method should be private and function name should end with `Preview` suffix
 */
class PreviewAnnotationRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(Warnings.PREVIEW_ANNOTATION)
) {
    override fun logic(node: ASTNode) {
        when (node.elementType) {
            FUN -> checkFunctionSignature(node)
            else -> return
        }
    }

    private fun checkFunctionSignature(node: ASTNode) {
        node.findChildByType(MODIFIER_LIST)?.let { modList ->
            fixAnnotation(modList)
        }
    }

    private fun fixAnnotation(node: ASTNode) {
        if (node.getAllChildrenWithType(ANNOTATION_ENTRY).size <= 1) {
            return
        }

        node.getAllChildrenWithType(ANNOTATION_ENTRY).forEach { annotationNode ->
            if (!annotationNode.isStandardMethod() || !annotationNode.isMethodHasPreviewSuffix()) {
                doWarnAndFix(annotationNode)
            }
        }
    }

    private fun doWarnAndFix(node: ASTNode) {
        PREVIEW_ANNOTATION.warnAndFix(
            configRules,
            emitWarn,
            isFixMode,
            "Method, annotated with ${node.text} should has `Preview` suffix and be private",
            node.startOffset,
            node
        ) {
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
        const val NAME_ID = "preview-annotation"
    }
}
