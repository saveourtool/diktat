package com.saveourtool.diktat.ruleset.rules.chapter3

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.NULLABLE_PROPERTY_TYPE
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.KotlinParser
import com.saveourtool.diktat.ruleset.utils.findAllDescendantsWithSpecificType
import com.saveourtool.diktat.ruleset.utils.hasChildOfType

import org.jetbrains.kotlin.KtNodeTypes.BOOLEAN_CONSTANT
import org.jetbrains.kotlin.KtNodeTypes.CALL_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.CHARACTER_CONSTANT
import org.jetbrains.kotlin.KtNodeTypes.DOT_QUALIFIED_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.FLOAT_CONSTANT
import org.jetbrains.kotlin.KtNodeTypes.INTEGER_CONSTANT
import org.jetbrains.kotlin.KtNodeTypes.NULL
import org.jetbrains.kotlin.KtNodeTypes.NULLABLE_TYPE
import org.jetbrains.kotlin.KtNodeTypes.PROPERTY
import org.jetbrains.kotlin.KtNodeTypes.REFERENCE_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.STRING_TEMPLATE
import org.jetbrains.kotlin.KtNodeTypes.TYPE_REFERENCE
import org.jetbrains.kotlin.KtNodeTypes.USER_TYPE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.lexer.KtTokens.CHARACTER_LITERAL
import org.jetbrains.kotlin.lexer.KtTokens.CLOSING_QUOTE
import org.jetbrains.kotlin.lexer.KtTokens.EQ
import org.jetbrains.kotlin.lexer.KtTokens.FLOAT_LITERAL
import org.jetbrains.kotlin.lexer.KtTokens.INTEGER_LITERAL
import org.jetbrains.kotlin.lexer.KtTokens.OPEN_QUOTE
import org.jetbrains.kotlin.lexer.KtTokens.QUEST
import org.jetbrains.kotlin.lexer.KtTokens.TRUE_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.VAL_KEYWORD

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
                NULLABLE_PROPERTY_TYPE.warn(configRules, emitWarn, "don't use nullable type",
                    node.findChildByType(TYPE_REFERENCE)!!.startOffset, node)
            } else if (node.hasChildOfType(NULL)) {
                val fixedParam = findFixableParam(node)
                NULLABLE_PROPERTY_TYPE.warnOnlyOrWarnAndFix(configRules, emitWarn, "initialize explicitly",
                    node.findChildByType(NULL)!!.startOffset, node, shouldBeAutoCorrected = fixedParam != null, isFixMode) {
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
        const val NAME_ID = "nullable-type"
        private val allowExpression = listOf("emptyList", "emptySequence", "emptyArray", "emptyMap", "emptySet",
            "listOf", "mapOf", "arrayOf", "sequenceOf", "setOf")
    }
}
