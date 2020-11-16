package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY_ACCESSOR
import com.pinterest.ktlint.core.ast.ElementType.RBRACE
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER_LIST
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.isWhiteSpace
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.getIdentifierName
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.cqfn.diktat.ruleset.utils.prettyPrint
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtPropertyAccessor

/**
 * This rule checks if there are any trivial getters and setters and, if so, deletes them
 */
class TrivialPropertyAccessors(private val configRules: List<RulesConfig>) : Rule("trivial-property-accessors") {
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    companion object {
        private val EXCESS_CHILDREN_TYPES = listOf(LBRACE, RBRACE, WHITE_SPACE, EOL_COMMENT, BLOCK_COMMENT)
        private const val ONE_CHILD_IN_ARRAY = 1
    }

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == PROPERTY_ACCESSOR) {
            handlePropertyAccessors(node)
        }
    }

    private fun handlePropertyAccessors(node: ASTNode) {
        if ((node.psi as KtPropertyAccessor).isGetter) {
            handleGetAccessor(node)
        } else {
            handleSetAccessor(node)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleSetAccessor(node: ASTNode) {
        val valueParamName = node
                .getFirstChildWithType(VALUE_PARAMETER_LIST)
                ?.firstChildNode
                ?.getIdentifierName()
                ?.text

        if (node.hasChildOfType(BLOCK) && !valueParamName.isNullOrEmpty()) {
            val block = node.getFirstChildWithType(BLOCK)!!

            val blockChildren = block.getChildren(null).filter { it.elementType !in EXCESS_CHILDREN_TYPES }

            if (blockChildren.size == 1
                    && blockChildren.first().elementType == BINARY_EXPRESSION
                    && (blockChildren.first().psi as KtBinaryExpression).left?.text == "field"
                    && (blockChildren.first().psi as KtBinaryExpression).right?.text == valueParamName
            ) {
                raiseWarning(node)
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleGetAccessor(node: ASTNode) {
        // It handles both cases: get() = ...  and  get() { return ... }
        val references = node.findAllNodesWithSpecificType(REFERENCE_EXPRESSION)
        if (references.singleOrNull()?.text == "field") {
            raiseWarning(node)
        } else if (node.getChildren(null).size == ONE_CHILD_IN_ARRAY) {
            raiseWarning(node)
        }
    }

    private fun raiseWarning(node: ASTNode) {
        Warnings.TRIVIAL_ACCESSORS_ARE_NOT_RECOMMENDED.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
            val property = (node.psi as KtPropertyAccessor).property.node
            if (node.treePrev.isWhiteSpace()) {
                property.removeChild(node.treePrev)
            }
            property.removeChild(node)
        }
    }
}
