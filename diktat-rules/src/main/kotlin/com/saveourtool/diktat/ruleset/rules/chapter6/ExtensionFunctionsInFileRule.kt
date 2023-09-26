package com.saveourtool.diktat.ruleset.rules.chapter6

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.EXTENSION_FUNCTION_WITH_CLASS
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.findAllDescendantsWithSpecificType
import com.saveourtool.diktat.ruleset.utils.getAllChildrenWithType
import com.saveourtool.diktat.ruleset.utils.getFirstChildWithType
import com.saveourtool.diktat.ruleset.utils.prevSibling

import org.jetbrains.kotlin.KtNodeTypes.CLASS
import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.TYPE_REFERENCE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens.EXTERNAL_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.IDENTIFIER
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.psiUtil.allChildren
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType

/**
 * This rule checks if there are any extension functions for the class in the same file, where it is defined
 */
class ExtensionFunctionsInFileRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(EXTENSION_FUNCTION_WITH_CLASS)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == KtFileElementType.INSTANCE) {
            val classNames = collectAllClassNames(node)

            collectAllExtensionFunctionsWithSameClassName(node, classNames).forEach {
                fireWarning(it)
            }
        }
    }

    /**
     * Collects all class names in the [file], except those with modifiers from
     * the [ignore list][ignoredModifierTypes].
     *
     * @throws IllegalArgumentException if [file] is not a
     *   [FILE][KtFileElementType.INSTANCE] node.
     */
    private fun collectAllClassNames(file: ASTNode): List<String> {
        require(file.elementType == KtFileElementType.INSTANCE)

        val classes = file.findAllDescendantsWithSpecificType(CLASS)

        return classes.asSequence()
            .map(ASTNode::getPsi)
            .filterIsInstance(KtClass::class.java)
            .filter { clazz ->
                clazz.modifierTypes().none { modifierType ->
                    modifierType in ignoredModifierTypes
                }
            }
            .map(KtClass::getName)
            .filterNotNull()
            .toList()
    }

    private fun fireWarning(node: ASTNode) {
        EXTENSION_FUNCTION_WITH_CLASS.warn(configRules, emitWarn, "fun ${(node.psi as KtFunction).name}", node.startOffset, node)
    }

    private fun collectAllExtensionFunctionsWithSameClassName(node: ASTNode, classNames: List<String>): List<ASTNode> =
        node.getAllChildrenWithType(FUN).filter { isExtensionFunctionWithClassName(it, classNames) }

    @Suppress("UnsafeCallOnNullableType")
    private fun isExtensionFunctionWithClassName(node: ASTNode, classNames: List<String>): Boolean =
        node.getFirstChildWithType(IDENTIFIER)!!.prevSibling { it.elementType == TYPE_REFERENCE }?.text in classNames

    companion object {
        const val NAME_ID = "extension-functions-class-file"

        /**
         * Types of class/interface modifiers which, if present, don't trigger
         * the warning.
         *
         * @since 1.2.5
         */
        private val ignoredModifierTypes: Array<out KtModifierKeywordToken> = arrayOf(
            EXTERNAL_KEYWORD,
        )

        /**
         * @since 1.2.5
         */
        private fun KtClass.modifiers(): Sequence<PsiElement> =
            modifierList?.allChildren ?: emptySequence()

        /**
         * @since 1.2.5
         */
        private fun KtClass.modifierTypes(): Sequence<KtModifierKeywordToken> =
            modifiers()
                .filterIsInstance<LeafPsiElement>()
                .map(LeafPsiElement::getElementType)
                .filterIsInstance<KtModifierKeywordToken>()
    }
}
