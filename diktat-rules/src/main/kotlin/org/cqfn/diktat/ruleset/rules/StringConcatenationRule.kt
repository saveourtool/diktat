package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.PLUS
import com.pinterest.ktlint.core.ast.ElementType.STRING_TEMPLATE
import org.cqfn.diktat.ruleset.constants.Warnings.STRING_CONCATENATION
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.utils.*
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import java.lang.annotation.ElementType

/**
 * This rule covers checks and fixes related to string concatenation.
 * Rule 3.8 prohibits string concatenation and suggests to use string templates instead
 * // FixMe: fixes will be added
 * // FixMe: .toString() method and functions that return strings are not supported
 */
class StringConcatenationRule : Rule("string-concatenation") {
    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var fileName: String? = null
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        configRules = params.getDiktatConfigRules()
        fileName = params.fileName
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == BINARY_EXPRESSION) {
            // searching top-level binary expression to detect any operations with "plus" (+)
            // string concatenation is prohibited only for single line statements
            if (node.findParentNodeWithSpecificType(BINARY_EXPRESSION) == null && isSingleLineStatement(node)) {
                detectStringConcatenation(node)
            }
        }
    }

    private fun isSingleLineStatement(node: ASTNode): Boolean =
            !node.text.contains("\n")

    private fun detectStringConcatenation(node: ASTNode) {
        node.findAllNodesWithSpecificType(BINARY_EXPRESSION).find { detectStringConcatenationInExpression(it, node) }
    }

    private fun detectStringConcatenationInExpression(node: ASTNode, parentNode: ASTNode): Boolean {
        assert(node.elementType == BINARY_EXPRESSION)
        val firstChild = node.firstChildNode
        return if (isPlusBinaryExpression(node) && firstChild.elementType == STRING_TEMPLATE) {
            STRING_CONCATENATION.warn(configRules, emitWarn, this.isFixMode, parentNode.text, firstChild.startOffset, firstChild)
            true
        } else {
            false
        }
    }

    private fun isPlusBinaryExpression(node: ASTNode): Boolean {
        assert(node.elementType == BINARY_EXPRESSION)
        //     binary expression
        //    /        |        \
        //  expr1 operationRef expr2

        val operationReference = node.getFirstChildWithType(OPERATION_REFERENCE)
        return operationReference
                ?.getFirstChildWithType(PLUS) != null
    }
}
