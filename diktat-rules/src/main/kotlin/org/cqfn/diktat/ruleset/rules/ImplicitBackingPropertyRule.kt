package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY_ACCESSOR
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.NO_CORRESPONDING_PROPERTY
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.getIdentifierName
import org.cqfn.diktat.ruleset.utils.hasAnyChildOfTypes
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

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

    private fun findAllProperties(node: ASTNode) {
        val properties = node.getChildren(null).filter { it.elementType == PROPERTY }

        val propsWithBackSymbol = mutableListOf<String>()

        properties
                .filter { it.getFirstChildWithType(IDENTIFIER)!!.text.startsWith("_") }
                .forEach {
                    propsWithBackSymbol.add(it.getFirstChildWithType(IDENTIFIER)!!.text)
                }

        properties.filter { it.hasAnyChildOfTypes(PROPERTY_ACCESSOR) }.forEach {
            validateAccessors(it, propsWithBackSymbol)
        }
    }

    private fun validateAccessors(node: ASTNode, propsWithBackSymbol: List<String>) {
        val accessors = node.findAllNodesWithSpecificType(PROPERTY_ACCESSOR)

        accessors.forEach {
            val refExprs = it.findAllNodesWithSpecificType(REFERENCE_EXPRESSION)

            if (refExprs.isNotEmpty()) {
                handleReferenceExpressions(node, refExprs, propsWithBackSymbol)
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleReferenceExpressions(node:ASTNode,
                                           expressions: List<ASTNode>,
                                           backingPropertiesNames: List<String>) {
        if (expressions.none { backingPropertiesNames.contains(it.text) || it.text != "field" }) {
            raiseWarning(node, node.getFirstChildWithType(IDENTIFIER)!!.text)
        }
    }

    private fun raiseWarning(node:ASTNode, propName: String) {
        NO_CORRESPONDING_PROPERTY.warn(configRules, emitWarn, isFixMode,
                "_$propName has no corresponding property with name $propName", node.startOffset, node)
    }

}
