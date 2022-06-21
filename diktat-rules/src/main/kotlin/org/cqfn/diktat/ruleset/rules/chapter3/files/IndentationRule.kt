/**
 * Main logic of indentation including Rule and utility classes and methods.
 */

package org.cqfn.diktat.ruleset.rules.chapter3.files

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.NEWLINE
import org.cqfn.diktat.ruleset.utils.SPACE
import org.cqfn.diktat.ruleset.utils.TAB
import org.cqfn.diktat.ruleset.utils.calculateLineColByOffset
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.getAllLeafsWithSpecificType
import org.cqfn.diktat.ruleset.utils.getFilePath
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.indentBy
import org.cqfn.diktat.ruleset.utils.indentation.ArrowInWhenChecker
import org.cqfn.diktat.ruleset.utils.indentation.AssignmentOperatorChecker
import org.cqfn.diktat.ruleset.utils.indentation.ConditionalsAndLoopsWithoutBracesChecker
import org.cqfn.diktat.ruleset.utils.indentation.CustomGettersAndSettersChecker
import org.cqfn.diktat.ruleset.utils.indentation.CustomIndentationChecker
import org.cqfn.diktat.ruleset.utils.indentation.DotCallChecker
import org.cqfn.diktat.ruleset.utils.indentation.ExpressionIndentationChecker
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig
import org.cqfn.diktat.ruleset.utils.indentation.KdocIndentationChecker
import org.cqfn.diktat.ruleset.utils.indentation.SuperTypeListChecker
import org.cqfn.diktat.ruleset.utils.indentation.ValueParameterListChecker
import org.cqfn.diktat.ruleset.utils.isSpaceCharacter
import org.cqfn.diktat.ruleset.utils.lastIndent
import org.cqfn.diktat.ruleset.utils.leaveOnlyOneNewLine

import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.CLOSING_QUOTE
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.ELSE
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.LBRACKET
import com.pinterest.ktlint.core.ast.ElementType.LITERAL_STRING_TEMPLATE_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.LONG_STRING_TEMPLATE_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.LONG_TEMPLATE_ENTRY_END
import com.pinterest.ktlint.core.ast.ElementType.LONG_TEMPLATE_ENTRY_START
import com.pinterest.ktlint.core.ast.ElementType.LPAR
import com.pinterest.ktlint.core.ast.ElementType.RBRACE
import com.pinterest.ktlint.core.ast.ElementType.RBRACKET
import com.pinterest.ktlint.core.ast.ElementType.REGULAR_STRING_PART
import com.pinterest.ktlint.core.ast.ElementType.RPAR
import com.pinterest.ktlint.core.ast.ElementType.SAFE_ACCESS_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.SHORT_STRING_TEMPLATE_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.STRING_TEMPLATE
import com.pinterest.ktlint.core.ast.ElementType.THEN
import com.pinterest.ktlint.core.ast.ElementType.VALUE_ARGUMENT
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.visit
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.com.intellij.util.containers.Stack
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtLoopExpression
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.slf4j.LoggerFactory

import kotlin.math.abs

/**
 * Rule that checks indentation. The following general rules are checked:
 * 1. Only spaces should be used each indentation is equal to 4 spaces
 * 2. File should end with new line
 * Additionally, a set of CustomIndentationChecker objects checks all WHITE_SPACE node if they are exceptions from general rules.
 * @see CustomIndentationChecker
 */
@Suppress("LargeClass")
class IndentationRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(WRONG_INDENTATION)
) {
    private val configuration: IndentationConfig by lazy {
        IndentationConfig(configRules.getRuleConfig(WRONG_INDENTATION)?.configuration ?: emptyMap())
    }
    private lateinit var filePath: String
    private lateinit var customIndentationCheckers: List<CustomIndentationChecker>
    private lateinit var positionByOffset: (Int) -> Pair<Int, Int>

    override fun logic(node: ASTNode) {
        if (node.elementType == FILE) {
            filePath = node.getFilePath()

            customIndentationCheckers = listOf(
                ::AssignmentOperatorChecker,
                ::ConditionalsAndLoopsWithoutBracesChecker,
                ::SuperTypeListChecker,
                ::ValueParameterListChecker,
                ::ExpressionIndentationChecker,
                ::DotCallChecker,
                ::KdocIndentationChecker,
                ::CustomGettersAndSettersChecker,
                ::ArrowInWhenChecker
            ).map { it.invoke(configuration) }

            if (checkIsIndentedWithSpaces(node)) {
                checkIndentation(node)
            } else {
                log.warn("Not going to check indentation because there are tabs")
            }
            checkNewlineAtEnd(node)
        }
    }

    /**
     * This method warns if tabs are used in WHITE_SPACE nodes and substitutes them with spaces in fix mode
     *
     * @return true if there are no tabs or all of them have been fixed, false otherwise
     */
    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    private fun checkIsIndentedWithSpaces(node: ASTNode): Boolean {
        val whiteSpaceNodes: MutableList<ASTNode> = mutableListOf()
        node.getAllLeafsWithSpecificType(WHITE_SPACE, whiteSpaceNodes)
        whiteSpaceNodes
            .filter { it.textContains(TAB) }
            .apply {
                if (isEmpty()) {
                    return true
                }
            }
            .forEach {
                WRONG_INDENTATION.warnAndFix(configRules, emitWarn, isFixMode, "tabs are not allowed for indentation", it.startOffset + it.text.indexOf(TAB), it) {
                    (it as LeafPsiElement).rawReplaceWithText(it.text.replace(TAB.toString(), configuration.indentationSize.spaces))
                }
            }
        return isFixMode  // true if we changed all tabs to spaces
    }

    /**
     * Checks that file ends with exactly one empty line
     */
    private fun checkNewlineAtEnd(node: ASTNode) {
        if (configuration.newlineAtEnd) {
            val lastChild = generateSequence(node) { it.lastChildNode }.last()
            val numBlankLinesAfter = lastChild.text.count { it == NEWLINE }
            if (lastChild.elementType != WHITE_SPACE || numBlankLinesAfter != 1) {
                val warnText = if (lastChild.elementType != WHITE_SPACE || numBlankLinesAfter == 0) "no newline" else "too many blank lines"
                val fileName = filePath.substringAfterLast(File.separator)
                // In case, when last child is newline, visually user will see blank line at the end of file,
                // however, the text length does not consider it, since it's blank and line appeared only because of `\n`
                // But ktlint synthetically increase length in aim to have ability to point to this line, so in this case
                // offset will be `node.textLength`, otherwise we will point to the last symbol, i.e `node.textLength - 1`
                val offset = if (lastChild.elementType == WHITE_SPACE && lastChild.textContains(NEWLINE)) node.textLength else node.textLength - 1
                WRONG_INDENTATION.warnAndFix(configRules, emitWarn, isFixMode, "$warnText at the end of file $fileName", offset, node) {
                    if (lastChild.elementType != WHITE_SPACE) {
                        node.addChild(PsiWhiteSpaceImpl(NEWLINE.toString()), null)
                    } else {
                        lastChild.leaveOnlyOneNewLine()
                    }
                }
            }
        }
    }

    /**
     * Traverses the tree, keeping track of regular and exceptional indentations
     */
    private fun checkIndentation(node: ASTNode) {
        val context = IndentContext(configuration)
        node.visit { astNode ->
            context.checkAndReset(astNode)
            if (astNode.elementType in increasingTokens) {
                context.storeIncrementingToken(astNode.elementType)
            } else if (astNode.elementType in decreasingTokens && !astNode.treePrev.let { it.elementType == WHITE_SPACE && it.textContains(NEWLINE) }) {
                // if decreasing token is after WHITE_SPACE with \n, indents are corrected in visitWhiteSpace method
                context.dec(astNode.elementType)
            } else if (astNode.elementType == WHITE_SPACE && astNode.textContains(NEWLINE) && astNode.treeNext != null) {
                // we check only WHITE_SPACE nodes with newlines, other than the last line in file; correctness of newlines should be checked elsewhere
                visitWhiteSpace(astNode, context)
            }
        }
    }

    private fun isCloseAndOpenQuoterOffset(nodeWhiteSpace: ASTNode, expectedIndent: Int): Boolean {
        val nextNode = nodeWhiteSpace.treeNext
        if (nextNode.elementType == VALUE_ARGUMENT) {
            val nextNodeDot = getNextDotExpression(nextNode)
            nextNodeDot?.getFirstChildWithType(STRING_TEMPLATE)?.let {
                if (it.getAllChildrenWithType(LITERAL_STRING_TEMPLATE_ENTRY).size > 1) {
                    val closingQuote = it.getFirstChildWithType(CLOSING_QUOTE)?.treePrev?.text
                        ?.length ?: -1
                    return expectedIndent == closingQuote
                }
            }
        }
        return true
    }

    @Suppress("ForbiddenComment")
    private fun visitWhiteSpace(astNode: ASTNode, context: IndentContext) {
        context.maybeIncrement()
        positionByOffset = astNode.treeParent.calculateLineColByOffset()
        val whiteSpace = astNode.psi as PsiWhiteSpace
        if (astNode.treeNext.elementType in decreasingTokens) {
            // if newline is followed by closing token, it should already be indented less
            context.dec(astNode.treeNext.elementType)
        }

        val indentError = IndentationError(context.indent(), astNode.text.lastIndent())

        val checkResult = customIndentationCheckers.firstNotNullOfOrNull {
            it.checkNode(whiteSpace, indentError)
        }

        val expectedIndent = checkResult?.expectedIndent ?: indentError.expected
        if (checkResult?.adjustNext == true && astNode.parents().none { it.elementType == LONG_STRING_TEMPLATE_ENTRY }) {
            val exceptionInitiatorNode = astNode.getExceptionalIndentInitiator()
            context.addException(exceptionInitiatorNode, expectedIndent - indentError.expected, checkResult.includeLastChild)
        }

        if (astNode.treeParent.elementType == LONG_STRING_TEMPLATE_ENTRY && indentError.expected != indentError.actual) {
            context.addException(astNode.treeParent, abs(indentError.expected - indentError.actual), false)
        }

        val difOffsetCloseAndOpenQuote = isCloseAndOpenQuoterOffset(astNode, indentError.actual)

        if ((checkResult?.isCorrect != true && expectedIndent != indentError.actual) || !difOffsetCloseAndOpenQuote) {
            val warnText = if (!difOffsetCloseAndOpenQuote) {
                "the same number of indents to the opening and closing quotes was expected"
            } else {
                "expected $expectedIndent but was ${indentError.actual}"
            }
            WRONG_INDENTATION.warnAndFix(configRules, emitWarn, isFixMode, warnText,
                whiteSpace.startOffset + whiteSpace.text.lastIndexOf(NEWLINE) + 1, whiteSpace.node) {
                checkStringLiteral(whiteSpace, expectedIndent, indentError.actual)
                whiteSpace.node.indentBy(expectedIndent)
            }
        }
    }

    /**
     * Checks if it is triple-quoted string template with trimIndent() or trimMargin() function.
     */
    private fun checkStringLiteral(
        whiteSpace: PsiWhiteSpace,
        expectedIndent: Int,
        actualIndent: Int
    ) {
        val nextNodeDot = getNextDotExpression(whiteSpace.node.treeNext)
        if (nextNodeDot != null &&
            nextNodeDot.elementType == DOT_QUALIFIED_EXPRESSION &&
            nextNodeDot.firstChildNode.elementType == STRING_TEMPLATE &&
            nextNodeDot.firstChildNode.text.startsWith("\"\"\"") &&
            nextNodeDot.findChildByType(CALL_EXPRESSION)?.text?.let {
                it == "trimIndent()" ||
                    it == "trimMargin()"
            } == true) {
            fixStringLiteral(nextNodeDot.firstChildNode, expectedIndent, actualIndent)
        }
    }

    /**
     * Indents each [entry][LITERAL_STRING_TEMPLATE_ENTRY] in a (potentially,
     * multi-line) triple-quoted [string template][STRING_TEMPLATE].
     *
     * String templates usually have the following structure:
     *
     * * `STRING_TEMPLATE`
     *    * `OPEN_QUOTE`
     *    * `LITERAL_STRING_TEMPLATE_ENTRY`
     *       * `REGULAR_STRING_PART`
     *    * &#x2026;
     *    * `LITERAL_STRING_TEMPLATE_ENTRY`
     *       * `REGULAR_STRING_PART`
     *    * `CLOSING_QUOTE`
     *
     * @param stringTemplate the string template.
     * @see STRING_TEMPLATE
     * @see LITERAL_STRING_TEMPLATE_ENTRY
     */
    @Suppress("LOCAL_VARIABLE_EARLY_DECLARATION")
    private fun fixStringLiteral(
        stringTemplate: ASTNode,
        expectedIndent: Int,
        actualIndent: Int
    ) {
        val templateEntries = stringTemplate.getAllChildrenWithType(LITERAL_STRING_TEMPLATE_ENTRY)
        templateEntries.asSequence().filterIndexed { index, templateEntry ->
            val text = templateEntry.text
            val containsNewline = text.contains(NEWLINE)

            if (containsNewline) {
                /*
                 * In real-life cases observed, whenever a `LITERAL_STRING_TEMPLATE_ENTRY`
                 * _contains_ a newline character, it is _exactly_ a newline character.
                 */
                check(text.length == 1) {
                    val escapedText = text.replace(NEWLINE.toString(), "\\n")

                    "A LITERAL_STRING_TEMPLATE_ENTRY at index $index contains extra characters in addition to the newline, " +
                        "entry: \"$escapedText\", " +
                        "string template: ${stringTemplate.text}"
                }
            }

            !containsNewline
        }.forEach { templateEntry ->
            fixFirstTemplateEntries(
                templateEntry,
                expectedIndent = expectedIndent,
                actualIndent = actualIndent)
        }

        /*
         * This is the last string template fragment which is usually followed
         * with the closing `"""` and the `.trimIndent()` or `.trimMargin()` call.
         */
        val lastRegularStringPart = templateEntries.last().firstChildNode as LeafPsiElement
        lastRegularStringPart.checkRegularStringPart().apply {
            val textWithoutIndent = text.trimStart()
            rawReplaceWithText(expectedIndent.spaces + textWithoutIndent)
        }
    }

    private fun getNextDotExpression(node: ASTNode) = if (node.elementType == DOT_QUALIFIED_EXPRESSION) {
        node
    } else {
        node.getFirstChildWithType(DOT_QUALIFIED_EXPRESSION)
    }

    /**
     * Modifies [templateEntry] by correcting its indentation level.
     *
     * This method can be used to fix all [lines][LITERAL_STRING_TEMPLATE_ENTRY]
     * of a [string template][STRING_TEMPLATE] except for the last one.
     *
     * Also, it considers `$foo` insertions in a string.
     *
     * @param templateEntry a [LITERAL_STRING_TEMPLATE_ENTRY] node.
     * @param expectedIndent the expected indent level, as returned by
     *   [IndentationError.expected].
     * @param actualIndent the actual indent level, as returned by
     *   [IndentationError.actual].
     */
    private fun fixFirstTemplateEntries(
        templateEntry: ASTNode,
        expectedIndent: Int,
        actualIndent: Int
    ) {
        require(templateEntry.elementType == LITERAL_STRING_TEMPLATE_ENTRY) {
            "The elementType of this node is ${templateEntry.elementType} while $LITERAL_STRING_TEMPLATE_ENTRY expected"
        }

        /*
         * Quite possible, do nothing in this case.
         */
        if (expectedIndent == actualIndent) {
            return
        }

        /*
         * A `REGULAR_STRING_PART`.
         */
        val regularStringPart = templateEntry.firstChildNode as LeafPsiElement
        val regularStringPartText = regularStringPart.checkRegularStringPart().text
        val nodeStartIndentOrNegative = regularStringPartText.leadingSpaceCount() - actualIndent - DEFAULT_INDENT_SIZE
        // shift of the node depending on its initial string template indent
        val nodeStartIndent = nodeStartIndentOrNegative.zeroIfNegative()

        val isPrevStringTemplate = templateEntry.treePrev.elementType in stringLiteralTokens
        val isNextStringTemplate = templateEntry.treeNext.elementType in stringLiteralTokens

        val correctedText = when {
            isPrevStringTemplate -> when {
                isNextStringTemplate -> regularStringPartText

                // if string template is before literal_string
                else -> regularStringPartText.trimEnd()

            }

            else -> {
                val textIndent = (expectedIndent + DEFAULT_INDENT_SIZE).spaces

                when {
                    // if string template is after literal_string
                    isNextStringTemplate -> textIndent + nodeStartIndent.spaces + regularStringPartText.trimStart()

                    // if there is no string template in literal_string
                    else -> textIndent + nodeStartIndent.spaces + regularStringPartText.trim()
                }
            }
        }

        regularStringPart.rawReplaceWithText(correctedText)
    }

    private fun ASTNode.getExceptionalIndentInitiator() = treeParent.let { parent ->
        when (parent.psi) {
            // fixme: custom logic for determining exceptional indent initiator, should be moved elsewhere
            // get the topmost expression to keep extended indent for the whole chain of dot call expressions
            is KtDotQualifiedExpression -> parents().takeWhile { it.elementType == DOT_QUALIFIED_EXPRESSION || it.elementType == SAFE_ACCESS_EXPRESSION }.last()
            is KtIfExpression -> parent.findChildByType(THEN) ?: parent.findChildByType(ELSE) ?: parent
            is KtLoopExpression -> (parent.psi as KtLoopExpression).body?.node ?: parent
            else -> parent
        }
    }

    /**
     * Class that contains state needed to calculate indent and keep track of exceptional indents.
     * Tokens from [increasingTokens] are stored in stack [activeTokens]. When [WHITE_SPACE] with line break is encountered,
     * if stack is not empty, indentation is increased. When token from [decreasingTokens] is encountered, it's counterpart is removed
     * from stack. If there has been a [WHITE_SPACE] with line break between them, indentation is decreased.
     */
    private class IndentContext(private val config: IndentationConfig) {
        private var regularIndent = 0
        private val exceptionalIndents: MutableList<ExceptionalIndent> = mutableListOf()
        private val activeTokens: Stack<IElementType> = Stack()

        /**
         * @param token a token that caused indentation increment, for example an opening brace
         * @return Unit
         */
        fun storeIncrementingToken(token: IElementType) = token
            .also { require(it in increasingTokens) { "Only tokens that increase indentation should be passed to this method" } }
            .let(activeTokens::push)

        /**
         * Checks whether indentation needs to be incremented and increments in this case.
         */
        fun maybeIncrement() {
            if (activeTokens.isNotEmpty() && activeTokens.peek() != WHITE_SPACE) {
                regularIndent += config.indentationSize
                activeTokens.push(WHITE_SPACE)
            }
        }

        /**
         * @param token a token that caused indentation decrement, for example a closing brace
         */
        fun dec(token: IElementType) {
            if (activeTokens.peek() == WHITE_SPACE) {
                while (activeTokens.peek() == WHITE_SPACE) {
                    activeTokens.pop()
                }
                regularIndent -= config.indentationSize
            }
            if (activeTokens.isNotEmpty() && activeTokens.peek() == matchingTokens.find { it.second == token }?.first) {
                activeTokens.pop()
            }
        }

        /**
         * @return full current indent
         */
        fun indent() = regularIndent + exceptionalIndents.sumOf { it.indent }

        /**
         * @param initiator a node that caused exceptional indentation
         * @param indent an additional indent
         * @param includeLastChild whether the last child node should be included in the range affected by exceptional indentation
         * @return true if add exception in exceptionalIndents
         */
        fun addException(
            initiator: ASTNode,
            indent: Int,
            includeLastChild: Boolean
        ) = exceptionalIndents.add(ExceptionalIndent(initiator, indent, includeLastChild))

        /**
         * @param astNode the node which is used to determine whether exceptional indents are still active
         * @return boolean result
         */
        fun checkAndReset(astNode: ASTNode) = exceptionalIndents.retainAll { it.isActive(astNode) }

        /**
         * @property initiator a node that caused exceptional indentation
         * @property indent an additional indent
         * @property includeLastChild whether the last child node should be included in the range affected by exceptional indentation
         */
        private data class ExceptionalIndent(
            val initiator: ASTNode,
            val indent: Int,
            val includeLastChild: Boolean = true
        ) {
            /**
             * Checks whether this exceptional indent is still active. This is a hypotheses that exceptional indentation will end
             * outside of node where it appeared, e.g. when an expression after assignment operator is over.
             *
             * @param currentNode the current node during AST traversal
             * @return boolean result
             */
            fun isActive(currentNode: ASTNode): Boolean = currentNode.psi.parentsWithSelf.any { it.node == initiator } &&
                (includeLastChild || currentNode.treeNext != initiator.lastChildNode)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(IndentationRule::class.java)

        /**
         * The default indent size (space characters), configurable via
         * `indentationSize`.
         */
        const val DEFAULT_INDENT_SIZE = 4
        const val NAME_ID = "zct-indentation"
        private val increasingTokens = listOf(LPAR, LBRACE, LBRACKET, LONG_TEMPLATE_ENTRY_START)
        private val decreasingTokens = listOf(RPAR, RBRACE, RBRACKET, LONG_TEMPLATE_ENTRY_END)
        private val matchingTokens = increasingTokens.zip(decreasingTokens)
        private val stringLiteralTokens = listOf(SHORT_STRING_TEMPLATE_ENTRY, LONG_STRING_TEMPLATE_ENTRY)

        /**
         * @return a string which consists of `N` [space][SPACE] characters.
         */
        @Suppress("CUSTOM_GETTERS_SETTERS")
        private val Int.spaces: String
            get() =
                SPACE.toString().repeat(n = this)

        /**
         * Checks this [REGULAR_STRING_PART] child of a [LITERAL_STRING_TEMPLATE_ENTRY].
         *
         * @return this `REGULAR_STRING_PART` PSI element.
         */
        private fun LeafPsiElement.checkRegularStringPart(): LeafPsiElement {
            val lastRegularStringPartType = elementType

            check(lastRegularStringPartType == REGULAR_STRING_PART) {
                "Unexpected type of the 1st child of the string template entry, " +
                    "expected: $REGULAR_STRING_PART, " +
                    "actual: $lastRegularStringPartType, " +
                    "string template: ${parent.parent.text}"
            }

            return this
        }

        /**
         * @return the number of leading space characters in this string.
         */
        private fun String.leadingSpaceCount(): Int =
            asSequence()
                .takeWhile(::isSpaceCharacter)
                .count()

        /**
         * @return this very integer if non-negative, 0 otherwise.
         */
        private fun Int.zeroIfNegative(): Int =
            when {
                this > 0 -> this
                else -> 0
            }
    }
}
