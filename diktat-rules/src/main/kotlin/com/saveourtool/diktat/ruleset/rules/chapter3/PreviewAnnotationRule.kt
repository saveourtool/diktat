package com.saveourtool.diktat.ruleset.rules.chapter3

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.PREVIEW_ANNOTATION
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.*

import org.jetbrains.kotlin.KtNodeTypes.ANNOTATION_ENTRY
import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.MODIFIER_LIST
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.isPrivate

/**
 * This rule checks, whether the method has `@Preview` annotation (Jetpack Compose)
 * If so, such method should be private and function name should end with `Preview` suffix
 */
class PreviewAnnotationRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(PREVIEW_ANNOTATION)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == FUN) {
            checkFunctionSignature(node)
        }
    }

    private fun checkFunctionSignature(node: ASTNode) {
        node.findChildByType(MODIFIER_LIST)?.let { modList ->
            doCheck(node, modList)
        }
    }

    private fun doCheck(functionNode: ASTNode, modeList: ASTNode) {
        if (modeList.getAllChildrenWithType(ANNOTATION_ENTRY).isEmpty()) {
            return
        }

        modeList.getAllChildrenWithType(ANNOTATION_ENTRY).filter {
            it.text.contains("$ANNOTATION_SYMBOL$PREVIEW_ANNOTATION_TEXT")
        }.forEach { annotationNode ->
            if (!((functionNode.psi as KtNamedFunction).isPrivate())) {
                PREVIEW_ANNOTATION.warnAndFix(
                    configRules,
                    emitWarn,
                    isFixMode,
                    "${functionNode.text} method should has `Preview` suffix",
                    functionNode.startOffset,
                    functionNode
                ) {
                    // TODO: provide fix
                }
            }

            if(!functionNode.isMethodHasPreviewSuffix()) {
                PREVIEW_ANNOTATION.warnAndFix(
                    configRules,
                    emitWarn,
                    isFixMode,
                    "${functionNode.treeParent.text} method should be private",
                    functionNode.startOffset,
                    functionNode
                ) {
                    // TODO: provide fix
                }
            }
        }
    }

    private fun ASTNode.isMethodHasPreviewSuffix() =
        this.getIdentifierName()?.text?.contains(PREVIEW_ANNOTATION_TEXT) ?: false


    companion object {
        const val NAME_ID = "preview-annotation"
        const val ANNOTATION_SYMBOL = "@"
        const val PREVIEW_ANNOTATION_TEXT = "Preview"
    }
}
