package org.cqfn.diktat.ruleset.rules.chapter6

import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.INTEGER_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.children
import com.pinterest.ktlint.core.ast.lineNumber
import com.pinterest.ktlint.core.ast.parent
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.*
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.prefixExpressionRecursiveVisitor
import org.jetbrains.kotlin.psi.typeReferenceRecursiveVisitor
import org.jetbrains.kotlin.psi2ir.generators.getTypeArguments

class UnsafeUseLastIndex(configRules: List<RulesConfig>) : DiktatRule(
    "last-index",
    configRules,
    listOf(Warnings.UNSAFE_USE_LAST_INDEX)
) {

    override fun logic(node: ASTNode) {
        if (node.elementType == ElementType.BINARY_EXPRESSION) {
            changeRight(node)
        }
    }

    private fun checkSymbol(node: ASTNode, str: String): Boolean =
        when {
            str == " " && node.elementType == WHITE_SPACE -> true
            str == "1" && node.elementType == INTEGER_CONSTANT && node.text == "1" -> true
            str == "length" && node.elementType == REFERENCE_EXPRESSION && node.text == "length" -> true
            str == "-" && node.elementType == OPERATION_REFERENCE && node.text == "-" ->  true
            else -> false
        }

    private fun fixup(node: ASTNode) {
        println("NODE: ${node.text}: ${node.elementType}")
        val text = node.firstChildNode.text.removeSuffix("length") + "lastIndex"
        var parent = node.treeParent
        var textParent = ""
        parent.children().forEach { elem->
            if (elem.text == node.text)
                textParent += text
            else
                textParent += elem.text
        }
        val newParent = KotlinParser().createNode(textParent)
        parent.treeParent.replaceChild(parent, newParent)
    }

    private fun changeRight(node: ASTNode) {
        val listWithRightLength = node.children()
            .filter { it.elementType == DOT_QUALIFIED_EXPRESSION && checkSymbol(it.lastChildNode, "length") }
            .filter { (checkSymbol(it.treeNext, "-") && checkSymbol(it.treeNext.treeNext, "1")) ||
                    (checkSymbol(it.treeNext, "-") && checkSymbol( it.treeNext.treeNext, " " )  && checkSymbol(it.treeNext.treeNext.treeNext, "1")) ||
                    (checkSymbol(it.treeNext, " ") && checkSymbol( it.treeNext.treeNext,"-") && checkSymbol(it.treeNext.treeNext.treeNext, "1")) ||
                    (checkSymbol(it.treeNext, " ") && checkSymbol(it.treeNext.treeNext, "-") && checkSymbol(it.treeNext.treeNext.treeNext, " ") && checkSymbol(it.treeNext.treeNext.treeNext.treeNext, "1"))
        }

        listWithRightLength.forEach { elem ->
            println("${node.text}")
        }

        listWithRightLength.forEach { elem ->
            Warnings.UNSAFE_USE_LAST_INDEX.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
                fixup(node)
            }
        }

    }

}