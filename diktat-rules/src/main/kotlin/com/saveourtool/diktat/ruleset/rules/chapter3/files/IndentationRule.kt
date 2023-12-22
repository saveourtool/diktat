/**
 * Main logic of indentation including Rule and utility classes and methods.
 */

@file:Suppress("FILE_UNORDERED_IMPORTS")// False positives, see #1494.

package com.saveourtool.diktat.ruleset.rules.chapter3.files

import com.saveourtool.diktat.api.DiktatErrorEmitter
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getRuleConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.rules.chapter3.files.IndentationAmount.NONE
import com.saveourtool.diktat.ruleset.rules.chapter3.files.IndentationAmount.SINGLE
import com.saveourtool.diktat.ruleset.rules.chapter3.files.IndentationConfigAware.Factory.withIndentationConfig
import com.saveourtool.diktat.ruleset.utils.NEWLINE
import com.saveourtool.diktat.ruleset.utils.SPACE
import com.saveourtool.diktat.ruleset.utils.TAB
import com.saveourtool.diktat.ruleset.utils.getAllChildrenWithType
import com.saveourtool.diktat.ruleset.utils.getAllLeafsWithSpecificType
import com.saveourtool.diktat.ruleset.utils.getFilePath
import com.saveourtool.diktat.ruleset.utils.getFirstChildWithType
import com.saveourtool.diktat.ruleset.utils.indentBy
import com.saveourtool.diktat.ruleset.utils.indentation.ArrowInWhenChecker
import com.saveourtool.diktat.ruleset.utils.indentation.AssignmentOperatorChecker
import com.saveourtool.diktat.ruleset.utils.indentation.ConditionalsAndLoopsWithoutBracesChecker
import com.saveourtool.diktat.ruleset.utils.indentation.CustomGettersAndSettersChecker
import com.saveourtool.diktat.ruleset.utils.indentation.CustomIndentationChecker
import com.saveourtool.diktat.ruleset.utils.indentation.DotCallChecker
import com.saveourtool.diktat.ruleset.utils.indentation.ExpressionIndentationChecker
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig
import com.saveourtool.diktat.ruleset.utils.indentation.KdocIndentationChecker
import com.saveourtool.diktat.ruleset.utils.indentation.SuperTypeListChecker
import com.saveourtool.diktat.ruleset.utils.indentation.ValueParameterListChecker
import com.saveourtool.diktat.ruleset.utils.lastIndent
import com.saveourtool.diktat.ruleset.utils.leadingSpaceCount
import com.saveourtool.diktat.ruleset.utils.leaveOnlyOneNewLine
import com.saveourtool.diktat.ruleset.utils.visit

import org.jetbrains.kotlin.KtNodeTypes.BINARY_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.CALL_EXPRESSION
import org.jetbrains.kotlin.lexer.KtTokens.CLOSING_QUOTE
import org.jetbrains.kotlin.KtNodeTypes.DOT_QUALIFIED_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.ELSE
import org.jetbrains.kotlin.lexer.KtTokens.IDENTIFIER
import org.jetbrains.kotlin.lexer.KtTokens.LBRACE
import org.jetbrains.kotlin.lexer.KtTokens.LBRACKET
import org.jetbrains.kotlin.KtNodeTypes.LITERAL_STRING_TEMPLATE_ENTRY
import org.jetbrains.kotlin.KtNodeTypes.LONG_STRING_TEMPLATE_ENTRY
import org.jetbrains.kotlin.lexer.KtTokens.LONG_TEMPLATE_ENTRY_END
import org.jetbrains.kotlin.lexer.KtTokens.LONG_TEMPLATE_ENTRY_START
import org.jetbrains.kotlin.lexer.KtTokens.LPAR
import org.jetbrains.kotlin.KtNodeTypes.PARENTHESIZED
import org.jetbrains.kotlin.lexer.KtTokens.RBRACE
import org.jetbrains.kotlin.lexer.KtTokens.RBRACKET
import org.jetbrains.kotlin.KtNodeTypes.REFERENCE_EXPRESSION
import org.jetbrains.kotlin.lexer.KtTokens.REGULAR_STRING_PART
import org.jetbrains.kotlin.lexer.KtTokens.RPAR
import org.jetbrains.kotlin.KtNodeTypes.SAFE_ACCESS_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.SHORT_STRING_TEMPLATE_ENTRY
import org.jetbrains.kotlin.KtNodeTypes.STRING_TEMPLATE
import org.jetbrains.kotlin.KtNodeTypes.THEN
import org.jetbrains.kotlin.KtNodeTypes.VALUE_ARGUMENT
import org.jetbrains.kotlin.KtNodeTypes.VALUE_ARGUMENT_LIST
import org.jetbrains.kotlin.KtNodeTypes.VALUE_PARAMETER_LIST
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import com.saveourtool.diktat.ruleset.utils.isWhiteSpaceWithNewline
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtLoopExpression
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KCallable

import java.util.ArrayDeque as Stack

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
    private lateinit var overriddenEmitWarn: DiktatErrorEmitter

    override fun logic(node: ASTNode) {
        overriddenEmitWarn = configuration.overrideIfRequiredWarnMessage(emitWarn)
        if (node.elementType == KtFileElementType.INSTANCE) {
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
            ).map { it(configuration) }

            if (checkIsIndentedWithSpaces(node)) {
                checkIndentation(node)
            } else {
                log.warn { "Not going to check indentation because there are tabs" }
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
            .forEach { whiteSpaceNode ->
                WRONG_INDENTATION.warnAndFix(configRules, overriddenEmitWarn, isFixMode, "tabs are not allowed for indentation",
                    whiteSpaceNode.startOffset + whiteSpaceNode.text.indexOf(TAB), whiteSpaceNode) {
                    (whiteSpaceNode as LeafPsiElement).rawReplaceWithText(whiteSpaceNode.text.replace(TAB.toString(), configuration.indentationSize.spaces))
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
                val offset = if (lastChild.isMultilineWhitespace()) node.textLength else node.textLength - 1
                WRONG_INDENTATION.warnAndFix(configRules, overriddenEmitWarn, isFixMode, "$warnText at the end of file $fileName", offset, node) {
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
    private fun checkIndentation(node: ASTNode) =
        with(IndentContext(configuration)) {
            node.visit { astNode ->
                checkAndReset(astNode)
                val indentationIncrement = astNode.getIndentationIncrement()
                if (indentationIncrement.isNonZero()) {
                    storeIncrementingToken(astNode.elementType, indentationIncrement)
                } else if (astNode.getIndentationDecrement().isNonZero() && !astNode.treePrev.isMultilineWhitespace()) {
                    // if decreasing token is after WHITE_SPACE with \n, indents are corrected in visitWhiteSpace method
                    this -= astNode.elementType
                } else if (astNode.isMultilineWhitespace() && astNode.treeNext != null) {
                    // we check only WHITE_SPACE nodes with newlines, other than the last line in file; correctness of newlines should be checked elsewhere
                    visitWhiteSpace(astNode)
                }
            }
        }

    @Suppress("ForbiddenComment")
    private fun IndentContext.visitWhiteSpace(astNode: ASTNode) {
        require(astNode.isMultilineWhitespace()) {
            "The node is $astNode while a multi-line $WHITE_SPACE expected"
        }

        maybeIncrement()
        val whiteSpace = astNode.psi as PsiWhiteSpace
        if (astNode.treeNext.getIndentationDecrement().isNonZero()) {
            // if newline is followed by closing token, it should already be indented less
            this -= astNode.treeNext.elementType
        }

        val indentError = IndentationError(indentation, astNode.text.lastIndent())

        val checkResult = customIndentationCheckers.firstNotNullOfOrNull {
            it.checkNode(whiteSpace, indentError)
        }

        val expectedIndent = checkResult?.expectedIndent ?: indentError.expected
        if (checkResult?.adjustNext == true && astNode.parents().none { it.elementType == LONG_STRING_TEMPLATE_ENTRY }) {
            val exceptionInitiatorNode = astNode.getExceptionalIndentInitiator()
            addException(exceptionInitiatorNode, expectedIndent - indentError.expected, checkResult.includeLastChild)
        }

        if (astNode.treeParent.elementType == LONG_STRING_TEMPLATE_ENTRY && astNode.treeNext.elementType != LONG_TEMPLATE_ENTRY_END) {
            addException(astNode.treeParent, SINGLE.level() * configuration.indentationSize, false)
        }

        val alignedOpeningAndClosingQuotes = hasAlignedOpeningAndClosingQuotes(astNode, indentError.actual)

        if ((checkResult?.isCorrect != true && expectedIndent != indentError.actual) || !alignedOpeningAndClosingQuotes) {
            val warnText = if (!alignedOpeningAndClosingQuotes) {
                "the same number of indents to the opening and closing quotes was expected"
            } else {
                "expected $expectedIndent but was ${indentError.actual}"
            }
            WRONG_INDENTATION.warnAndFix(configRules, overriddenEmitWarn, isFixMode, warnText,
                whiteSpace.startOffset + whiteSpace.text.lastIndexOf(NEWLINE) + 1, whiteSpace.node) {
                checkStringLiteral(whiteSpace, expectedIndent, indentError.actual)
                whiteSpace.node.indentBy(expectedIndent)
            }
        }
    }

    /**
     * Checks if it is a triple-quoted string template with
     * [trimIndent()][String.trimIndent] or [trimMargin(...)][String.trimMargin]
     * function.
     */
    private fun checkStringLiteral(
        whiteSpace: PsiWhiteSpace,
        expectedIndent: Int,
        actualIndent: Int
    ) {
        val nextNodeDot = whiteSpace.node.treeNext.getNextDotExpression()
        if (nextNodeDot != null &&
                nextNodeDot.elementType == DOT_QUALIFIED_EXPRESSION &&
                nextNodeDot.firstChildNode.elementType == STRING_TEMPLATE &&
                nextNodeDot.firstChildNode.text.startsWith("\"\"\"") &&
                nextNodeDot.findChildByType(CALL_EXPRESSION).isTrimIndentOrMarginCall()) {
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
        val templateEntriesLastIndex = templateEntries.size - 1
        var templateEntryFollowingNewline = false

        templateEntries.forEachIndexed { index, templateEntry ->
            val text = templateEntry.text

            when {
                text.contains(NEWLINE) -> {
                    /*
                     * Set the flag.
                     */
                    templateEntryFollowingNewline = true

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

                /*
                 * This is the last string template fragment which is usually followed
                 * with the closing `"""` and the `.trimIndent()` or `.trimMargin(...)` call.
                 */
                index == templateEntriesLastIndex -> {
                    val lastRegularStringPart = templateEntries.last().firstChildNode as LeafPsiElement
                    lastRegularStringPart.checkRegularStringPart().apply {
                        val textWithoutIndent = text.trimStart()
                        rawReplaceWithText(expectedIndent.spaces + textWithoutIndent)
                    }
                }

                /*
                 * Either this is the very first string template entry, or an
                 * entry which immediately follows the newline.
                 */
                index == 0 || templateEntryFollowingNewline -> {
                    fixFirstTemplateEntries(
                        templateEntry,
                        expectedIndentation = expectedIndent,
                        actualIndentation = actualIndent)

                    /*
                     * Re-set the flag.
                     */
                    templateEntryFollowingNewline = false
                }
            }
        }
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
     * @param expectedIndentation the expected indentation level, as returned by
     *   [IndentationError.expected].
     * @param actualIndentation the actual indentation level, as returned by
     *   [IndentationError.actual].
     */
    private fun fixFirstTemplateEntries(
        templateEntry: ASTNode,
        expectedIndentation: Int,
        actualIndentation: Int
    ) {
        require(templateEntry.elementType == LITERAL_STRING_TEMPLATE_ENTRY) {
            "The elementType of this node is ${templateEntry.elementType} while $LITERAL_STRING_TEMPLATE_ENTRY expected"
        }

        /*
         * Quite possible, do nothing in this case.
         */
        if (expectedIndentation == actualIndentation) {
            return
        }

        withIndentationConfig(configuration) {
            /*
             * A `REGULAR_STRING_PART`.
             */
            val regularStringPart = templateEntry.firstChildNode as LeafPsiElement
            val regularStringPartText = regularStringPart.checkRegularStringPart().text
            // shift of the node depending on its initial string template indentation
            val nodeStartIndent = (regularStringPartText.leadingSpaceCount() - actualIndentation - SINGLE).zeroIfNegative()

            val isPrevStringTemplate = templateEntry.treePrev.elementType in stringLiteralTokens
            val isNextStringTemplate = templateEntry.treeNext.elementType in stringLiteralTokens

            val correctedText = when {
                isPrevStringTemplate -> when {
                    isNextStringTemplate -> regularStringPartText

                    // if string template is before literal_string
                    else -> regularStringPartText.trimEnd()
                }

                // if string template is after literal_string
                // or if there is no string template in literal_string
                else -> (expectedIndentation + SINGLE + nodeStartIndent).spaces + regularStringPartText.trimStart()
            }

            regularStringPart.rawReplaceWithText(correctedText)
        }
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
     * @return the amount by which the indentation should be incremented
     *   once this node is encountered (may be [none][NONE]).
     * @see ASTNode.getIndentationDecrement
     */
    private fun ASTNode.getIndentationIncrement(): IndentationAmount =
        when (elementType) {
            /*
             * A special case of an opening parenthesis which *may* or *may not*
             * increment the indentation.
             */
            LPAR -> getParenthesisIndentationChange()

            in increasingTokens -> SINGLE

            else -> NONE
        }

    /**
     * @return the amount by which the indentation should be decremented
     *   once this node is encountered (may be [none][NONE]).
     * @see ASTNode.getIndentationIncrement
     */
    private fun ASTNode.getIndentationDecrement(): IndentationAmount =
        when (elementType) {
            /*
             * A special case of a closing parenthesis which *may* or *may not*
             * increment the indentation.
             */
            RPAR -> getParenthesisIndentationChange()

            in decreasingTokens -> SINGLE

            else -> NONE
        }

    /**
     * Parentheses always affect indentation when they're a part of a
     * [VALUE_PARAMETER_LIST] (formal arguments) or a [VALUE_ARGUMENT_LIST]
     * (effective function call arguments).
     *
     * When they're children of a [PARENTHESIZED] (often inside a
     * [BINARY_EXPRESSION]), contribute to the indentation depending on
     * whether there's a newline after the opening parenthesis.
     *
     * @receiver an opening or a closing parenthesis.
     * @return the amount by which the indentation should be incremented
     *   (after [LPAR]) or decremented (after [RPAR]). The returned value
     *   may well be [NONE], meaning the indentation level should be
     *   preserved.
     * @see BINARY_EXPRESSION
     * @see PARENTHESIZED
     * @see VALUE_ARGUMENT_LIST
     * @see VALUE_PARAMETER_LIST
     */
    private fun ASTNode.getParenthesisIndentationChange(): IndentationAmount {
        require(elementType in arrayOf(LPAR, RPAR)) {
            elementType.toString()
        }

        return when (treeParent.elementType) {
            PARENTHESIZED -> when (elementType) {
                /*
                 * `LPAR` inside a binary expression only contributes to the
                 * indentation if it's immediately followed by a newline.
                 */
                LPAR -> when {
                    treeNext.isWhiteSpaceWithNewline() -> IndentationAmount.valueOf(configuration.extendedIndentAfterOperators)
                    else -> NONE
                }

                /*
                 * `RPAR` inside a binary expression affects the indentation
                 * only if its matching `LPAR` node does so.
                 */
                else -> {
                    val openingParenthesis = elementType.braceMatchOrNull()?.let { braceMatch ->
                        treeParent.findChildByType(braceMatch)
                    }
                    openingParenthesis?.getParenthesisIndentationChange() ?: NONE
                }
            }

            /*
             * Either a control-flow statement (one of IF, WHEN, FOR or
             * DO_WHILE), a function declaration (VALUE_PARAMETER_LIST or
             * PROPERTY_ACCESSOR), or a function call (VALUE_ARGUMENT_LIST).
             */
            else -> SINGLE
        }
    }

    /**
     * Holds a mutable state needed to calculate the indentation and keep track
     * of exceptions.
     *
     * Tokens from [increasingTokens] are stored in stack [activeTokens]. When [WHITE_SPACE] with line break is encountered,
     * if stack is not empty, indentation is increased. When token from [decreasingTokens] is encountered, it's counterpart is removed
     * from stack. If there has been a [WHITE_SPACE] with line break between them, indentation is decreased.
     *
     * @see IndentationConfigAware
     */
    private class IndentContext(config: IndentationConfig) : IndentationAware, IndentationConfigAware by IndentationConfigAware(config) {
        private var regularIndent = 0
        private val exceptionalIndents: MutableList<ExceptionalIndent> = mutableListOf()

        /**
         * The stack of element types (either [WHITE_SPACE] or any of
         * [increasingTokens]) along with the indentation changes the
         * corresponding elements induce.
         *
         * [WHITE_SPACE] is always accompanied by [no indentation change][NONE].
         */
        private val activeTokens: Stack<IndentedElementType> = Stack()

        /**
         * @return full current indentation.
         */
        @Suppress(
            "CUSTOM_GETTERS_SETTERS",
            "WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR",  // #1464
        )
        override val indentation: Int
            get() =
                regularIndent + exceptionalIndents.sumOf(ExceptionalIndent::indentation)

        /**
         * Pushes [token] onto the [stack][activeTokens], but doesn't increment
         * the indentation. The indentation is incremented separately, see
         * [maybeIncrement].
         *
         * A call to this method **may or may not** be followed by a single call
         * to [maybeIncrement].
         *
         * @param token a token that caused indentation increment, any of
         *   [increasingTokens] (e.g.: an [opening brace][LPAR]).
         * @param increment the indentation increment (must be non-zero).
         * @see maybeIncrement
         */
        fun storeIncrementingToken(token: IElementType, increment: IndentationAmount) {
            require(token in increasingTokens) {
                "The token is $token while any of $increasingTokens expected"
            }
            require(increment.isNonZero()) {
                "The indentation increment is zero"
            }

            activeTokens.push(token to increment)
        }

        /**
         * Increments the indentation if a multi-line [WHITE_SPACE] is
         * encountered after an opening brace.
         *
         * A call to this method **always** has a preceding call to
         * [storeIncrementingToken].
         *
         * @see minusAssign
         */
        fun maybeIncrement() {
            val headOrNull: IndentedElementType? = activeTokens.peek()
            check(headOrNull == null ||
                    headOrNull.type == WHITE_SPACE ||
                    headOrNull.type in increasingTokens) {
                "The head of the stack is $headOrNull while only $WHITE_SPACE or any of $increasingTokens expected"
            }

            if (headOrNull != null && headOrNull.type != WHITE_SPACE) {
                regularIndent += headOrNull.indentationChange
                activeTokens.push(WHITE_SPACE to NONE)
            }
        }

        /**
         * Pops tokens from the [stack][activeTokens] and decrements the
         * indentation accordingly.
         *
         * @param token a token that caused indentation decrement, any of
         *   [decreasingTokens] (e.g.: a [closing brace][RPAR]).
         * @see maybeIncrement
         */
        operator fun minusAssign(token: IElementType) {
            require(token in decreasingTokens) {
                "The token is $token while any of $decreasingTokens expected"
            }

            if (activeTokens.peek()?.type == WHITE_SPACE) {
                /*-
                 * In practice, there's always only a single `WHITE_SPACE`
                 * element type (representing the newline) pushed onto the stack
                 * after an opening brace (`LPAR` & friends), so it needs to be
                 * popped only once.
                 *
                 * Still, preserving the logic for compatibility.
                 */
                while (activeTokens.peek()?.type == WHITE_SPACE) {
                    activeTokens.pop()
                }

                /*-
                 * If an opening brace (`LPAR` etc.) was followed by a newline,
                 * this has led to the indentation being increased.
                 *
                 * Now, let's decrease it back to the original value.
                 */
                val headOrNull: IndentedElementType? = activeTokens.peek()
                if (headOrNull != null && headOrNull.type == token.braceMatchOrNull()) {
                    regularIndent -= headOrNull.indentationChange
                }
            }

            /*
             * In practice, the predicate is always `true` (provided braces are
             * balanced) and can be replaced with a `check()` call.
             */
            val headOrNull: IndentedElementType? = activeTokens.peek()
            if (headOrNull != null && headOrNull.type == token.braceMatchOrNull()) {
                /*
                 * Pop the matching opening brace.
                 */
                activeTokens.pop()
            }
        }

        /**
         * @param initiator a node that caused exceptional indentation
         * @param indentation an additional indentation.
         * @param includeLastChild whether the last child node should be included in the range affected by exceptional indentation
         * @return true if add exception in exceptionalIndents
         */
        fun addException(
            initiator: ASTNode,
            indentation: Int,
            includeLastChild: Boolean
        ) = exceptionalIndents.add(ExceptionalIndent(initiator, indentation, includeLastChild))

        /**
         * @param astNode the node which is used to determine whether exceptional indents are still active
         * @return boolean result
         */
        fun checkAndReset(astNode: ASTNode) = exceptionalIndents.retainAll { it.isActive(astNode) }

        /**
         * @property initiator a node that caused exceptional indentation
         * @property indentation an additional indentation.
         * @property includeLastChild whether the last child node should be included in the range affected by exceptional indentation
         */
        private data class ExceptionalIndent(
            val initiator: ASTNode,
            override val indentation: Int,
            val includeLastChild: Boolean = true
        ) : IndentationAware {
            /**
             * Checks whether this exceptional indentation is still active. This is a hypotheses that exceptional indentation will end
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
        private val log = KotlinLogging.logger {}
        const val NAME_ID = "indentation"
        private val increasingTokens: Set<IElementType> = linkedSetOf(LPAR, LBRACE, LBRACKET, LONG_TEMPLATE_ENTRY_START)
        private val decreasingTokens: Set<IElementType> = linkedSetOf(RPAR, RBRACE, RBRACKET, LONG_TEMPLATE_ENTRY_END)

        /**
         * This is essentially a bi-map, which allows to look up a closing brace
         * type by an opening brace type, or vice versa.
         */
        private val matchingTokens = (increasingTokens.asSequence() zip decreasingTokens.asSequence()).flatMap { (opening, closing) ->
            sequenceOf(opening to closing, closing to opening)
        }.toMap()
        private val stringLiteralTokens = listOf(SHORT_STRING_TEMPLATE_ENTRY, LONG_STRING_TEMPLATE_ENTRY)
        private val knownTrimFunctionPatterns = sequenceOf(String::trimIndent, String::trimMargin)
            .map(KCallable<String>::name)
            .toSet()

        /**
         * @return a string which consists of `N` [space][SPACE] characters.
         */
        @Suppress("CUSTOM_GETTERS_SETTERS")
        private val Int.spaces: String
            get() =
                SPACE.toString().repeat(n = this)

        /**
         * @return `true` if this is a [whitespace][WHITE_SPACE] node containing
         *   a [newline][NEWLINE], `false` otherwise.
         */
        private fun ASTNode.isMultilineWhitespace(): Boolean =
            elementType == WHITE_SPACE && textContains(NEWLINE)

        @OptIn(ExperimentalContracts::class)
        private fun ASTNode?.isMultilineStringTemplate(): Boolean {
            contract {
                returns(true) implies (this@isMultilineStringTemplate != null)
            }

            this ?: return false

            return elementType == STRING_TEMPLATE &&
                    getAllChildrenWithType(LITERAL_STRING_TEMPLATE_ENTRY).any { entry ->
                        entry.textContains(NEWLINE)
                    }
        }

        /**
         * @return `true` if this is a [String.trimIndent] or [String.trimMargin]
         * call, `false` otherwise.
         */
        @OptIn(ExperimentalContracts::class)
        private fun ASTNode?.isTrimIndentOrMarginCall(): Boolean {
            contract {
                returns(true) implies (this@isTrimIndentOrMarginCall != null)
            }

            this ?: return false

            require(elementType == CALL_EXPRESSION) {
                "The elementType of this node is $elementType while $CALL_EXPRESSION expected"
            }

            val referenceExpression = firstChildNode ?: return false
            if (referenceExpression.elementType != REFERENCE_EXPRESSION) {
                return false
            }

            val identifier = referenceExpression.firstChildNode ?: return false
            if (identifier.elementType != IDENTIFIER) {
                return false
            }

            return identifier.text in knownTrimFunctionPatterns
        }

        private fun ASTNode.getNextDotExpression(): ASTNode? =
            when (elementType) {
                DOT_QUALIFIED_EXPRESSION -> this
                else -> getFirstChildWithType(DOT_QUALIFIED_EXPRESSION)
            }

        /**
         * @return the matching closing brace type for this opening brace type,
         *   or vice versa.
         */
        private fun IElementType.braceMatchOrNull(): IElementType? =
            matchingTokens[this]

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
         * @return this very integer if non-negative, 0 otherwise.
         */
        private fun Int.zeroIfNegative(): Int =
            when {
                this > 0 -> this
                else -> 0
            }

        /**
         * Processes fragments like:
         *
         * ```kotlin
         * f(
         *     """
         *     |foobar
         *     """.trimMargin()
         * )
         * ```
         *
         * @param whitespace the whitespace node between an [LPAR] and the
         *   `trimIndent()`- or `trimMargin()`- terminated string template, which is
         *   an effective argument of a function call. The string template is
         *   expected to begin on a separate line (otherwise, there'll be no
         *   whitespace in-between).
         * @return `true` if the opening and the closing quotes of the string
         *   template are aligned, `false` otherwise.
         */
        private fun hasAlignedOpeningAndClosingQuotes(whitespace: ASTNode, expectedIndent: Int): Boolean {
            require(whitespace.isMultilineWhitespace()) {
                "The node is $whitespace while a multi-line $WHITE_SPACE expected"
            }

            /*
             * Here, we expect that `nextNode` is a VALUE_ARGUMENT which contains
             * the dot-qualified expression (`STRING_TEMPLATE.trimIndent()` or
             * `STRING_TEMPLATE.trimMargin()`).
             */
            val nextFunctionArgument = whitespace.treeNext
            if (nextFunctionArgument.elementType == VALUE_ARGUMENT) {
                val memberOrExtensionCall = nextFunctionArgument.getNextDotExpression()

                /*
                 * Limit allowed member or extension calls to `trimIndent()` and
                 * `trimMargin()`.
                 */
                if (memberOrExtensionCall != null &&
                        memberOrExtensionCall.getFirstChildWithType(CALL_EXPRESSION).isTrimIndentOrMarginCall()) {
                    val stringTemplate = memberOrExtensionCall.getFirstChildWithType(STRING_TEMPLATE)

                    /*
                     * Limit the logic to multi-line string templates only (the
                     * opening and closing quotes of a single-line template are,
                     * obviously, always mis-aligned).
                     */
                    if (stringTemplate != null && stringTemplate.isMultilineStringTemplate()) {
                        val closingQuoteIndent = stringTemplate.getFirstChildWithType(CLOSING_QUOTE)
                            ?.treePrev
                            ?.text
                            ?.length ?: -1
                        return expectedIndent == closingQuoteIndent
                    }
                }
            }

            return true
        }
    }
}
