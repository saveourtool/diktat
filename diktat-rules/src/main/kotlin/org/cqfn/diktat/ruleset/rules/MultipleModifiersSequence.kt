package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.ANNOTATION_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_MULTIPLE_MODIFIERS_ORDER
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.lexer.KtTokens

class MultipleModifiersSequence(private val configRules: List<RulesConfig>) : Rule("multiple-modifiers") {

    companion object {
        private val MODIFIER_ORDER = listOf(KtTokens.PUBLIC_KEYWORD, KtTokens.INTERNAL_KEYWORD, KtTokens.PROTECTED_KEYWORD,
                KtTokens.PRIVATE_KEYWORD, KtTokens.EXPECT_KEYWORD, KtTokens.ACTUAL_KEYWORD, KtTokens.FINAL_KEYWORD,
                KtTokens.OPEN_KEYWORD, KtTokens.ABSTRACT_KEYWORD, KtTokens.SEALED_KEYWORD, KtTokens.CONST_KEYWORD,
                KtTokens.EXTERNAL_KEYWORD, KtTokens.OVERRIDE_KEYWORD, KtTokens.LATEINIT_KEYWORD, KtTokens.TAILREC_KEYWORD,
                KtTokens.CROSSINLINE_KEYWORD, KtTokens.VARARG_KEYWORD, KtTokens.SUSPEND_KEYWORD, KtTokens.INNER_KEYWORD,
                KtTokens.OUT_KEYWORD, KtTokens.ENUM_KEYWORD, KtTokens.ANNOTATION_KEYWORD, KtTokens.COMPANION_KEYWORD,
                KtTokens.INLINE_KEYWORD, KtTokens.NOINLINE_KEYWORD, KtTokens.REIFIED_KEYWORD, KtTokens.INFIX_KEYWORD,
                KtTokens.OPERATOR_KEYWORD, KtTokens.DATA_KEYWORD, KtTokens.IN_KEYWORD, KtTokens.HEADER_KEYWORD,
                KtTokens.IMPL_KEYWORD)
    }

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false
    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == MODIFIER_LIST) {
            checkModifierList(node)
            checkAnnotation(node)
        }
    }

    private fun checkModifierList(node: ASTNode) {
        val modifierListOfPair = node.getChildren(KtTokens.MODIFIER_KEYWORDS).toList()
                .map { Pair(it, MODIFIER_ORDER.indexOf(it.elementType)) }
        val sortModifierListOfPair = modifierListOfPair.sortedBy { it.second }.map { it.first }
        modifierListOfPair.forEachIndexed { index, (modifierNode, _) ->
            if (modifierNode != sortModifierListOfPair[index]) {
                WRONG_MULTIPLE_MODIFIERS_ORDER.warnAndFix(configRules, emitWarn, isFixMode,
                        "${modifierNode.text} should be on position ${sortModifierListOfPair.indexOf(modifierNode) + 1}, but is on position ${index + 1}",
                        modifierNode.startOffset) {
                    val nodeBefore = modifierNode.treeNext
                    node.removeChild(modifierNode)
                    node.addChild((sortModifierListOfPair[index].clone() as ASTNode), nodeBefore)
                }
            }
        }
    }

    private fun checkAnnotation(node: ASTNode) {
        val firstModifierIndex = node.getChildren(KtTokens.MODIFIER_KEYWORDS).map { node.getChildren(null).indexOf(it) }.min() ?: return
        node.getChildren(null).filter { it.elementType == ANNOTATION_ENTRY && node.getChildren(null).indexOf(it) > firstModifierIndex }.forEach {
            WRONG_MULTIPLE_MODIFIERS_ORDER.warnAndFix(configRules, emitWarn, isFixMode,
                    "${it.text} annotation should be before all modifiers",
                    it.startOffset) {
                val spaceAfter = it.treePrev
                node.removeChild(it)
                if (spaceAfter != null && spaceAfter.elementType == WHITE_SPACE){
                    node.removeChild(spaceAfter)
                    node.addChild(spaceAfter, node.getChildren(null).first())
                    node.addChild(it.clone() as ASTNode, spaceAfter)
                } else {
                    node.addChild(PsiWhiteSpaceImpl(" "), node.getChildren(null).first())
                    node.addChild(it.clone() as ASTNode, node.getChildren(null).first())
                }
            }
        }
    }
}
