package org.cqfn.diktat.ruleset.utils.indentation

import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.RPAR
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER_LIST
import org.cqfn.diktat.ruleset.rules.files.IndentationRule.Companion.INDENT_SIZE
import org.cqfn.diktat.ruleset.rules.files.IndentationRule.IndentationError
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtParameterList
import org.jetbrains.kotlin.psi.psiUtil.parents

internal abstract class CustomIndentationChecker(protected val configuration: IndentationConfig) {
    /**
     * This method checks if this white space is an exception from general rule
     * If true, checks if it is properly indented and fixes
     * @return null true if node is not an exception, CheckResult otherwise
     */
    abstract fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationError): CheckResult?
}

/**
 * When breaking parameter list of a method/class constructor it can be aligned with 8 spaces or a parameter that was moved to a newline
 * can be on the same level as the previous argument
 */
internal class ValueParameterListChecker(configuration: IndentationConfig) : CustomIndentationChecker(configuration) {
    override fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationError): CheckResult? {
//        if (whiteSpace.parent is KtParameterList) {
        if (whiteSpace.parent.node.elementType == VALUE_PARAMETER_LIST && whiteSpace.nextSibling.node.elementType != RPAR) {
            val parameterList = whiteSpace.parent as KtParameterList
            val listNode = parameterList.node
            val firstValueParameter = listNode.findChildByType(ElementType.LPAR)!!.treeNext.takeIf { it.elementType == ElementType.VALUE_PARAMETER }

            val expectedIndent = if (firstValueParameter != null) {
                if (configuration.alignedParameters) {
                    listNode.parents().last().text.substringBefore(firstValueParameter.text).lines().last().count()
                } else {
                    indentError.expected
                }
            } else {
                indentError.expected + (if (configuration.extendedIndentOfParameters) 1 else 0) * INDENT_SIZE
            }

            return CheckResult(indentError.actual == expectedIndent, expectedIndent)
        }
        return null
    }
}

/**
 * When breaking line after operators like +/-/`*` etc. new line can be indented with 8 space
 */
internal class ExpressionIndentationChecker(configuration: IndentationConfig) : CustomIndentationChecker(configuration) {
    override fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationError): CheckResult? {
        if (whiteSpace.parent.node.elementType == BINARY_EXPRESSION) {
            // fixme 2 and 1 instead of 1 and 0 because assignment operator isn't yet included in increasingTokens
            val expectedIndent = indentError.expected + (if (configuration.extendedIndentAfterOperators) 2 else 1) * INDENT_SIZE
            return CheckResult(expectedIndent == indentError.actual, expectedIndent)
        }
        return null
    }
}

internal data class CheckResult(val correct: Boolean, val expected: Int)
