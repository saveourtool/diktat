package org.cqfn.diktat.ruleset.rules.calculations

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.FLOAT_IN_ACCURATE_CALCULATIONS
import org.cqfn.diktat.ruleset.utils.findLocalDeclaration
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.psiUtil.startOffset

/**
 * Rule that checks that floating-point numbers are not used for accurate calculations
 * 1. Checks that floating-point numbers are not used in arithmetic binary expressions
 * Fixme: detect variables by type, not only floating-point literals
 */
class AccurateCalculationsRule(private val configRules: List<RulesConfig>) : Rule("accurate-calculations") {
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    companion object {
        private val arithmeticOperationTokens = listOf(KtTokens.PLUS, KtTokens.PLUSEQ, KtTokens.PLUSPLUS,
                KtTokens.MINUS, KtTokens.MINUSEQ, KtTokens.MINUSMINUS,
                KtTokens.MUL, KtTokens.MULTEQ, KtTokens.DIV, KtTokens.DIVEQ,
                KtTokens.GT, KtTokens.LT, KtTokens.LTEQ, KtTokens.GTEQ,
                KtTokens.EQEQ
        )
        private val arithmeticOperationsFunctions = listOf("equals", "compareTo")
    }

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        when (val psi = node.psi) {
            is KtBinaryExpression -> handleBinaryExpression(psi)
            is KtDotQualifiedExpression -> handleFunction(psi)
            else -> return
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleBinaryExpression(expression: KtBinaryExpression) = expression
            .takeIf { it.operationToken in arithmeticOperationTokens }
            ?.let { expr ->
                // !! is safe because `KtBinaryExpression#left` is annotated `Nullable IfNotParsed`
                val floatValue = expr.left!!.takeIf { it.isFloatingPoint() }
                        ?: expr.right!!.takeIf { it.isFloatingPoint() }
                checkFloatValue(floatValue, expr)
            }

    private fun handleFunction(expression: KtDotQualifiedExpression) = expression
            .takeIf { it.selectorExpression is KtCallExpression }
            ?.run { receiverExpression to selectorExpression as KtCallExpression }
            ?.takeIf {
                (it.second.calleeExpression as? KtNameReferenceExpression)
                        ?.getReferencedName() in arithmeticOperationsFunctions
            }
            ?.let { (receiverExpression, selectorExpression) ->
                val floatValue = receiverExpression.takeIf { it.isFloatingPoint() }
                        ?: selectorExpression
                                .valueArguments
                                .find { it.getArgumentExpression()?.isFloatingPoint() ?: false }

                checkFloatValue(floatValue, expression)
            }

    private fun checkFloatValue(floatValue: PsiElement?, expression: KtExpression) {
        if (floatValue != null) {
            // float value is used in comparison
            FLOAT_IN_ACCURATE_CALCULATIONS.warn(configRules, emitWarn, isFixMode,
                    "float value of <${floatValue.text}> used in arithmetic expression in ${expression.text}", expression.startOffset, expression.node)
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
