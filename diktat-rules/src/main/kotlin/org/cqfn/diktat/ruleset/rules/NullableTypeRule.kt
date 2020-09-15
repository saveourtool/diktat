package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.CHARACTER_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.CHARACTER_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.CLOSING_QUOTE
import com.pinterest.ktlint.core.ast.ElementType.EQ
import com.pinterest.ktlint.core.ast.ElementType.FLOAT_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.FLOAT_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.INTEGER_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.INTEGER_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.NULL
import com.pinterest.ktlint.core.ast.ElementType.NULLABLE_TYPE
import com.pinterest.ktlint.core.ast.ElementType.OPEN_QUOTE
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.STRING_TEMPLATE
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.USER_TYPE
import com.pinterest.ktlint.core.ast.ElementType.VAL_KEYWORD
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.NULLABLE_PROPERTY_TYPE
import org.cqfn.diktat.ruleset.utils.KotlinParser
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType

class NullableTypeRule(private val configRules: List<RulesConfig>) : Rule("nullable-type") {

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        isFixMode = autoCorrect
        emitWarn = emit

        if (node.elementType == PROPERTY)
            checkProperty(node)
    }

    private fun checkProperty(node: ASTNode) {
        if (node.hasChildOfType(VAL_KEYWORD) && node.hasChildOfType(EQ) && node.hasChildOfType(TYPE_REFERENCE) && node.hasChildOfType(NULL)) {
            NULLABLE_PROPERTY_TYPE.warnAndFix(configRules, emitWarn, isFixMode, "initialize explicitly", node.findChildByType(NULL)!!.startOffset) {
                findSubstitution(node)
            }
        }
    }

    private fun findSubstitution(node: ASTNode) {
        val reference = node.findChildByType(TYPE_REFERENCE)!!.findChildByType(NULLABLE_TYPE)!!.findChildByType(USER_TYPE)?.findChildByType(REFERENCE_EXPRESSION)
                ?: return

        when (reference.text) {
            "Int", "Short", "Byte" -> replaceValue(node, INTEGER_CONSTANT, INTEGER_LITERAL, "0")
            "Double", "Float" -> replaceValue(node, FLOAT_CONSTANT, FLOAT_LITERAL, "0.0")
            "Long" -> replaceValue(node, INTEGER_CONSTANT, INTEGER_LITERAL, "0L")
            "Char" -> replaceValue(node, CHARACTER_CONSTANT, CHARACTER_LITERAL, "\'\'")
            "String" -> replaceValueForString(node)
            "List" -> replaceValueByText(node, "emptyList()")
            "MutableList" -> replaceValueByText(node, "mutableListOf()")
            "MutableMap" -> replaceValueByText(node, "mutableMapOf()")
            else -> return
        }
    }

    private fun replaceValueByText(node: ASTNode, nodeText: String) {
        val newNode = KotlinParser().createNode(nodeText)
        if (newNode.elementType == CALL_EXPRESSION) {
            node.addChild(newNode, node.findChildByType(NULL))
            node.removeChild(node.findChildByType(NULL)!!)
        }
    }

    private fun replaceValue(node: ASTNode, insertConstantType: IElementType, insertType: IElementType, textNode: String) {
        val value = CompositeElement(insertConstantType)
        node.addChild(value, node.findChildByType(NULL)!!)
        node.removeChild(node.findChildByType(NULL)!!)
        value.addChild(LeafPsiElement(insertType, textNode))
    }

    private fun replaceValueForString(node: ASTNode) {
        val value = CompositeElement(STRING_TEMPLATE)
        node.addChild(value, node.findChildByType(NULL)!!)
        node.removeChild(node.findChildByType(NULL)!!)
        value.addChild(LeafPsiElement(OPEN_QUOTE, ""))
        value.addChild(LeafPsiElement(CLOSING_QUOTE, ""))
    }
}
