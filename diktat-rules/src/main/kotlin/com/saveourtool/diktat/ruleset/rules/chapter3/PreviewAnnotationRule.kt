package com.saveourtool.diktat.ruleset.rules.chapter3

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.PREVIEW_ANNOTATION
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.KotlinParser
import com.saveourtool.diktat.ruleset.utils.findAllNodesWithCondition
import com.saveourtool.diktat.ruleset.utils.getAllChildrenWithType
import com.saveourtool.diktat.ruleset.utils.getIdentifierName

import org.jetbrains.kotlin.KtNodeTypes.ANNOTATION_ENTRY
import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.MODIFIER_LIST
import org.jetbrains.kotlin.com.intellij.lang.ASTFactory
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.lexer.KtTokens.ABSTRACT_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.INTERNAL_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.OPEN_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.PRIVATE_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.PROTECTED_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.PUBLIC_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
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

    @Suppress("TOO_LONG_FUNCTION")
    private fun doCheck(functionNode: ASTNode, modeList: ASTNode) {
        if (modeList.getAllChildrenWithType(ANNOTATION_ENTRY).isEmpty()) {
            return
        }

        val previewAnnotationNode = modeList.getAllChildrenWithType(ANNOTATION_ENTRY).firstOrNull {
            it.text.contains("$ANNOTATION_SYMBOL$PREVIEW_ANNOTATION_TEXT")
        }

        previewAnnotationNode?.let {
            val functionNameNode = functionNode.getIdentifierName()
            val functionName = functionNameNode?.text ?: return

            // warn if function is not private
            if (!(functionNode.psi as KtNamedFunction).isPrivate()) {
                PREVIEW_ANNOTATION.warnAndFix(
                    configRules,
                    emitWarn,
                    isFixMode,
                    "$functionName method should be private",
                    functionNode.startOffset,
                    functionNode
                ) {
                    addPrivateModifier(functionNode)
                }
            }

            // warn if function has no `Preview` suffix
            if (!isMethodHasPreviewSuffix(functionName)) {
                PREVIEW_ANNOTATION.warnAndFix(
                    configRules,
                    emitWarn,
                    isFixMode,
                    "$functionName method should has `Preview` suffix",
                    functionNode.startOffset,
                    functionNode
                ) {
                    functionNode.replaceChild(
                        functionNameNode,
                        KotlinParser().createNode("${functionNameNode.text}Preview")
                    )
                }
            }
        }
    }

    private fun isMethodHasPreviewSuffix(functionName: String) =
        functionName.contains(PREVIEW_ANNOTATION_TEXT)

    private fun addPrivateModifier(functionNode: ASTNode) {
        // MODIFIER_LIST should be present since ANNOTATION_ENTRY is there
        val modifierListNode = functionNode.findChildByType(MODIFIER_LIST) ?: return
        val modifiersList = modifierListNode
            .getChildren(KtTokens.MODIFIER_KEYWORDS)
            .toList()

        val isMethodAbstract = modifiersList.any {
            it.elementType == ABSTRACT_KEYWORD
        }

        // private modifier is not applicable for abstract methods
        if (isMethodAbstract) {
            return
        }

        // these modifiers could be safely replaced via `private`
        val modifierForReplacement = modifiersList.firstOrNull {
            it.elementType in listOf(
                PUBLIC_KEYWORD, PROTECTED_KEYWORD, INTERNAL_KEYWORD, OPEN_KEYWORD
            )
        }

        modifierForReplacement?.let {
            // replace current modifier with `private`
            val parent = it.treeParent
            parent.replaceChild(it, createPrivateModifierNode())
        } ?: run {
            // the case, when there is no explicit modifier, i.e. `fun foo`
            // just add `private` in MODIFIER_LIST at the end
            // and move WHITE_SPACE before function identifier `fun` to MODIFIER_LIST
            val funNode = functionNode.findAllNodesWithCondition { it.text == "fun" }.single()
            val whiteSpaceAfterAnnotation = modifierListNode.treeNext
            modifierListNode.addChild(whiteSpaceAfterAnnotation, null)
            modifierListNode.addChild(createPrivateModifierNode(), null)
            // add ` ` node before `fun`
            functionNode.addChild(ASTFactory.leaf(WHITE_SPACE, " "), funNode)
        }
    }

    private fun createPrivateModifierNode() = ASTFactory.leaf(PRIVATE_KEYWORD, "private")

    companion object {
        const val ANNOTATION_SYMBOL = "@"
        const val NAME_ID = "preview-annotation"
        const val PREVIEW_ANNOTATION_TEXT = "Preview"
    }
}
