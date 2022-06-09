/**
 * Implementations of CustomIndentationChecker for IndentationRule
 */

package org.cqfn.diktat.ruleset.utils.indentation

import org.cqfn.diktat.ruleset.rules.chapter3.files.IndentationError
import org.cqfn.diktat.ruleset.rules.chapter3.files.lastIndent
import org.cqfn.diktat.ruleset.utils.hasParent

import com.pinterest.ktlint.core.ast.ElementType.ARROW
import com.pinterest.ktlint.core.ast.ElementType.AS_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.AS_SAFE
import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.BODY
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.COLON
import com.pinterest.ktlint.core.ast.ElementType.DOT
import com.pinterest.ktlint.core.ast.ElementType.ELSE
import com.pinterest.ktlint.core.ast.ElementType.ELVIS
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.EQ
import com.pinterest.ktlint.core.ast.ElementType.IS_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.KDOC_END
import com.pinterest.ktlint.core.ast.ElementType.KDOC_LEADING_ASTERISK
import com.pinterest.ktlint.core.ast.ElementType.KDOC_SECTION
import com.pinterest.ktlint.core.ast.ElementType.LONG_STRING_TEMPLATE_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.LPAR
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.SAFE_ACCESS
import com.pinterest.ktlint.core.ast.ElementType.SUPER_TYPE_LIST
import com.pinterest.ktlint.core.ast.ElementType.THEN
import com.pinterest.ktlint.core.ast.ElementType.VALUE_ARGUMENT
import com.pinterest.ktlint.core.ast.ElementType.VALUE_ARGUMENT_LIST
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER_LIST
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.nextCodeSibling
import com.pinterest.ktlint.core.ast.prevSibling
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtLoopExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtSafeQualifiedExpression
import org.jetbrains.kotlin.psi.KtWhenEntry
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf
import org.jetbrains.kotlin.psi.psiUtil.siblings

/**
 * Performs the following check: assignment operator increases indent by one step for the expression after it.
 * If [IndentationConfig.extendedIndentAfterOperators] is set to true, indentation is increased by two steps instead.
 */
internal class AssignmentOperatorChecker(configuration: IndentationConfig) : CustomIndentationChecker(configuration) {
    override fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationError): CheckResult? {
        val prevNode = whiteSpace.prevSibling?.node
        if (prevNode?.elementType == EQ && prevNode.treeNext.let { it.elementType == WHITE_SPACE && it.textContains('\n') }) {
            return CheckResult.from(indentError.actual, (whiteSpace.parentIndent()
                ?: indentError.expected) + (if (configuration.extendedIndentAfterOperators) 2 else 1) * configuration.indentationSize, true)
        }
        return null
    }
}

/**
 * Performs the following check: When breaking parameter list of a method/class constructor it can be aligned with 8 spaces
 * or in a method/class declaration a parameter that was moved to a newline can be on the same level as the previous argument.
 */
@Suppress("ForbiddenComment")
internal class ValueParameterListChecker(configuration: IndentationConfig) : CustomIndentationChecker(configuration) {
    /**
     * This check triggers if the following conditions are met:
     * 1. line break is inside value parameter or value argument list (function declaration or invocation)
     * 2. there are no other line breaks before this node
     * 3. there are no more arguments after this node
     */
    private fun isCheckNeeded(whiteSpace: PsiWhiteSpace) =
        whiteSpace.parent
            .node
            .elementType
            .let { it == VALUE_PARAMETER_LIST || it == VALUE_ARGUMENT_LIST } &&
            whiteSpace.siblings(forward = false, withItself = false).none { it is PsiWhiteSpace && it.textContains('\n') } &&
            // no need to trigger when there are no more parameters in the list
            whiteSpace.siblings(forward = true, withItself = false).any {
                it.node.elementType.run { this == VALUE_ARGUMENT || this == VALUE_PARAMETER }
            }

    override fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationError): CheckResult? {
        if (isCheckNeeded(whiteSpace)) {
            val parameterList = whiteSpace.parent.node
            // parameters in lambdas are VALUE_PARAMETER_LIST and might have no LPAR: list { elem -> ... }
            val parameterAfterLpar = parameterList
                .findChildByType(LPAR)
                ?.treeNext
                ?.takeIf {
                    it.elementType != WHITE_SPACE &&
                        // there can be multiline arguments and in this case we don't align parameters with them
                        !it.textContains('\n')
                }

            val expectedIndent = if (parameterAfterLpar != null && configuration.alignedParameters && parameterList.elementType == VALUE_PARAMETER_LIST) {
                val ktFile = whiteSpace.parents.last() as KtFile
                // count column number of the first parameter
                ktFile.text
                    .lineSequence()
                    // calculate offset for every line end, `+1` for `\n` which is trimmed in `lineSequence`
                    .scan(0 to "") { (length, _), line -> length + line.length + 1 to line }
                    .run {
                        // find the line where `parameterAfterLpar` resides
                        find { it.first > parameterAfterLpar.startOffset } ?: last()
                    }
                    .let { (_, line) -> line.substringBefore(parameterAfterLpar.text).length }
            } else if (configuration.extendedIndentOfParameters) {
                indentError.expected + configuration.indentationSize
            } else {
                indentError.expected
            }

            return CheckResult.from(indentError.actual, expectedIndent, adjustNext = true, includeLastChild = false)
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
            return CheckResult.from(indentError.actual, expectedIndent, true)
        }
        return null
    }
}

/**
 * In KDoc leading asterisks should be indented with one additional space
 */
internal class KdocIndentationChecker(config: IndentationConfig) : CustomIndentationChecker(config) {
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
            val hasNewlineBeforeColon = whiteSpace.node
                .prevSibling { it.elementType == COLON }!!
                .treePrev
                .takeIf { it.elementType == WHITE_SPACE }
                ?.textContains('\n') ?: false
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
 * This checker performs the following check: When dot call start on a new line, it should be indented by [IndentationConfig.indentationSize].
 * Same is true for safe calls (`?.`) and elvis operator (`?:`).
 */
internal class DotCallChecker(config: IndentationConfig) : CustomIndentationChecker(config) {
    private fun ASTNode.isDotBeforeCallOrReference() = elementType.let { it == DOT || it == SAFE_ACCESS } &&
        treeNext.elementType.let { it == CALL_EXPRESSION || it == REFERENCE_EXPRESSION }

    private fun ASTNode.isCommentBeforeDot(): Boolean {
        if (elementType == EOL_COMMENT || elementType == BLOCK_COMMENT) {
            var nextNode: ASTNode? = treeNext
            while (nextNode != null && (nextNode.elementType == WHITE_SPACE || nextNode.elementType == EOL_COMMENT)) {
                nextNode = nextNode.treeNext
            }
            return nextNode?.isDotBeforeCallOrReference() ?: false
        }
        return false
    }

    private fun ASTNode.isFromStringTemplate(): Boolean =
        hasParent(LONG_STRING_TEMPLATE_ENTRY)

    @Suppress("ComplexMethod")
    override fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationError): CheckResult? {
        whiteSpace.nextSibling
            .node
            .takeIf { nextNode ->
                (nextNode.isDotBeforeCallOrReference() ||
                    nextNode.elementType == OPERATION_REFERENCE && nextNode.firstChildNode.elementType.let { type ->
                        type == ELVIS || type == IS_EXPRESSION || type == AS_KEYWORD || type == AS_SAFE
                    } || nextNode.isCommentBeforeDot()) && whiteSpace.parents.none { it.node.elementType == LONG_STRING_TEMPLATE_ENTRY }
            }
            ?.let { node ->
                if (node.isFromStringTemplate()) {
                    return CheckResult.from(indentError.actual, indentError.expected +
                        (if (configuration.extendedIndentBeforeDot) 2 else 1) * configuration.indentationSize, true)
                }

                // we need to get indent before the first expression in calls chain
                return CheckResult.from(indentError.actual, (whiteSpace.run {
                    parents.takeWhile { it is KtDotQualifiedExpression || it is KtSafeQualifiedExpression }.lastOrNull() ?: this
                }
                    .parentIndent()
                    ?: indentError.expected) +
                    (if (configuration.extendedIndentBeforeDot) 2 else 1) * configuration.indentationSize, true)
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
        val nextNode = whiteSpace.node.nextCodeSibling()  // if there is comment after if or else, it should be indented too
        return when (parent) {
            is KtLoopExpression -> nextNode?.elementType == BODY && parent.body !is KtBlockExpression
            is KtIfExpression -> nextNode?.elementType == THEN && parent.then !is KtBlockExpression ||
                nextNode?.elementType == ELSE && parent.`else`.let { it !is KtBlockExpression && it !is KtIfExpression }
            else -> false
        }
            .takeIf { it }
            ?.let {
                CheckResult.from(indentError.actual, indentError.expected + configuration.indentationSize, true)
            }
    }
}

/**
 * This [CustomIndentationChecker] check indentation before custom getters and setters on property.
 */
internal class CustomGettersAndSettersChecker(config: IndentationConfig) : CustomIndentationChecker(config) {
    override fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationError): CheckResult? {
        val parent = whiteSpace.parent
        if (parent is KtProperty && whiteSpace.nextSibling is KtPropertyAccessor) {
            return CheckResult.from(indentError.actual, (parent.parentIndent()
                ?: indentError.expected) + configuration.indentationSize, true)
        }
        return null
    }
}

/**
 * Performs the following check: arrow in `when` expression increases indent by one step for the expression after it.
 */
internal class ArrowInWhenChecker(configuration: IndentationConfig) : CustomIndentationChecker(configuration) {
    override fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationError): CheckResult? {
        val prevNode = whiteSpace.prevSibling?.node
        if (prevNode?.elementType == ARROW && whiteSpace.parent is KtWhenEntry) {
            return CheckResult.from(indentError.actual, (whiteSpace.parentIndent()
                ?: indentError.expected) + configuration.indentationSize, true)
        }
        return null
    }
}

/**
 * @return indentation of parent node
 */
internal fun PsiElement.parentIndent(): Int? = parentsWithSelf
    .map { parent ->
        parent.node.prevSibling { it.elementType == WHITE_SPACE && it.textContains('\n') }
    }
    .filterNotNull()
    .firstOrNull()
    ?.text
    ?.lastIndent()
