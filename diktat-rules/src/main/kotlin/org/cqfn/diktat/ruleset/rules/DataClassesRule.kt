package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.ABSTRACT_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.DATA_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.INNER_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.OPEN_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.SEALED_KEYWORD
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.USE_DATA_CLASS
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass

/**
 * This rule checks if class can be made as data class
 */
class DataClassesRule(private val configRule: List<RulesConfig>) : Rule("data-classes") {
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    companion object {
        private val BAD_MODIFIERS = listOf(OPEN_KEYWORD, ABSTRACT_KEYWORD, INNER_KEYWORD, SEALED_KEYWORD)
    }
    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == CLASS) {
            handleClass(node)
        }
    }

    private fun handleClass(node: ASTNode) {
        if (node.isDataClass())
            return

        if (node.canBeDataClass()) {
            raiseWarn(node)
        }
    }

    // fixme: Need to know types of vars and props to create data class
    private fun raiseWarn(node: ASTNode) {
        USE_DATA_CLASS.warn(configRule, emitWarn, isFixMode, "${(node.psi as KtClass).name}", node.startOffset, node)
    }

    private fun ASTNode.canBeDataClass() : Boolean {
        if (hasChildOfType(MODIFIER_LIST)) {
            val list = getFirstChildWithType(MODIFIER_LIST)!!
            return list.getChildren(null).any { it.elementType !in BAD_MODIFIERS }
                    && findAllNodesWithSpecificType(FUN).isEmpty()
        }
        return findAllNodesWithSpecificType(FUN).isEmpty()
    }

    private fun ASTNode.isDataClass(): Boolean {
        if (hasChildOfType(MODIFIER_LIST)) {
            val list = getFirstChildWithType(MODIFIER_LIST)!!
            return list.getChildren(null).any { it.elementType == DATA_KEYWORD }
        }
        return false
    }
}
