package org.cqfn.diktat.ruleset.utils.indentation

import com.pinterest.ktlint.core.ast.ElementType
import org.cqfn.diktat.ruleset.rules.files.IndentationRule
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtParameterList
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * When breaking parameter list of a method/class constructor it can be aligned with 8 spaces or a parameter that was moved to a newline
 * can be on the same level as the previous argument
 */
internal class ValueParameterListChecker(configuration: IndentationConfig) : CustomIndentationChecker(configuration) {
    override fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationRule.IndentationError): CheckResult? {
        if (whiteSpace.parent.node.elementType == ElementType.VALUE_PARAMETER_LIST && whiteSpace.nextSibling.node.elementType != ElementType.RPAR) {
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
                indentError.expected + (if (configuration.extendedIndentOfParameters) 1 else 0) * IndentationRule.INDENT_SIZE
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
    override fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationRule.IndentationError): CheckResult? {
        if (whiteSpace.parent.node.elementType == ElementType.BINARY_EXPRESSION) {
            // fixme 2 and 1 instead of 1 and 0 because assignment operator isn't yet included in increasingTokens
            val expectedIndent = indentError.expected + (if (configuration.extendedIndentAfterOperators) 2 else 1) * IndentationRule.INDENT_SIZE
            return CheckResult(expectedIndent == indentError.actual, expectedIndent)
        }
        return null
    }
}

/**
 * In KDoc leading asterisks should be indented with one additional space
 */
internal class KDocIndentationChecker(config: IndentationConfig) : CustomIndentationChecker(config) {
    override fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationRule.IndentationError): CheckResult? {
        if (whiteSpace.nextSibling.node.elementType in listOf(ElementType.KDOC_LEADING_ASTERISK, ElementType.KDOC_END, ElementType.KDOC_SECTION)) {
            val expectedIndent = indentError.expected + 1
            return CheckResult(expectedIndent == indentError.actual, expectedIndent)
        }
        return null
    }
}
