package org.cqfn.diktat.ruleset.rules.chapter6

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.NO_CORRESPONDING_PROPERTY
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.hasAnyChildOfTypes
import org.cqfn.diktat.ruleset.utils.hasChildOfType

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.GET_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY_ACCESSOR
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.RETURN
import com.pinterest.ktlint.core.ast.ElementType.SET_KEYWORD
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtProperty

/**
 * This rule checks if there is a backing property for field with property accessors, in case they don't use field keyword
 */
class ImplicitBackingPropertyRule(private val configRules: List<RulesConfig>) : Rule("implicit-backing-property") {
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == CLASS_BODY) {
            findAllProperties(node)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun findAllProperties(node: ASTNode) {
        val properties = node.getChildren(null).filter { it.elementType == PROPERTY }

        val propsWithBackSymbol = properties
            .filter { it.getFirstChildWithType(IDENTIFIER)!!.text.startsWith("_") }
            .map {
                it.getFirstChildWithType(IDENTIFIER)!!.text
            }

        properties.filter { it.hasAnyChildOfTypes(PROPERTY_ACCESSOR) }.forEach {
            validateAccessors(it, propsWithBackSymbol)
        }
    }

    private fun validateAccessors(node: ASTNode, propsWithBackSymbol: List<String>) {
        val accessors = node.findAllNodesWithSpecificType(PROPERTY_ACCESSOR).filter { it.hasChildOfType(BLOCK) }  // exclude get with expression body

        accessors.filter { it.hasChildOfType(GET_KEYWORD) }.forEach { handleGetAccessors(it, node, propsWithBackSymbol) }
        accessors.filter { it.hasChildOfType(SET_KEYWORD) }.forEach { handleSetAccessors(it, node, propsWithBackSymbol) }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleGetAccessors(
        accessor: ASTNode,
        node: ASTNode,
        propsWithBackSymbol: List<String>) {
        val refExprs = accessor
            .findAllNodesWithSpecificType(RETURN)
            .filterNot { it.hasChildOfType(DOT_QUALIFIED_EXPRESSION) }
            .flatMap { it.findAllNodesWithSpecificType(REFERENCE_EXPRESSION) }

        val localProps = accessor
            .findAllNodesWithSpecificType(PROPERTY)
            .map { (it.psi as KtProperty).name!! }
        // If refExprs is empty then we assume that it returns some constant
        if (refExprs.isNotEmpty()) {
            handleReferenceExpressions(node, refExprs, propsWithBackSymbol, localProps)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleSetAccessors(
        accessor: ASTNode,
        node: ASTNode,
        propsWithBackSymbol: List<String>) {
        val refExprs = accessor.findAllNodesWithSpecificType(REFERENCE_EXPRESSION)

        // In set we don't check for local properties. At least one reference expression should contain field or _prop
        if (refExprs.isNotEmpty()) {
            handleReferenceExpressions(node, refExprs, propsWithBackSymbol, null)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleReferenceExpressions(node: ASTNode,
                                           expressions: List<ASTNode>,
                                           backingPropertiesNames: List<String>,
                                           localProperties: List<String>?) {
        if (expressions.none {
            backingPropertiesNames.contains(it.text) || it.text == "field" || localProperties?.contains(it.text) == true
        }) {
            raiseWarning(node, node.getFirstChildWithType(IDENTIFIER)!!.text)
        }
    }

    private fun raiseWarning(node: ASTNode, propName: String) {
        NO_CORRESPONDING_PROPERTY.warn(configRules, emitWarn, isFixMode,
            "$propName has no corresponding property with name _$propName", node.startOffset, node)
    }
}
