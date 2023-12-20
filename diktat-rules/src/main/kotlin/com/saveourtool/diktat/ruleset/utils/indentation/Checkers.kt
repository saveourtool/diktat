/**
 * Implementations of CustomIndentationChecker for IndentationRule
 */

package com.saveourtool.diktat.ruleset.utils.indentation

import com.saveourtool.diktat.ruleset.rules.chapter3.files.IndentationAmount
import com.saveourtool.diktat.ruleset.rules.chapter3.files.IndentationAmount.SINGLE
import com.saveourtool.diktat.ruleset.rules.chapter3.files.IndentationError
import com.saveourtool.diktat.ruleset.utils.hasParent
import com.saveourtool.diktat.ruleset.utils.isBooleanExpression
import com.saveourtool.diktat.ruleset.utils.isDotBeforeCallOrReference
import com.saveourtool.diktat.ruleset.utils.isElvisOperationReference
import com.saveourtool.diktat.ruleset.utils.isLongStringTemplateEntry
import com.saveourtool.diktat.ruleset.utils.lastIndent
import com.saveourtool.diktat.ruleset.utils.nextCodeSibling
import com.saveourtool.diktat.ruleset.utils.prevSibling

import org.jetbrains.kotlin.KtNodeTypes.BINARY_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.BINARY_WITH_TYPE
import org.jetbrains.kotlin.KtNodeTypes.BODY
import org.jetbrains.kotlin.KtNodeTypes.DOT_QUALIFIED_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.ELSE
import org.jetbrains.kotlin.KtNodeTypes.IS_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.LONG_STRING_TEMPLATE_ENTRY
import org.jetbrains.kotlin.KtNodeTypes.OPERATION_REFERENCE
import org.jetbrains.kotlin.KtNodeTypes.SAFE_ACCESS_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.SUPER_TYPE_LIST
import org.jetbrains.kotlin.KtNodeTypes.THEN
import org.jetbrains.kotlin.KtNodeTypes.VALUE_ARGUMENT
import org.jetbrains.kotlin.KtNodeTypes.VALUE_ARGUMENT_LIST
import org.jetbrains.kotlin.KtNodeTypes.VALUE_PARAMETER
import org.jetbrains.kotlin.KtNodeTypes.VALUE_PARAMETER_LIST
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens
import org.jetbrains.kotlin.kdoc.parser.KDocElementTypes
import org.jetbrains.kotlin.lexer.KtTokens.ARROW
import org.jetbrains.kotlin.lexer.KtTokens.AS_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.AS_SAFE
import org.jetbrains.kotlin.lexer.KtTokens.BLOCK_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.COLON
import org.jetbrains.kotlin.lexer.KtTokens.ELVIS
import org.jetbrains.kotlin.lexer.KtTokens.EOL_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.EQ
import org.jetbrains.kotlin.lexer.KtTokens.LPAR
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtLoopExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor
import org.jetbrains.kotlin.psi.KtWhenEntry
import org.jetbrains.kotlin.psi.psiUtil.children
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf
import org.jetbrains.kotlin.psi.psiUtil.siblings

/**
 * Performs the following check: assignment operator increases indent by one step for the expression after it.
 * If [IndentationConfig.extendedIndentForExpressionBodies] is set to `true`, indentation is increased by two steps instead.
 */
internal class AssignmentOperatorChecker(configuration: IndentationConfig) : CustomIndentationChecker(configuration) {
    override fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationError): CheckResult? {
        whiteSpace.prevSibling?.node?.let { prevNode ->
            if (prevNode.elementType == EQ && prevNode.treeNext.let { it.elementType == WHITE_SPACE && it.textContains('\n') }) {
                return CheckResult.from(indentError.actual, (whiteSpace.parentIndent()
                    ?: indentError.expected) + IndentationAmount.valueOf(configuration.extendedIndentForExpressionBodies), true)
            }
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
                @Suppress("PARAMETER_NAME_IN_OUTER_LAMBDA")
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
                indentError.expected + SINGLE
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
    override fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationError): CheckResult? =
        when {
            whiteSpace.parent.node.elementType in sequenceOf(BINARY_EXPRESSION, BINARY_WITH_TYPE) &&
                    whiteSpace.immediateSiblings().any { sibling ->
                        /*
                         * We're looking for an operation reference, including
                         * `as` and `as?` (`AS_SAFE`), but excluding `?:` (`ELVIS`),
                         * because there's a separate flag for Elvis expressions
                         * in IDEA (`CONTINUATION_INDENT_IN_ELVIS`).
                         */
                        sibling.node.elementType == OPERATION_REFERENCE &&
                                sibling.node.children().firstOrNull()?.elementType != ELVIS
                    } -> {
                val parentIndent = whiteSpace.parentIndent() ?: indentError.expected
                val expectedIndent = parentIndent + IndentationAmount.valueOf(configuration.extendedIndentAfterOperators)
                CheckResult.from(indentError.actual, expectedIndent, true)
            }

            else -> null
        }
}

/**
 * In KDoc leading asterisks should be indented with one additional space
 */
internal class KdocIndentationChecker(config: IndentationConfig) : CustomIndentationChecker(config) {
    override fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationError): CheckResult? {
        if (whiteSpace.nextSibling.node.elementType in listOf(KDocTokens.LEADING_ASTERISK, KDocTokens.END, KDocElementTypes.KDOC_SECTION)) {
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
            val expectedIndent = indentError.expected + IndentationAmount.valueOf(extendedIndent = hasNewlineBeforeColon)
            return CheckResult.from(indentError.actual, expectedIndent)
        } else if (whiteSpace.parent.node.elementType == SUPER_TYPE_LIST) {
            val expectedIndent = whiteSpace.parentIndent() ?: (indentError.expected + SINGLE)
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
    /**
     * @param nextNodePredicate the predicate which the next non-comment
     *   non-whitespace node should satisfy.
     * @return `true` if this is a comment node which is immediately preceding
     *   the node specified by [nextNodePredicate].
     */
    private fun ASTNode.isCommentBefore(nextNodePredicate: ASTNode.() -> Boolean): Boolean {
        if (elementType in sequenceOf(EOL_COMMENT, BLOCK_COMMENT)) {
            var nextNode: ASTNode? = treeNext
            while (nextNode != null && nextNode.elementType in sequenceOf(WHITE_SPACE, EOL_COMMENT)) {
                nextNode = nextNode.treeNext
            }
            return nextNode?.nextNodePredicate() ?: false
        }
        return false
    }

    private fun ASTNode.isElvisReferenceOrCommentBeforeElvis(): Boolean =
        isElvisOperationReference() ||
                isCommentBefore(ASTNode::isElvisOperationReference)

    private fun ASTNode.isFromStringTemplate(): Boolean =
        hasParent(LONG_STRING_TEMPLATE_ENTRY)

    @Suppress(
        "ComplexMethod",
        "TOO_LONG_FUNCTION",
    )
    override fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationError): CheckResult? {
        whiteSpace.nextSibling
            .node
            .takeIf { nextNode ->
                (nextNode.isDotBeforeCallOrReference() ||
                        nextNode.elementType == OPERATION_REFERENCE &&
                                nextNode.firstChildNode.elementType in sequenceOf(ELVIS, IS_EXPRESSION, AS_KEYWORD, AS_SAFE) ||
                        nextNode.isCommentBefore(ASTNode::isDotBeforeCallOrReference) ||
                        nextNode.isCommentBefore(ASTNode::isElvisOperationReference)) &&
                        whiteSpace.parents.none(PsiElement::isLongStringTemplateEntry)
            }
            /*-
             * Here, `node` is any of:
             *
             *  - a `DOT` or a `SAFE_ACCESS`,
             *  - an `OPERATION_REFERENCE` with `ELVIS` as the only child, or
             */
            ?.let { node ->
                val indentIncrement = IndentationAmount.valueOf(configuration.extendedIndentBeforeDot)
                if (node.isFromStringTemplate()) {
                    return CheckResult.from(indentError.actual, indentError.expected +
                            indentIncrement, true)
                }

                /*-
                 * The list of immediate parents of this whitespace node,
                 * nearest-to-farthest order
                 * (the farthest parent is the file node).
                 */
                val parentExpressions = whiteSpace.parents.takeWhile { parent ->
                    val parentType = parent.node.elementType

                    when {
                        /*
                         * #1532, 1.2.4+: if this is an Elvis operator
                         * (OPERATION_REFERENCE -> ELVIS), or an EOL or a
                         * block comment which immediately precedes this
                         * Elvis operator, then the indent of the parent
                         * binary expression should be used as a base for
                         * the increment.
                         */
                        node.isElvisReferenceOrCommentBeforeElvis() -> parentType == BINARY_EXPRESSION

                        /*
                         * Pre-1.2.4 behaviour, all other cases: the indent
                         * of the parent dot-qualified or safe-access
                         * expression should be used as a base for the
                         * increment.
                         */
                        else -> parentType in sequenceOf(
                            DOT_QUALIFIED_EXPRESSION,
                            SAFE_ACCESS_EXPRESSION,
                        )
                    }
                }.toList()

                /*
                 * Selects from the matching parent nodes.
                 */
                val matchOrNull: Iterable<PsiElement>.() -> PsiElement? = {
                    when {
                        /*
                         * Selects nearest.
                         */
                        node.isElvisReferenceOrCommentBeforeElvis() -> firstOrNull()

                        /*
                         * Selects farthest.
                         */
                        else -> lastOrNull()
                    }
                }

                // we need to get indent before the first expression in calls chain
                /*-
                 * If the parent indent (the one before a `DOT_QUALIFIED_EXPRESSION`
                 * or a `SAFE_ACCESS_EXPRESSION`) is `null`, then use 0 as the
                 * fallback value.
                 *
                 * If `indentError.expected` is used as a fallback (pre-1.2.2
                 * behaviour), this breaks chained dot-qualified or safe-access
                 * expressions (see #1336), e.g.:
                 *
                 * ```kotlin
                 * val a = first()
                 *     .second()
                 *     .third()
                 * ```
                 */
                val parentIndent = (parentExpressions.matchOrNull() ?: whiteSpace).parentIndent()
                    ?: 0

                val expectedIndent = when {
                    /*-
                     * Don't indent Elvis expressions (and the corresponding comments)
                     * which are nested inside boolean expressions:
                     *
                     * ```kotlin
                     * val x = true &&
                     *         ""
                     *             ?.isEmpty()
                     *         ?: true
                     * ```
                     *
                     * This is a special case, and this is how IDEA formats source code.
                     */
                    node.isElvisReferenceOrCommentBeforeElvis() &&
                            parentExpressions.any { it.node.isBooleanExpression() } -> parentIndent

                    /*-
                     * All other cases (dot-qualified, safe-access, Elvis).
                     * Expression parts are indented regularly, e.g.:
                     *
                     * ```kotlin
                     * val a = null as Boolean?
                     *     ?: true
                     * ```
                     */
                    else -> parentIndent + indentIncrement
                }

                return CheckResult.from(indentError.actual, expectedIndent, true)
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
                CheckResult.from(indentError.actual, indentError.expected + SINGLE, true)
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
                ?: indentError.expected) + SINGLE, true)
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
                ?: indentError.expected) + SINGLE, true)
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

/**
 * @return the sequence of immediate siblings (the previous and the next one),
 *   excluding `null`'s.
 */
private fun PsiElement.immediateSiblings(): Sequence<PsiElement> =
    sequenceOf(prevSibling, nextSibling).filterNotNull()
