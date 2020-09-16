package org.cqfn.diktat.ruleset.rules.calculations

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.FLOAT_IN_ACCURATE_CALCULATIONS
import org.cqfn.diktat.ruleset.utils.findLocalDeclaration
import org.jetbrains.kotlin.backend.common.onlyIf
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.startOffset

/**
 * Rule that checks that floating-point numbers are not used for accurate calculations
 * 1. Checks that floating-point numbers are not used in arithmetic binary expressions
 * Fixme: detect variables by type, not only floating-point literals
 */
class AccurateCalculationsRule(private val configRules: List<RulesConfig>) : Rule("accurate-calculations") {
    companion object {
        private val arithmeticOperationTokens = listOf(KtTokens.PLUS, KtTokens.PLUSEQ, KtTokens.PLUSPLUS,
                KtTokens.MINUS, KtTokens.MINUSEQ, KtTokens.MINUSMINUS,
                KtTokens.MUL, KtTokens.MULTEQ, KtTokens.DIV, KtTokens.DIVEQ, KtTokens.EQEQ
        )
    }

    @Suppress("UnsafeCallOnNullableType")
    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        (node.psi as? KtBinaryExpression)?.onlyIf({ operationToken in arithmeticOperationTokens }) { expr ->
            // !! is safe because `KtBinaryExpression#left` is annotated `Nullable IfNotParsed`
            val floatValue = expr.left!!.takeIf { it.isFloatingPoint() } ?: expr.right!!.takeIf { it.isFloatingPoint() }
            if (floatValue != null) {
                // float value is used in comparison
                FLOAT_IN_ACCURATE_CALCULATIONS.warn(configRules, emit, autoCorrect,
                        "float value of <${floatValue.text}> used in arithmetic expression in ${expr.text}", expr.startOffset, node)
            }
        }
    }
}

private fun PsiElement.isFloatingPoint() =
        node.elementType == ElementType.FLOAT_LITERAL ||
                node.elementType == ElementType.FLOAT_CONSTANT ||
                ((this as? KtNameReferenceExpression)
                        ?.findLocalDeclaration()
                        ?.initializer
                        ?.node
                        ?.run { elementType == ElementType.FLOAT_LITERAL || elementType == ElementType.FLOAT_CONSTANT }
                        ?: false)
