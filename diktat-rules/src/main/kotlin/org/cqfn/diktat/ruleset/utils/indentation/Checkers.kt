package org.cqfn.diktat.ruleset.utils.indentation

import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.BODY
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.COLON
import com.pinterest.ktlint.core.ast.ElementType.DOT
import com.pinterest.ktlint.core.ast.ElementType.ELSE
import com.pinterest.ktlint.core.ast.ElementType.EQ
import com.pinterest.ktlint.core.ast.ElementType.KDOC_END
import com.pinterest.ktlint.core.ast.ElementType.KDOC_LEADING_ASTERISK
import com.pinterest.ktlint.core.ast.ElementType.KDOC_SECTION
import com.pinterest.ktlint.core.ast.ElementType.LPAR
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.RPAR
import com.pinterest.ktlint.core.ast.ElementType.SAFE_ACCESS
import com.pinterest.ktlint.core.ast.ElementType.SUPER_TYPE_LIST
import com.pinterest.ktlint.core.ast.ElementType.THEN
import com.pinterest.ktlint.core.ast.ElementType.VALUE_ARGUMENT_LIST
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER_LIST
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.prevSibling
import org.cqfn.diktat.ruleset.rules.files.IndentationError
import org.cqfn.diktat.ruleset.rules.files.lastIndent
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtLoopExpression
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf

/**
 * Performs the following check: assignment operator increases indent by one step for the expression after it.
 * If [IndentationConfig.extendedIndentAfterOperators] is set to true, indentation is increased by two steps instead.
 */
internal class AssignmentOperatorChecker(configuration: IndentationConfig) : CustomIndentationChecker(configuration) {
    override fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationError): CheckResult? {
        val prevNode = whiteSpace.prevSibling.node
        if (prevNode.elementType == EQ && prevNode.treeNext.let { it.elementType == WHITE_SPACE && it.textContains('\n') }) {
            return CheckResult.from(indentError.actual, (whiteSpace.parentIndent()
                    ?: indentError.expected) + (if (configuration.extendedIndentAfterOperators) 2 else 1) * configuration.indentationSize, true)
        }
        return null
    }
}

/**
 * Performs the following check: When breaking parameter list of a method/class constructor it can be aligned with 8 spaces
 * or a parameter that was moved to a newline can be on the same level as the previous argument
 */
@Suppress("ForbiddenComment")
internal class ValueParameterListChecker(configuration: IndentationConfig) : CustomIndentationChecker(configuration) {
    override fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationError): CheckResult? {
        if (whiteSpace.parent.node.elementType in listOf(VALUE_PARAMETER_LIST, VALUE_ARGUMENT_LIST) && whiteSpace.nextSibling.node.elementType != RPAR) {
            val parameterList = whiteSpace.parent.node
            // parameters in lambdas are VALUE_PARAMETER_LIST and might have no LPAR: list { elem -> ... }
            val parameterAfterLpar = parameterList
                    .findChildByType(LPAR)
                    ?.treeNext
                    ?.takeIf { it.elementType == VALUE_PARAMETER }

            val expectedIndent = if (parameterAfterLpar != null && configuration.alignedParameters) {
                // fixme: probably there is a better way to find column number
                parameterList.parents().last().text.substringBefore(parameterAfterLpar.text).lines().last().count()
            } else if (parameterAfterLpar == null && configuration.extendedIndentOfParameters) {
                indentError.expected + configuration.indentationSize
            } else {
                indentError.expected
            }

            return CheckResult.from(indentError.actual, expectedIndent)
        }
        return null
    }
}

/**
 * Performs the following check: When breaking line after operators like +/-/`*` etc. new line can be indented with 8 space
 */
internal class ExpressionIndentationChecker(configuration: IndentationConfig) : CustomIndentationChecker(configuration) {
    override fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationError): CheckResult? {
        if (whiteSpace.parent.node.elementType == BINARY_EXPRESSION && whiteSpace.prevSibling.node.elementType == OPERATION_REFERENCE) {
            val expectedIndent = (whiteSpace.parentIndent() ?: indentError.expected) +
                    (if (configuration.extendedIndentAfterOperators) 2 else 1) * configuration.indentationSize
            return CheckResult.from(indentError.actual, expectedIndent)
        }
        return null
    }
}

/**
 * In KDoc leading asterisks should be indented with one additional space
 */
internal class KDocIndentationChecker(config: IndentationConfig) : CustomIndentationChecker(config) {
    override fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationError): CheckResult? {
        if (whiteSpace.nextSibling.node.elementType in listOf(KDOC_LEADING_ASTERISK, KDOC_END, KDOC_SECTION)) {
            val expectedIndent = indentError.expected + 1
            return CheckResult.from(indentError.actual, expectedIndent)
        }
        return null
    }
}

/**
 * This checker indents all super types of class by one INDENT_SIZE or by two if colon is on a new line
 * If class declaration has supertype list, then it should have a colon before it, therefore UnsafeCallOnNullableType inspection is suppressed
 */
@Suppress("UnsafeCallOnNullableType")
internal class SuperTypeListChecker(config: IndentationConfig) : CustomIndentationChecker(config) {
    override fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationError): CheckResult? {
        if (whiteSpace.nextSibling.node.elementType == SUPER_TYPE_LIST) {
            val hasNewlineBeforeColon = whiteSpace.node.prevSibling { it.elementType == COLON }!!
                    .treePrev.takeIf { it.elementType == WHITE_SPACE }?.textContains('\n') ?: false
            val expectedIndent = indentError.expected + (if (hasNewlineBeforeColon) 2 else 1) * configuration.indentationSize
            return CheckResult.from(indentError.actual, expectedIndent)
        } else if (whiteSpace.parent.node.elementType == SUPER_TYPE_LIST) {
            val expectedIndent = whiteSpace.parentIndent() ?: (indentError.expected + configuration.indentationSize)
            return CheckResult.from(indentError.actual, expectedIndent)
        }
        return null
    }
}

/**
 * This checker performs the following check: When dot call start on a new line, it should be indented by [IndentationConfig.indentationSize]
 */
internal class DotCallChecker(config: IndentationConfig) : CustomIndentationChecker(config) {
    override fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationError): CheckResult? {
        whiteSpace.nextSibling.node.takeIf {
            it.elementType in listOf(DOT, SAFE_ACCESS) && it.treeNext.elementType in listOf(CALL_EXPRESSION, REFERENCE_EXPRESSION)
        }?.let {
            return CheckResult.from(indentError.actual, (whiteSpace.parentIndent()
                    ?: indentError.expected) + configuration.indentationSize, true)
        }
        return null
    }
}

/**
 * This [CustomIndentationChecker] checks indentation in loops and if-else expressions without braces around body.
 */
internal class ConditionalsAndLoopsWithoutBracesChecker(config: IndentationConfig) : CustomIndentationChecker(config) {
    override fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationError): CheckResult? {
        val parent = whiteSpace.parent
        val nextNode = whiteSpace.nextSibling.node
        return when (parent) {
            is KtLoopExpression -> nextNode.elementType == BODY && parent.body !is KtBlockExpression
            is KtIfExpression -> nextNode.elementType.let { it == THEN || it == ELSE } && parent.then !is KtBlockExpression
            else -> false
        }
                .takeIf { it }
                ?.let {
                    CheckResult.from(indentError.actual, (whiteSpace.parentIndent()
                            ?: indentError.expected) + configuration.indentationSize, false)
                }
    }
}

internal fun PsiElement.parentIndent(): Int? = parentsWithSelf
        .map { parent ->
            parent.node.prevSibling { it.elementType == WHITE_SPACE && it.textContains('\n') }
        }
        .filterNotNull()
        .firstOrNull()
        ?.text
        ?.lastIndent()
