package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.TYPE_ARGUMENT_LIST
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

class VariableGenericTypeDeclarationRule(private val configRules: List<RulesConfig>) : Rule("variable-generic-type") {
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == PROPERTY || node.elementType == VALUE_PARAMETER) {
            handleProperty(node)
        }
    }

    private fun handleProperty(node: ASTNode) {

        val rightSide = node.findChildByType(CALL_EXPRESSION)

        val hasGenericTypeReference: Boolean = node.findChildByType(TYPE_REFERENCE)?.textContains('<') ?: false
                && node.findChildByType(TYPE_REFERENCE)?.textContains('>') ?: false

        val rightSideHasGenericType: Boolean = rightSide?.textContains('<') ?: false
                && rightSide?.textContains('>') ?: false

        if ((hasGenericTypeReference && rightSideHasGenericType)) {
            Warnings.GENERIC_VARIABLE_WRONG_DECLARATION.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
                rightSide!!.removeChild(rightSide.findChildByType(TYPE_ARGUMENT_LIST)!!)
            }
        }

        if (!hasGenericTypeReference && rightSideHasGenericType) {
            Warnings.GENERIC_VARIABLE_WRONG_DECLARATION.warn(configRules, emitWarn, isFixMode, node.text, node.startOffset, node)
        }
    }

}