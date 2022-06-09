package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.NULLABLE_PROPERTY_TYPE
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.KotlinParser
import org.cqfn.diktat.ruleset.utils.findAllDescendantsWithSpecificType
import org.cqfn.diktat.ruleset.utils.hasChildOfType

import com.pinterest.ktlint.core.ast.ElementType.BOOLEAN_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.CHARACTER_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.CHARACTER_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.CLOSING_QUOTE
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.EQ
import com.pinterest.ktlint.core.ast.ElementType.FLOAT_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.FLOAT_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.INTEGER_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.INTEGER_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.NULL
import com.pinterest.ktlint.core.ast.ElementType.NULLABLE_TYPE
import com.pinterest.ktlint.core.ast.ElementType.OPEN_QUOTE
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.QUEST
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.STRING_TEMPLATE
import com.pinterest.ktlint.core.ast.ElementType.TRUE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.USER_TYPE
import com.pinterest.ktlint.core.ast.ElementType.VAL_KEYWORD
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType

/**
 * Rule that checks if nullable types are used and suggest to substitute them with non-nullable
 */
class NullableTypeRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(NULLABLE_PROPERTY_TYPE)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == PROPERTY) {
            checkProperty(node)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkProperty(node: ASTNode) {
        if (node.hasChildOfType(VAL_KEYWORD) && node.hasChildOfType(EQ) && node.hasChildOfType(TYPE_REFERENCE)) {
            val typeReferenceNode = node.findChildByType(TYPE_REFERENCE)!!
            // check that property has nullable type, right value one of allow expression
            if (!node.hasChildOfType(NULL) &&
                node.findAllDescendantsWithSpecificType(DOT_QUALIFIED_EXPRESSION).isEmpty() &&
                typeReferenceNode.hasChildOfType(NULLABLE_TYPE) &&
                typeReferenceNode.findChildByType(NULLABLE_TYPE)!!.hasChildOfType(QUEST) &&
                (node.findChildByType(CALL_EXPRESSION)?.findChildByType(REFERENCE_EXPRESSION) == null ||
                    node.findChildByType(CALL_EXPRESSION)!!.findChildByType(REFERENCE_EXPRESSION)!!.text in allowExpression)) {
                NULLABLE_PROPERTY_TYPE.warn(configRules, emitWarn, isFixMode, "don't use nullable type",
                    node.findChildByType(TYPE_REFERENCE)!!.startOffset, node)
            } else if (node.hasChildOfType(NULL)) {
                val fixedParam = findFixableParam(node)
                NULLABLE_PROPERTY_TYPE.warnAndFix(configRules, emitWarn, isFixMode, "initialize explicitly",
                    node.findChildByType(NULL)!!.startOffset, node, fixedParam != null) {
                    fixedParam?.let {
                        findSubstitution(node, fixedParam)
                    }
                }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun findFixableParam(node: ASTNode): FixedParam? {
        val reference = node.findChildByType(TYPE_REFERENCE)!!
            .findChildByType(NULLABLE_TYPE)!!
            .findChildByType(USER_TYPE)
            ?.findChildByType(REFERENCE_EXPRESSION)
            ?: return null
        return when (reference.text) {
            "Boolean" -> FixedParam(BOOLEAN_CONSTANT, TRUE_KEYWORD, "true")
            "Int", "Short", "Byte" -> FixedParam(INTEGER_CONSTANT, INTEGER_LITERAL, "0")
            "Double" -> FixedParam(FLOAT_CONSTANT, FLOAT_LITERAL, "0.0")
            "Float" -> FixedParam(FLOAT_CONSTANT, FLOAT_LITERAL, "0.0F")
            "Long" -> FixedParam(INTEGER_CONSTANT, INTEGER_LITERAL, "0L")
            "Char" -> FixedParam(CHARACTER_CONSTANT, CHARACTER_LITERAL, "''")
            "String" -> FixedParam(null, null, "", true)
            else -> findFixableForCollectionParam(reference.text)
        }
    }

    private fun findFixableForCollectionParam(referenceText: String): FixedParam? =
        when (referenceText) {
            "List", "Iterable" -> FixedParam(null, null, "emptyList()")
            "Map" -> FixedParam(null, null, "emptyMap()")
            "Array" -> FixedParam(null, null, "emptyArray()")
            "Set" -> FixedParam(null, null, "emptySet()")
            "Sequence" -> FixedParam(null, null, "emptySequence()")
            "Queue" -> FixedParam(null, null, "LinkedList()")
            "MutableList" -> FixedParam(null, null, "mutableListOf()")
            "MutableMap" -> FixedParam(null, null, "mutableMapOf()")
            "MutableSet" -> FixedParam(null, null, "mutableSetOf()")
            "LinkedList" -> FixedParam(null, null, "LinkedList()")
            "LinkedHashMap" -> FixedParam(null, null, "LinkedHashMap()")
            "LinkedHashSet" -> FixedParam(null, null, "LinkedHashSet()")
            else -> null
        }

    @Suppress("UnsafeCallOnNullableType")
    private fun findSubstitution(node: ASTNode, fixedParam: FixedParam) {
        if (fixedParam.isString) {
            replaceValueForString(node)
        } else if (fixedParam.insertConstantType != null && fixedParam.insertType != null) {
            replaceValue(node, fixedParam.insertConstantType, fixedParam.insertType, fixedParam.textNode)
        } else {
            replaceValueByText(node, fixedParam.textNode)
        }
        val nullableNode = node.findChildByType(TYPE_REFERENCE)!!.findChildByType(NULLABLE_TYPE)!!
        val userTypeNode = nullableNode.firstChildNode
        node.findChildByType(TYPE_REFERENCE)!!.replaceChild(nullableNode, userTypeNode)
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun replaceValueByText(node: ASTNode, nodeText: String) {
        val newNode = KotlinParser().createNode(nodeText)
        if (newNode.elementType == CALL_EXPRESSION) {
            node.replaceChild(node.findChildByType(NULL)!!, newNode)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun replaceValue(
        node: ASTNode,
        insertConstantType: IElementType,
        insertType: IElementType,
        textNode: String
    ) {
        val value = CompositeElement(insertConstantType)
        node.addChild(value, node.findChildByType(NULL)!!)
        node.removeChild(node.findChildByType(NULL)!!)
        value.addChild(LeafPsiElement(insertType, textNode))
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun replaceValueForString(node: ASTNode) {
        val value = CompositeElement(STRING_TEMPLATE)
        node.addChild(value, node.findChildByType(NULL)!!)
        node.removeChild(node.findChildByType(NULL)!!)
        value.addChild(LeafPsiElement(OPEN_QUOTE, ""))
        value.addChild(LeafPsiElement(CLOSING_QUOTE, ""))
    }

    @Suppress("KDOC_NO_CONSTRUCTOR_PROPERTY")  // todo add proper docs
    private data class FixedParam(
        val insertConstantType: IElementType?,
        val insertType: IElementType?,
        val textNode: String,
        val isString: Boolean = false
    )

    companion object {
        const val NAME_ID = "acg-nullable-type"
        private val allowExpression = listOf("emptyList", "emptySequence", "emptyArray", "emptyMap", "emptySet",
            "listOf", "mapOf", "arrayOf", "sequenceOf", "setOf")
    }
}
