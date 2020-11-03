package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.GET_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY_ACCESSOR
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.RETURN
import com.pinterest.ktlint.core.ast.ElementType.SET_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.NO_CORRESPONDING_PROPERTY
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.getIdentifierName
import org.cqfn.diktat.ruleset.utils.hasAnyChildOfTypes
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.cqfn.diktat.ruleset.utils.prettyPrint
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * This rule checks if there is a backing property for field with property accessors, in case they don't use field keyword
 */
class ImplicitBackingPropertyRule(private val configRules: List<RulesConfig>) : Rule("implicit-backing-property") {
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
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
        val accessors = node.findAllNodesWithSpecificType(PROPERTY_ACCESSOR).filter { it.hasChildOfType(BLOCK) } // exclude inline get

        accessors.filter { it.hasChildOfType(GET_KEYWORD) }.forEach { handleGetAccessors(it, node, propsWithBackSymbol) }
        accessors.filter { it.hasChildOfType(SET_KEYWORD) }.forEach { handleSetAccessors(it, node, propsWithBackSymbol) }
    }

    private fun handleGetAccessors(accessor: ASTNode, node: ASTNode, propsWithBackSymbol: List<String>) {
        val refExprs = accessor
                .findAllNodesWithSpecificType(RETURN)
                .flatMap { _return -> _return.findAllNodesWithSpecificType(REFERENCE_EXPRESSION) }

        // If refExprs is empty then we assume that it returns some constant
        if (refExprs.isNotEmpty()) {
            handleReferenceExpressions(node, refExprs, propsWithBackSymbol)
        }
    }

    private fun handleSetAccessors(accessor: ASTNode, node: ASTNode, propsWithBackSymbol: List<String>) {
        val refExprs = accessor.findAllNodesWithSpecificType(REFERENCE_EXPRESSION)

        if (refExprs.isNotEmpty()) {
            handleReferenceExpressions(node, refExprs, propsWithBackSymbol)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleReferenceExpressions(node:ASTNode,
                                           expressions: List<ASTNode>,
                                           backingPropertiesNames: List<String>) {
        if (expressions.none { backingPropertiesNames.contains(it.text) || it.text == "field" }) {
            raiseWarning(node, node.getFirstChildWithType(IDENTIFIER)!!.text)
        }
    }

    private fun raiseWarning(node:ASTNode, propName: String) {
        NO_CORRESPONDING_PROPERTY.warn(configRules, emitWarn, isFixMode,
                "$propName has no corresponding property with name _$propName", node.startOffset, node)
    }

}
