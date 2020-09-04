package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_MULTIPLE_MODIFIERS_ORDER
import org.cqfn.diktat.ruleset.utils.KotlinParser
import org.jetbrains.kotlin.com.intellij.lang.ASTNode


class MultipleModifiersSequence : Rule("multiple-modifiers") {

    companion object {
        private val MODIFIER_ORDER = listOf("public", "internal", "protected", "private", "expect", "actual", "final",
                "open", "abstract", "sealed", "const", "external", "override", "lateinit", "tailrec", "crossinline", "vararg",
                "suspend", "inner", "out", "enum", "annotation", "companion", "inline", "noinline", "reified", "infix",
                "operator", "data")
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
                .filter { it.elementType != WHITE_SPACE }
                .map { Pair(it, MODIFIER_ORDER.indexOf(it.text)) }
        val sortModifierListOfPair = modifierListOfPair.sortedBy { it.second }
        modifierListOfPair.forEachIndexed { index, (modifierNode, _) ->
            if (modifierNode != sortModifierListOfPair[index].first) {
                WRONG_MULTIPLE_MODIFIERS_ORDER.warnAndFix(configRules, emitWarn, isFixMode,
                        "this modifier is not in the right position", modifierNode.startOffset) {
                    val nodeBefore = modifierNode.treeNext
                    node.removeChild(modifierNode)
                    node.addChild(KotlinParser().createNode(sortModifierListOfPair[index].first.text), nodeBefore)
                }
            }
        }
    }
}
