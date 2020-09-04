package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.ANNOTATION_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_MULTIPLE_MODIFIERS_ORDER
import org.cqfn.diktat.ruleset.utils.KotlinParser
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens


class MultipleModifiersSequence : Rule("multiple-modifiers") {

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

    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false
    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        configRules = params.getDiktatConfigRules()
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == MODIFIER_LIST)
            checkModifierList(node)
    }

    private fun checkModifierList(node: ASTNode) {
        val modifierListOfPair = node.getChildren(null)
                .filter { it.elementType != WHITE_SPACE && it.elementType != ANNOTATION_ENTRY }
                .map { Pair(it, MODIFIER_ORDER.indexOf(it.elementType)) }
        val sortModifierListOfPair = modifierListOfPair.sortedBy { it.second }.map { it.first }
        modifierListOfPair.forEachIndexed { index, (modifierNode, _) ->
            if (modifierNode != sortModifierListOfPair[index]) {
                WRONG_MULTIPLE_MODIFIERS_ORDER.warnAndFix(configRules, emitWarn, isFixMode,
                        "${modifierNode.text} modifier is not in the right position", modifierNode.startOffset) {
                    val nodeBefore = modifierNode.treeNext
                    node.removeChild(modifierNode)
                    node.addChild(KotlinParser().createNode(sortModifierListOfPair[index].text), nodeBefore)
                }
            }
        }
    }
}
