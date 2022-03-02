package org.cqfn.diktat.ruleset.rules.chapter6

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.*

import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.INTEGER_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.children
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * This rule checks if there use property length with operation - 1 and fix this on lastIndex
 */
class UnsafeUseLastIndex(configRules: List<RulesConfig>) : DiktatRule(
    "last-index",
    configRules,
    listOf(Warnings.UNSAFE_USE_LAST_INDEX)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == BINARY_EXPRESSION) {
            changeRight(node)
        }
    }

    private fun fixup(node: ASTNode) {
        val text = node.firstChildNode.text.removeSuffix("length") + "lastIndex"
        val parent = node.treeParent
        val textParent = parent.text.replace(node.text, text)
        val newParent = KotlinParser().createNode(textParent)
        val grand = parent.treeParent
        grand.replaceChild(parent, newParent)
    }

    private fun changeRight(node: ASTNode) {
        val listWithRightLength = node.children().filter {
            val operation = node.getFirstChildWithType(OPERATION_REFERENCE)
            val number = node.getFirstChildWithType(INTEGER_CONSTANT)
            it.elementType == DOT_QUALIFIED_EXPRESSION && it.lastChildNode.text == "length" && it.lastChildNode.elementType == REFERENCE_EXPRESSION &&
                    operation?.text == "-" && number?.text == "1"
        }
        if (listWithRightLength.toList().isNotEmpty()) {
            Warnings.UNSAFE_USE_LAST_INDEX.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
                fixup(node)
            }
        }
    }
}
