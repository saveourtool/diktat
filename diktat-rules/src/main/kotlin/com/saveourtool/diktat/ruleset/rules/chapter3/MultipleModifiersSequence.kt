package com.saveourtool.diktat.ruleset.rules.chapter3

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.WRONG_MULTIPLE_MODIFIERS_ORDER
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.findAllDescendantsWithSpecificType

import org.jetbrains.kotlin.KtNodeTypes.ANNOTATION_ENTRY
import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.MODIFIER_LIST
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.lexer.KtTokens.FUN_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.psiUtil.children

/**
 * @param configRules
 */
class MultipleModifiersSequence(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(WRONG_MULTIPLE_MODIFIERS_ORDER)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == MODIFIER_LIST) {
            checkModifierList(node)
            checkAnnotation(node)
        }
    }

    private fun checkModifierList(node: ASTNode) {
        val modifierListOfPair = node
            .getChildren(KtTokens.MODIFIER_KEYWORDS)
            .toList()
            .filter {
                !isSamInterfaces(node, it)
            }
            .map { Pair(it, modifierOrder.indexOf(it.elementType)) }
        val sortModifierListOfPair = modifierListOfPair.sortedBy { it.second }.map { it.first }
        modifierListOfPair.forEachIndexed { index, (modifierNode, _) ->
            if (modifierNode != sortModifierListOfPair[index]) {
                WRONG_MULTIPLE_MODIFIERS_ORDER.warnAndFix(configRules, emitWarn, isFixMode,
                    "${modifierNode.text} should be on position ${sortModifierListOfPair.indexOf(modifierNode) + 1}, but is on position ${index + 1}",
                    modifierNode.startOffset, modifierNode) {
                    val nodeAfter = modifierNode.treeNext
                    node.removeChild(modifierNode)
                    node.addChild((sortModifierListOfPair[index].clone() as ASTNode), nodeAfter)
                }
            }
        }
    }

    private fun isSamInterfaces(parent: ASTNode, node: ASTNode): Boolean {
        val parentPsi = parent.treeParent.psi
        return if (parentPsi is KtClass) {
            (parentPsi.isInterface()) && node.elementType == FUN_KEYWORD && parent.treeParent.findAllDescendantsWithSpecificType(FUN).size == 1
        } else {
            false
        }
    }

    private fun checkAnnotation(node: ASTNode) {
        val firstModifierIndex = node
            .children()
            .indexOfFirst { it.elementType in KtTokens.MODIFIER_KEYWORDS }
            .takeIf { it >= 0 } ?: return
        node
            .getChildren(null)
            .filterIndexed { index, astNode -> astNode.elementType == ANNOTATION_ENTRY && index > firstModifierIndex }
            .forEach { astNode ->
                WRONG_MULTIPLE_MODIFIERS_ORDER.warnAndFix(configRules, emitWarn, isFixMode,
                    "${astNode.text} annotation should be before all modifiers",
                    astNode.startOffset, astNode) {
                    val spaceBefore = astNode.treePrev
                    node.removeChild(astNode)
                    if (spaceBefore != null && spaceBefore.elementType == WHITE_SPACE) {
                        node.removeChild(spaceBefore)
                        node.addChild(spaceBefore, node.firstChildNode)
                        node.addChild(astNode.clone() as ASTNode, spaceBefore)
                    } else {
                        node.addChild(PsiWhiteSpaceImpl(" "), node.getChildren(null).first())
                        node.addChild(astNode.clone() as ASTNode, node.getChildren(null).first())
                    }
                }
            }
    }

    companion object {
        const val NAME_ID = "multiple-modifiers"
        private val modifierOrder = listOf(KtTokens.PUBLIC_KEYWORD, KtTokens.INTERNAL_KEYWORD, KtTokens.PROTECTED_KEYWORD,
            KtTokens.PRIVATE_KEYWORD, KtTokens.EXPECT_KEYWORD, KtTokens.ACTUAL_KEYWORD, KtTokens.FINAL_KEYWORD,
            KtTokens.OPEN_KEYWORD, KtTokens.ABSTRACT_KEYWORD, KtTokens.SEALED_KEYWORD, KtTokens.CONST_KEYWORD,
            KtTokens.EXTERNAL_KEYWORD, KtTokens.OVERRIDE_KEYWORD, KtTokens.LATEINIT_KEYWORD, KtTokens.TAILREC_KEYWORD,
            KtTokens.CROSSINLINE_KEYWORD, KtTokens.VARARG_KEYWORD, KtTokens.SUSPEND_KEYWORD, KtTokens.INNER_KEYWORD,
            KtTokens.OUT_KEYWORD, KtTokens.ENUM_KEYWORD, KtTokens.ANNOTATION_KEYWORD, KtTokens.COMPANION_KEYWORD,
            KtTokens.VALUE_KEYWORD, KtTokens.INLINE_KEYWORD, KtTokens.NOINLINE_KEYWORD, KtTokens.REIFIED_KEYWORD, KtTokens.INFIX_KEYWORD,
            KtTokens.OPERATOR_KEYWORD, KtTokens.DATA_KEYWORD, KtTokens.IN_KEYWORD)
    }
}
