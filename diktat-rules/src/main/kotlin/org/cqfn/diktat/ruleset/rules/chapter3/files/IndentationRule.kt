/**
 * Main logic of indentation including Rule and utility classes and methods.
 */

package org.cqfn.diktat.ruleset.rules.chapter3.files

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.*
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
import com.pinterest.ktlint.core.ast.ElementType.OPEN_QUOTE
import com.pinterest.ktlint.core.ast.ElementType.RBRACE
import com.pinterest.ktlint.core.ast.ElementType.RBRACKET
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

import java.lang.StringBuilder

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
            .filter { it.textContains('\t') }
            .apply {
                if (isEmpty()) {
                    return true
                }
            }
            .forEach {
                WRONG_INDENTATION.warnAndFix(configRules, emitWarn, isFixMode, "tabs are not allowed for indentation", it.startOffset + it.text.indexOf('\t'), it) {
                    (it as LeafPsiElement).rawReplaceWithText(it.text.replace("\t", " ".repeat(configuration.indentationSize)))
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
            val numBlankLinesAfter = lastChild.text.count { it == '\n' }
            if (lastChild.elementType != WHITE_SPACE || numBlankLinesAfter != 1) {
                val warnText = if (lastChild.elementType != WHITE_SPACE || numBlankLinesAfter == 0) "no newline" else "too many blank lines"
                val fileName = filePath.substringAfterLast(File.separator)
                // In case, when last child is newline, visually user will see blank line at the end of file,
                // however, the text length does not consider it, since it's blank and line appeared only because of `\n`
                // But ktlint synthetically increase length in aim to have ability to point to this line, so in this case
                // offset will be `node.textLength`, otherwise we will point to the last symbol, i.e `node.textLength - 1`
                val offset = if (lastChild.elementType == WHITE_SPACE && lastChild.textContains('\n')) node.textLength else node.textLength - 1
                WRONG_INDENTATION.warnAndFix(configRules, emitWarn, isFixMode, "$warnText at the end of file $fileName", offset, node) {
                    if (lastChild.elementType != WHITE_SPACE) {
                        node.addChild(PsiWhiteSpaceImpl("\n"), null)
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
            } else if (astNode.elementType in decreasingTokens && !astNode.treePrev.let { it.elementType == WHITE_SPACE && it.textContains('\n') }) {
                // if decreasing token is after WHITE_SPACE with \n, indents are corrected in visitWhiteSpace method
                context.dec(astNode.elementType)
            } else if (astNode.elementType == WHITE_SPACE && astNode.textContains('\n') && astNode.treeNext != null) {
                // we check only WHITE_SPACE nodes with newlines, other than the last line in file; correctness of newlines should be checked elsewhere
                visitWhiteSpace(astNode, context)
            }
        }
    }

    private fun isCloseAndOpenQuoterOffset(nodeWhiteSpace: ASTNode, expectedIndent: Int): Boolean {
        val nextNode = nodeWhiteSpace.treeNext
        if (nextNode.elementType == VALUE_ARGUMENT) {
            val nextNodeDot = if (nextNode.elementType == DOT_QUALIFIED_EXPRESSION) {
                nextNode
            } else {
                nextNode.getFirstChildWithType(DOT_QUALIFIED_EXPRESSION)
            }
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

        val difOffsetCloseAndOpenQuote = isCloseAndOpenQuoterOffset(astNode, expectedIndent)

        if (checkResult?.isCorrect != true && expectedIndent != indentError.actual) {
            WRONG_INDENTATION.warnAndFix(configRules, emitWarn, isFixMode, "expected $expectedIndent but was ${indentError.actual}",
                whiteSpace.startOffset + whiteSpace.text.lastIndexOf('\n') + 1, whiteSpace.node) {
                checkStringLiteral(whiteSpace, expectedIndent, indentError.actual)
                whiteSpace.node.indentBy(expectedIndent)
            }
        } else if (!difOffsetCloseAndOpenQuote){
            WRONG_INDENTATION.warnAndFix(configRules, emitWarn, isFixMode, "the same number of indents to the opening and closing quotes was expected",
                whiteSpace.startOffset + whiteSpace.text.lastIndexOf('\n') + 1, whiteSpace.node) {
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
        val nextNode = whiteSpace.node.treeNext
        val nextNodeDot = if (nextNode.elementType == DOT_QUALIFIED_EXPRESSION) {
            nextNode
        } else {
            nextNode.getFirstChildWithType(DOT_QUALIFIED_EXPRESSION)
        }
        if (nextNodeDot != null &&
            nextNodeDot.elementType == DOT_QUALIFIED_EXPRESSION &&
            nextNodeDot.firstChildNode.elementType == STRING_TEMPLATE &&
            nextNodeDot.firstChildNode.text.startsWith("\"\"\"") &&
            nextNodeDot.findChildByType(CALL_EXPRESSION)?.text?.let {
                it == "trimIndent()" ||
                    it == "trimMargin()"
            } == true) {
            fixStringLiteral(whiteSpace, expectedIndent, actualIndent)
        }
    }

    /**
     * If it is triple-quoted string template we need to indent all its parts
     */
    private fun fixStringLiteral(
        whiteSpace: PsiWhiteSpace,
        expectedIndent: Int,
        actualIndent: Int
    ) {
        val nextNode = whiteSpace.node.treeNext
        val nextNodeDot = if (nextNode.elementType == DOT_QUALIFIED_EXPRESSION) {
            nextNode
        } else {
            nextNode.getFirstChildWithType(DOT_QUALIFIED_EXPRESSION)
        }
        val textIndent = " ".repeat(expectedIndent + INDENT_SIZE)
        val templateEntries = nextNodeDot?.getFirstChildWithType(STRING_TEMPLATE)?.getAllChildrenWithType(LITERAL_STRING_TEMPLATE_ENTRY)
        templateEntries?.forEach { node ->
            if (!node.text.contains("\n")) {
                fixFirstTemplateEntries(node, textIndent, actualIndent)
            }
        }
        (templateEntries?.last()?.firstChildNode as LeafPsiElement)
            .rawReplaceWithText(" ".repeat(expectedIndent) + templateEntries
                .last()
                .firstChildNode
                .text
                .trim())
    }

    /**
     * This method fixes all lines of string template except the last one
     * Also it considers $foo insertions in string
     */
    private fun fixFirstTemplateEntries(
        node: ASTNode,
        textIndent: String,
        actualIndent: Int
    ) {
        val correctedText = StringBuilder()
        // shift of the node depending on its initial string template indent
        val nodeStartIndent = if (node.firstChildNode.text.takeWhile { it == ' ' }.count() - actualIndent - INDENT_SIZE > 0) {
            node.firstChildNode.text.takeWhile { it == ' ' }.count() - actualIndent - INDENT_SIZE
        } else {
            0
        }
        val isPrevStringTemplate = node.treePrev.elementType in stringLiteralTokens
        val isNextStringTemplate = node.treeNext.elementType in stringLiteralTokens
        when {
            // if string template is before literal_string
            isPrevStringTemplate && !isNextStringTemplate -> correctedText.append(node.firstChildNode.text.trimEnd())
            // if string template is after literal_string
            !isPrevStringTemplate && isNextStringTemplate -> correctedText.append(textIndent + " ".repeat(nodeStartIndent) + node.firstChildNode.text.trimStart())
            // if there is no string template in literal_string
            !isPrevStringTemplate && !isNextStringTemplate -> correctedText.append(textIndent + " ".repeat(nodeStartIndent) + node.firstChildNode.text.trim())
            isPrevStringTemplate && isNextStringTemplate -> correctedText.append(node.firstChildNode.text)
            node.text.isBlank() -> correctedText.append(textIndent)
            else -> {}
        }
        (node.firstChildNode as LeafPsiElement).rawReplaceWithText(correctedText.toString())
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
         * @param astNode the node which is used to determine whether exceptinoal indents are still active
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
        const val INDENT_SIZE = 4
        const val NAME_ID = "zct-indentation"
        private val increasingTokens = listOf(LPAR, LBRACE, LBRACKET, LONG_TEMPLATE_ENTRY_START)
        private val decreasingTokens = listOf(RPAR, RBRACE, RBRACKET, LONG_TEMPLATE_ENTRY_END)
        private val matchingTokens = increasingTokens.zip(decreasingTokens)
        private val stringLiteralTokens = listOf(SHORT_STRING_TEMPLATE_ENTRY, LONG_STRING_TEMPLATE_ENTRY)
    }
}

/**
 * @property expected expected indentation as a number of spaces
 * @property actual actual indentation as a number of spaces
 */
internal data class IndentationError(val expected: Int, val actual: Int)

/**
 * @return indentation of the last line of this string
 */
internal fun String.lastIndent() = substringAfterLast('\n').count { it == ' ' }
