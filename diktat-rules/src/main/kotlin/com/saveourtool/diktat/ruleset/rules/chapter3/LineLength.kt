package com.saveourtool.diktat.ruleset.rules.chapter3

import com.saveourtool.diktat.common.config.rules.RuleConfiguration
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getRuleConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.LONG_LINE
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.KotlinParser
import com.saveourtool.diktat.ruleset.utils.appendNewlineMergingWhiteSpace
import com.saveourtool.diktat.ruleset.utils.calculateLineColByOffset
import com.saveourtool.diktat.ruleset.utils.countCodeLines
import com.saveourtool.diktat.ruleset.utils.findAllNodesWithConditionOnLine
import com.saveourtool.diktat.ruleset.utils.findChildAfter
import com.saveourtool.diktat.ruleset.utils.findChildBefore
import com.saveourtool.diktat.ruleset.utils.findChildrenMatching
import com.saveourtool.diktat.ruleset.utils.findParentNodeWithSpecificType
import com.saveourtool.diktat.ruleset.utils.getAllChildrenWithType
import com.saveourtool.diktat.ruleset.utils.getFirstChildWithType
import com.saveourtool.diktat.ruleset.utils.getLineNumber
import com.saveourtool.diktat.ruleset.utils.hasChildOfType
import com.saveourtool.diktat.ruleset.utils.isChildAfterAnother
import com.saveourtool.diktat.ruleset.utils.isWhiteSpace
import com.saveourtool.diktat.ruleset.utils.isWhiteSpaceWithNewline
import com.saveourtool.diktat.ruleset.utils.nextSibling
import com.saveourtool.diktat.ruleset.utils.prevSibling

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlin.KtNodeTypes.BINARY_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.BLOCK
import org.jetbrains.kotlin.KtNodeTypes.DOT_QUALIFIED_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.FUNCTION_LITERAL
import org.jetbrains.kotlin.KtNodeTypes.IMPORT_LIST
import org.jetbrains.kotlin.KtNodeTypes.OPERATION_REFERENCE
import org.jetbrains.kotlin.KtNodeTypes.PACKAGE_DIRECTIVE
import org.jetbrains.kotlin.KtNodeTypes.PARENTHESIZED
import org.jetbrains.kotlin.KtNodeTypes.POSTFIX_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.PREFIX_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.PROPERTY
import org.jetbrains.kotlin.KtNodeTypes.SAFE_ACCESS_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.STRING_TEMPLATE
import org.jetbrains.kotlin.KtNodeTypes.VALUE_ARGUMENT_LIST
import org.jetbrains.kotlin.KtNodeTypes.WHEN_CONDITION_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.WHEN_ENTRY
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens.MARKDOWN_INLINE_LINK
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens.TEXT
import org.jetbrains.kotlin.lexer.KtTokens.ANDAND
import org.jetbrains.kotlin.lexer.KtTokens.ARROW
import org.jetbrains.kotlin.lexer.KtTokens.COMMA
import org.jetbrains.kotlin.lexer.KtTokens.DOT
import org.jetbrains.kotlin.lexer.KtTokens.ELVIS
import org.jetbrains.kotlin.lexer.KtTokens.EOL_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.EQ
import org.jetbrains.kotlin.lexer.KtTokens.EQEQ
import org.jetbrains.kotlin.lexer.KtTokens.EQEQEQ
import org.jetbrains.kotlin.lexer.KtTokens.EXCL
import org.jetbrains.kotlin.lexer.KtTokens.EXCLEQ
import org.jetbrains.kotlin.lexer.KtTokens.EXCLEQEQEQ
import org.jetbrains.kotlin.lexer.KtTokens.GT
import org.jetbrains.kotlin.lexer.KtTokens.GTEQ
import org.jetbrains.kotlin.lexer.KtTokens.LBRACE
import org.jetbrains.kotlin.lexer.KtTokens.LPAR
import org.jetbrains.kotlin.lexer.KtTokens.LT
import org.jetbrains.kotlin.lexer.KtTokens.LTEQ
import org.jetbrains.kotlin.lexer.KtTokens.OROR
import org.jetbrains.kotlin.lexer.KtTokens.RBRACE
import org.jetbrains.kotlin.lexer.KtTokens.RPAR
import org.jetbrains.kotlin.lexer.KtTokens.SAFE_ACCESS
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType

import java.net.MalformedURLException
import java.net.URL

/**
 * The rule checks for lines in the file that exceed the maximum length.
 * Rule ignores URL in KDoc. This rule can also fix some particular corner cases.
 * This inspection can fix long binary expressions in condition inside `if`,
 * in property declarations and in single line functions.
 */
@Suppress("ForbiddenComment")
class LineLength(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(LONG_LINE)
) {
    private val configuration by lazy {
        LineLengthConfiguration(
            configRules.getRuleConfig(LONG_LINE)?.configuration ?: emptyMap()
        )
    }
    private lateinit var positionByOffset: (Int) -> Pair<Int, Int>

    override fun logic(node: ASTNode) {
        var currentFixNumber = 0
        var isFixedSmthInPreviousStep: Boolean

        // loop that trying to fix LineLength rule warnings until warnings run out
        do {
            isFixedSmthInPreviousStep = false
            currentFixNumber++

            if (node.elementType == KtFileElementType.INSTANCE) {
                node.getChildren(null).forEach {
                    if (it.elementType != PACKAGE_DIRECTIVE && it.elementType != IMPORT_LIST) {
                        val isFixedSmthInChildNode = checkLength(it, configuration)

                        if (!isFixedSmthInPreviousStep && isFixedSmthInChildNode) {
                            isFixedSmthInPreviousStep = true
                        }
                    }
                }
            }
        } while (isFixedSmthInPreviousStep && currentFixNumber < MAX_FIX_NUMBER)

        if (currentFixNumber == MAX_FIX_NUMBER) {
            log.error {
                "The limit on the number of consecutive fixes has been reached. There may be a bug causing an endless loop of fixes."
            }
        }
    }

    @Suppress(
        "UnsafeCallOnNullableType",
        "TOO_LONG_FUNCTION",
        "FUNCTION_BOOLEAN_PREFIX"
    )
    private fun checkLength(node: ASTNode, configuration: LineLengthConfiguration): Boolean {
        var isFixedSmthInChildNode = false

        var offset = 0
        node.text.lines().forEach { line ->
            if (line.length > configuration.lineLength) {
                val newNode = node.psi.findElementAt(offset + configuration.lineLength.toInt() - 1)!!.node

                if ((newNode.elementType != TEXT && newNode.elementType != MARKDOWN_INLINE_LINK) || !isKdocValid(newNode)) {
                    positionByOffset = node.treeParent.calculateLineColByOffset()

                    val fixableType = isFixable(newNode, configuration)

                    LONG_LINE.warnOnlyOrWarnAndFix(
                        configRules, emitWarn,
                        "max line length ${configuration.lineLength}, but was ${line.length}",
                        offset + node.startOffset, node,
                        shouldBeAutoCorrected = fixableType !is None,
                        isFixMode,
                    ) {
                        val textBeforeFix = node.text
                        val textLenBeforeFix = node.textLength
                        val blankLinesBeforeFix = node.text.lines().size - countCodeLines(node)

                        fixableType.fix()

                        val textAfterFix = node.text
                        val textLenAfterFix = node.textLength
                        val blankLinesAfterFix = node.text.lines().size - countCodeLines(node)

                        // checking that any fix may have been made
                        isFixedSmthInChildNode = fixableType !is None

                        // for cases when text doesn't change, and then we need to stop fixes
                        if (textBeforeFix == textAfterFix) {
                            isFixedSmthInChildNode = false
                        }

                        // in some kernel cases of long lines, when in fix step we adding `\n` to certain place of the line
                        // and part of the line is transferred to the new line, this part may still be too long,
                        // and then in next fix step we can start generating unnecessary blank lines,
                        // to detect this we count blank lines and make unfix, if necessary
                        if (blankLinesAfterFix > blankLinesBeforeFix) {
                            isFixedSmthInChildNode = false
                            fixableType.unFix()
                        } else {
                            // we should keep in mind, that in the course of fixing we change the offset
                            // offset for all next nodes changed to this delta
                            offset += (textLenAfterFix - textLenBeforeFix)
                        }
                    }
                }
            }

            offset += line.length + 1
        }

        return isFixedSmthInChildNode
    }

    @Suppress(
        "TOO_LONG_FUNCTION",
        "LongMethod",
        "ComplexMethod",
        "GENERIC_VARIABLE_WRONG_DECLARATION",
    )
    private fun isFixable(wrongNode: ASTNode, configuration: LineLengthConfiguration): LongLineFixableCases {
        var parent = wrongNode
        var stringOrDot: ASTNode? = null
        do {
            when (parent.elementType) {
                BINARY_EXPRESSION, PARENTHESIZED -> {
                    val parentIsValArgListOrFunLitOrWhenEntry = listOf(VALUE_ARGUMENT_LIST, FUNCTION_LITERAL, WHEN_CONDITION_EXPRESSION)
                    findParentNodeMatching(parent, parentIsValArgListOrFunLitOrWhenEntry)?.let {
                        parent = it
                    } ?: run {
                        val splitOffset = searchRightSplitAfterOperationReference(parent, configuration)?.second
                        splitOffset?.let {
                            val parentIsBiExprOrParenthesized = parent.treeParent.elementType in listOf(BINARY_EXPRESSION, PARENTHESIZED)
                            val parentIsFunOrProperty = parent.treeParent.elementType in listOf(FUN, PROPERTY)
                            if (parentIsBiExprOrParenthesized || (parentIsFunOrProperty && splitOffset > configuration.lineLength)) {
                                parent = parent.treeParent
                            } else {
                                return checkBinaryExpression(parent, configuration)
                            }
                        }
                            ?: run {
                                stringOrDot?.let {
                                    val returnElem = checkStringTemplateAndDotQualifiedExpression(it, configuration)
                                    if (returnElem !is None) {
                                        return returnElem
                                    }
                                }
                                parent = parent.treeParent
                            }
                    }
                }
                FUN, PROPERTY -> return checkFunAndProperty(parent)
                VALUE_ARGUMENT_LIST -> parent.findParentNodeWithSpecificType(BINARY_EXPRESSION)?.let {
                    parent = it
                } ?: return checkArgumentsList(parent, configuration)
                WHEN_ENTRY -> return WhenEntry(parent)
                WHEN_CONDITION_EXPRESSION -> return None()
                EOL_COMMENT -> return checkComment(parent, configuration)
                FUNCTION_LITERAL -> return Lambda(parent)
                STRING_TEMPLATE, DOT_QUALIFIED_EXPRESSION, SAFE_ACCESS_EXPRESSION -> {
                    stringOrDot = parent
                    val parentIsBinExpOrValArgListOrWhenEntry = listOf(BINARY_EXPRESSION, VALUE_ARGUMENT_LIST, WHEN_CONDITION_EXPRESSION)
                    findParentNodeMatching(parent, parentIsBinExpOrValArgListOrWhenEntry)?.let {
                        parent = it
                    } ?: run {
                        val returnElem = checkStringTemplateAndDotQualifiedExpression(parent, configuration)
                        if (returnElem !is None) {
                            return returnElem
                        }
                        parent = parent.treeParent
                    }
                }
                else -> parent = parent.treeParent
            }
        } while (parent.treeParent != null)
        return None()
    }

    private fun findParentNodeMatching(node: ASTNode, listType: List<IElementType>): ASTNode? {
        listType.forEach { type ->
            node.findParentNodeWithSpecificType(type)?.let {
                return it
            }
        }
        return null
    }

    @Suppress("PARAMETER_NAME_IN_OUTER_LAMBDA")
    private fun checkArgumentsList(node: ASTNode, configuration: LineLengthConfiguration): LongLineFixableCases {
        node.findParentNodeWithSpecificType(WHEN_ENTRY)?.let {
            it.findChildByType(BLOCK)?.run {
                return ValueArgumentList(node, configuration, positionByOffset)
            } ?: return WhenEntry(it)
        }
        return ValueArgumentList(node, configuration, positionByOffset)
    }

    /**
     * Parses the existing binary expression and passes the necessary parameters to the fix function for splitting
     */
    private fun checkBinaryExpression(node: ASTNode, configuration: LineLengthConfiguration): LongLineFixableCases {
        val leftOffset = positionByOffset(node.firstChildNode.startOffset).second
        val binList: MutableList<ASTNode> = mutableListOf()
        searchBinaryExpression(node, binList)
        if (binList.size == 1) {
            return BinaryExpression(node)
        }
        return LongBinaryExpression(node, configuration, leftOffset, binList, positionByOffset)
    }

    @Suppress("TOO_MANY_LINES_IN_LAMBDA", "GENERIC_VARIABLE_WRONG_DECLARATION")
    private fun checkStringTemplateAndDotQualifiedExpression(
        node: ASTNode,
        configuration: LineLengthConfiguration
    ): LongLineFixableCases {
        val isPropertyOrFun = listOf(PROPERTY, FUN)
        val funOrPropertyNode = findParentNodeMatching(node, isPropertyOrFun)
        funOrPropertyNode?.let {
            if (it.hasChildOfType(EQ)) {
                val positionByOffset = positionByOffset(it.getFirstChildWithType(EQ)?.startOffset ?: 0).second
                if (positionByOffset < configuration.lineLength / 2) {
                    val returnedClass = parserStringAndDot(node, configuration)
                    if (returnedClass !is None) {
                        return returnedClass
                    }
                }
                return FunAndProperty(it)
            }
            return parserStringAndDot(node, configuration)
        } ?: return parserStringAndDot(node, configuration)
    }

    private fun parserStringAndDot(node: ASTNode, configuration: LineLengthConfiguration) =
        if (node.elementType == STRING_TEMPLATE) {
            parserStringTemplate(node, configuration)
        } else {
            parserDotQualifiedExpression(node, configuration)
        }

    /**
     * This class finds where the string can be split
     *
     * @return StringTemplate - if the string can be split,
     *         BinaryExpression - if there is two concatenated strings and new line should be inserted after `+`
     *         None - if the string can't be split
     */
    @Suppress("TOO_LONG_FUNCTION", "UnsafeCallOnNullableType")
    private fun parserStringTemplate(node: ASTNode, configuration: LineLengthConfiguration): LongLineFixableCases {
        var multiLineOffset = 0
        val leftOffset = if (node.text.lines().size > 1) {
            node
                .text
                .lines()
                .takeWhile { it.length < configuration.lineLength }
                .forEach { multiLineOffset += it.length }
            node
                .text
                .lines()
                .first { it.length > configuration.lineLength }
                .takeWhile { it.isWhitespace() }
                .count()
        } else {
            positionByOffset(node.startOffset).second
        }
        val delimiterIndex =
            node.text.substring(0, multiLineOffset + configuration.lineLength.toInt() - leftOffset).lastIndexOf(' ')
        if (delimiterIndex == -1) {
            // we can't split this string, however may be we can move it entirely:
            // case when new line should be inserted after `+`. Example: "first" + "second"
            node.treeParent.findChildByType(OPERATION_REFERENCE)?.let {
                return BinaryExpression(node.treeParent)
            }
            // can't fix this case
            return None()
        }
        // check, that space to split is a part of text - not code
        // If the space split is part of the code, then there is a chance of breaking the code when fixing, that why we should ignore it
        val isSpaceIsWhiteSpace = node.psi
            .findElementAt(delimiterIndex)!!
            .node
            .isWhiteSpace()
        if (isSpaceIsWhiteSpace) {
            return None()
        }
        // minus 2 here as we are inserting ` +` and we don't want it to exceed line length
        val shouldAddTwoSpaces =
            (multiLineOffset == 0) && (leftOffset + delimiterIndex > configuration.lineLength.toInt() - 2)
        val correcterDelimiter = if (shouldAddTwoSpaces) {
            node.text.substring(0, delimiterIndex - 2).lastIndexOf(' ')
        } else {
            delimiterIndex
        }
        if (correcterDelimiter == -1) {
            return None()
        }
        return StringTemplate(node, correcterDelimiter, multiLineOffset == 0)
    }

    private fun parserDotQualifiedExpression(
        wrongNode: ASTNode,
        configuration: LineLengthConfiguration
    ): LongLineFixableCases {
        val nodeDot = searchRightSplitBeforeDotOrSafeAccess(wrongNode, configuration, DOT)
        val nodeSafeAccess = searchRightSplitBeforeDotOrSafeAccess(wrongNode, configuration, SAFE_ACCESS)
        return nodeDot?.let {
            DotQualifiedExpression(wrongNode)
        } ?: nodeSafeAccess?.let {
            DotQualifiedExpression(wrongNode)
        } ?: None()
    }

    private fun checkFunAndProperty(wrongNode: ASTNode) =
        if (wrongNode.hasChildOfType(EQ)) FunAndProperty(wrongNode) else None()

    private fun checkComment(wrongNode: ASTNode, configuration: LineLengthConfiguration): LongLineFixableCases {
        val leftOffset = positionByOffset(wrongNode.startOffset).second
        val stringBeforeCommentContent = wrongNode.text.takeWhile { it == ' ' || it == '/' }
        if (stringBeforeCommentContent.length >= configuration.lineLength.toInt() - leftOffset) {
            return None()
        }
        val indexLastSpace = wrongNode.text.substring(stringBeforeCommentContent.length, configuration.lineLength.toInt() - leftOffset).lastIndexOf(' ')
        val isNewLine = wrongNode.treePrev?.isWhiteSpaceWithNewline() ?: wrongNode.treeParent?.treePrev?.isWhiteSpaceWithNewline() ?: false
        if (isNewLine && indexLastSpace == -1) {
            return None()
        }
        return Comment(wrongNode, isNewLine, indexLastSpace + stringBeforeCommentContent.length)
    }

    // fixme json method
    private fun isKdocValid(node: ASTNode) = try {
        if (node.elementType == TEXT) {
            URL(node.text.split("\\s".toRegex()).last { it.isNotEmpty() })
        } else {
            URL(node.text.substring(node.text.indexOfFirst { it == ']' } + 2, node.textLength - 1))
        }
        true
    } catch (e: MalformedURLException) {
        false
    }

    /**
     * This method uses recursion to store binary node in the order in which they are located
     * Also binList contains nodes with PREFIX_EXPRESSION element type ( !isFoo(), !isValid)
     *
     *@param node node in which to search
     *@param binList mutable list of ASTNode to store nodes
     */
    private fun searchBinaryExpression(node: ASTNode, binList: MutableList<ASTNode>) {
        if (node.hasChildOfType(BINARY_EXPRESSION) || node.hasChildOfType(PARENTHESIZED) || node.hasChildOfType(POSTFIX_EXPRESSION)) {
            node.getChildren(null)
                .forEach {
                    searchBinaryExpression(it, binList)
                }
        }
        if (node.elementType == BINARY_EXPRESSION) {
            binList.add(node)
            binList.add(node.treeParent.findChildByType(PREFIX_EXPRESSION) ?: return)
        }
    }

    /**
     * This method uses recursion to store dot qualified expression node in the order in which they are located
     * Also dotList contains nodes with PREFIX_EXPRESSION element type ( !isFoo(), !isValid))
     *
     *@param node node in which to search
     *@param dotList mutable list of ASTNode to store nodes
     */
    private fun searchDotOrSafeAccess(node: ASTNode, dotList: MutableList<ASTNode>) {
        if (node.elementType == DOT_QUALIFIED_EXPRESSION || node.elementType == SAFE_ACCESS_EXPRESSION || node.elementType == POSTFIX_EXPRESSION) {
            node.getChildren(null)
                .forEach {
                    searchDotOrSafeAccess(it, dotList)
                }
            if (node.elementType != POSTFIX_EXPRESSION) {
                dotList.add(node)
            }
        }
    }

    /**
     * Finds the first binary expression closer to the separator
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun searchRightSplitAfterOperationReference(
        parent: ASTNode,
        configuration: LineLengthConfiguration,
    ): Pair<ASTNode, Int>? {
        val list: MutableList<ASTNode> = mutableListOf()
        searchBinaryExpression(parent, list)
        return list.asSequence()
            .map {
                it to positionByOffset(it.getFirstChildWithType(OPERATION_REFERENCE)!!.startOffset).second
            }
            .sortedBy { it.second }
            .lastOrNull { (it, offset) ->
                offset + (it.getFirstChildWithType(OPERATION_REFERENCE)?.text?.length ?: 0) <= configuration.lineLength + 1
            }
    }

    /**
     * Finds the first dot or safe access closer to the separator
     */
    @Suppress(
        "MAGIC_NUMBER",
        "MagicNumber",
        "PARAMETER_NAME_IN_OUTER_LAMBDA"
    )
    private fun searchRightSplitBeforeDotOrSafeAccess(
        parent: ASTNode,
        configuration: LineLengthConfiguration,
        type: IElementType
    ): Pair<ASTNode, Int>? {
        val list: MutableList<ASTNode> = mutableListOf()
        searchDotOrSafeAccess(parent, list)
        val offsetFromMaximum = 10
        return list.asSequence()
            .map {
                val offset = it.getFirstChildWithType(type)?.run {
                    positionByOffset(this.startOffset).second
                } ?: run {
                    configuration.lineLength.toInt() + offsetFromMaximum
                }
                it to offset
            }
            .sortedBy { it.second }
            .lastOrNull { (_, offset) ->
                offset <= configuration.lineLength + 1
            }
    }

    /**
     *
     * [RuleConfiguration] for maximum line length
     */
    class LineLengthConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Maximum allowed line length
         */
        val lineLength = config["lineLength"]?.toLongOrNull() ?: MAX_LENGTH
    }

    /**
     * Class LongLineFixableCases is parent class for several specific error classes
     */
    @Suppress("KDOC_NO_CONSTRUCTOR_PROPERTY", "MISSING_KDOC_CLASS_ELEMENTS")  // todo add proper docs
    abstract class LongLineFixableCases(val node: ASTNode) {
        /**
         * Abstract fix - fix anything nodes
         */
        abstract fun fix()

        /**
         * Function unFix - unfix incorrect unnecessary fix-changes
         */
        @Suppress("EmptyFunctionBlock")
        open fun unFix() {
            // Nothing to do here by default.
        }
    }

    /**
     * Class None show error that long line have unidentified type or something else that we can't analyze
     */
    private class None : LongLineFixableCases(KotlinParser().createNode("ERROR")) {
        @Suppress("EmptyFunctionBlock")
        override fun fix() {}
    }

    /**
     * Class Comment show that long line should be split in comment
     * @property hasNewLineBefore flag to handle type of comment: ordinary comment (long part of which should be moved to the next line)
     * and inline comments (which should be moved entirely to the previous line)
     * @property indexLastSpace index of last space to substring comment
     */
    private class Comment(
        node: ASTNode,
        val hasNewLineBefore: Boolean,
        val indexLastSpace: Int = 0
    ) : LongLineFixableCases(node) {
        override fun fix() {
            if (this.hasNewLineBefore) {
                val indexLastSpace = this.indexLastSpace
                val nodeText = "//${node.text.substring(indexLastSpace, node.text.length)}"
                node.treeParent.apply {
                    addChild(LeafPsiElement(EOL_COMMENT, node.text.substring(0, indexLastSpace)), node)
                    addChild(PsiWhiteSpaceImpl("\n"), node)
                    addChild(LeafPsiElement(EOL_COMMENT, nodeText), node)
                    removeChild(node)
                }
            } else {
                if (node.treePrev.isWhiteSpace()) {
                    node.treeParent.removeChild(node.treePrev)
                }

                // for cases when property has multiline initialization, and then we need to move comment before first line
                val newLineNodeOnPreviousLine = if (node.treeParent.elementType == PROPERTY) {
                    node.treeParent.treeParent.findChildrenMatching {
                        it.elementType == WHITE_SPACE && node.treeParent.treeParent.isChildAfterAnother(node.treeParent, it) && it.textContains('\n')
                    }
                        .lastOrNull()
                } else {
                    node.findAllNodesWithConditionOnLine(node.getLineNumber() - 1) {
                        it.elementType == WHITE_SPACE && it.textContains('\n')
                    }?.lastOrNull()
                }

                newLineNodeOnPreviousLine?.let {
                    val parent = node.treeParent
                    parent.removeChild(node)
                    newLineNodeOnPreviousLine.treeParent.addChild(node, newLineNodeOnPreviousLine.treeNext)
                    newLineNodeOnPreviousLine.treeParent.addChild(PsiWhiteSpaceImpl("\n"), newLineNodeOnPreviousLine.treeNext.treeNext)
                }
            }
        }
    }

    /**
     * Class StringTemplate show that long line should be split in string template
     * @property delimiterIndex
     * @property isOneLineString
     */
    private class StringTemplate(
        node: ASTNode,
        val delimiterIndex: Int,
        val isOneLineString: Boolean
    ) : LongLineFixableCases(node) {
        override fun fix() {
            val incorrectText = node.text
            val firstPart = incorrectText.substring(0, delimiterIndex)
            val secondPart = incorrectText.substring(delimiterIndex, incorrectText.length)
            val textBetweenParts =
                if (isOneLineString) {
                    "\" +\n\""
                } else {
                    "\n"
                }
            val correctNode = KotlinParser().createNode("$firstPart$textBetweenParts$secondPart")
            node.treeParent.replaceChild(node, correctNode)
        }
    }

    /**
     * Class BinaryExpression show that long line should be split in short binary expression? after operation reference
     */
    private class BinaryExpression(node: ASTNode) : LongLineFixableCases(node) {
        override fun fix() {
            val binNode = if (node.elementType == PARENTHESIZED) {
                node.findChildByType(BINARY_EXPRESSION)
            } else {
                node
            }
            val nodeOperationReference = binNode?.findChildByType(OPERATION_REFERENCE)
            val nextNode = if (nodeOperationReference?.firstChildNode?.elementType != ELVIS) {
                nodeOperationReference?.treeNext
            } else {
                if (nodeOperationReference?.treePrev?.elementType == WHITE_SPACE) {
                    nodeOperationReference?.treePrev
                } else {
                    nodeOperationReference
                }
            }
            binNode?.appendNewlineMergingWhiteSpace(nextNode, nextNode)
        }
    }

    /**
     * Class LongBinaryExpression show that long line should be split between other parts long binary expression,
     * after one of operation reference
     * @property maximumLineLength is number of maximum line length
     * @property leftOffset is offset before start [node]
     * @property binList is list of Binary Expression which are children of [node]
     * @property positionByOffset
     */
    private class LongBinaryExpression(
        node: ASTNode,
        val maximumLineLength: LineLengthConfiguration,
        val leftOffset: Int,
        val binList: MutableList<ASTNode>,
        var positionByOffset: (Int) -> Pair<Int, Int>
    ) : LongLineFixableCases(node) {
        /**
         * Fix a binary expression -
         * - If the transfer is done on the Elvis operator, then transfers it to a new line
         * - If not on the Elvis operator, then transfers it to a new line after the operation reference
         */
        @Suppress("UnsafeCallOnNullableType")
        override fun fix() {
            val anySplitNode = searchSomeSplitInBinaryExpression(node, maximumLineLength)
            val rightSplitNode = anySplitNode[0] ?: anySplitNode[1] ?: anySplitNode[2]
            val nodeOperationReference = rightSplitNode?.first?.getFirstChildWithType(OPERATION_REFERENCE)
            rightSplitNode?.let {
                val nextNode = if (nodeOperationReference?.firstChildNode?.elementType != ELVIS) {
                    nodeOperationReference?.treeNext
                } else {
                    if (nodeOperationReference?.treePrev?.elementType == WHITE_SPACE) {
                        nodeOperationReference?.treePrev
                    } else {
                        nodeOperationReference
                    }
                }
                if (!nextNode?.text?.contains(("\n"))!!) {
                    rightSplitNode.first.appendNewlineMergingWhiteSpace(nextNode, nextNode)
                }
            }
        }

        /**
         * This method stored all the nodes that have [BINARY_EXPRESSION] or [PREFIX_EXPRESSION] element type.
         * - First elem in List - Logic Binary Expression (`&&`, `||`)
         * - Second elem in List - Comparison Binary Expression (`>`, `<`, `==`, `>=`, `<=`, `!=`, `===`, `!==`)
         * - Other types (Arithmetical and Bitwise operation) (`+`, `-`, `*`, `/`, `%`, `>>`, `<<`, `&`, `|`, `~`, `^`, `>>>`, `<<<`,
         *   `*=`, `+=`, `-=`, `/=`, `%=`, `++`, `--`, `in` `!in`, etc.)
         *
         * @return the list of node-to-offset pairs.
         */
        @Suppress("TYPE_ALIAS")
        private fun searchSomeSplitInBinaryExpression(parent: ASTNode, configuration: LineLengthConfiguration): List<Pair<ASTNode, Int>?> {
            val logicListOperationReference = listOf(OROR, ANDAND)
            val compressionListOperationReference = listOf(GT, LT, EQEQ, GTEQ, LTEQ, EXCLEQ, EQEQEQ, EXCLEQEQEQ)
            val binList: MutableList<ASTNode> = mutableListOf()
            searchBinaryExpression(parent, binList)
            val rightBinList = binList.map {
                it to positionByOffset(it.getFirstChildWithType(OPERATION_REFERENCE)?.startOffset ?: 0).second
            }
                .sortedBy { it.second }
                .reversed()
            val returnList: MutableList<Pair<ASTNode, Int>?> = mutableListOf()
            addInSmartListBinExpression(returnList, rightBinList, logicListOperationReference, configuration)
            addInSmartListBinExpression(returnList, rightBinList, compressionListOperationReference, configuration)
            val expression = rightBinList.firstOrNull { (it, offset) ->
                val binOperationReference = it.getFirstChildWithType(OPERATION_REFERENCE)?.firstChildNode?.elementType
                offset + (it.getFirstChildWithType(OPERATION_REFERENCE)?.text?.length ?: 0) <= configuration.lineLength + 1 &&
                        binOperationReference !in logicListOperationReference && binOperationReference !in compressionListOperationReference && binOperationReference != EXCL
            }
            returnList.add(expression)
            return returnList
        }

        private fun searchBinaryExpression(node: ASTNode, binList: MutableList<ASTNode>) {
            if (node.hasChildOfType(BINARY_EXPRESSION) || node.hasChildOfType(PARENTHESIZED) || node.hasChildOfType(POSTFIX_EXPRESSION)) {
                node.getChildren(null)
                    .forEach {
                        searchBinaryExpression(it, binList)
                    }
            }
            if (node.elementType == BINARY_EXPRESSION) {
                binList.add(node)
                binList.add(node.treeParent.findChildByType(PREFIX_EXPRESSION) ?: return)
            }
        }

        /**
         * Runs through the sorted list [rightBinList], finds its last element, the type of which is included in the set [typesList] and adds it in the list [returnList]
         */
        @Suppress("TYPE_ALIAS")
        private fun addInSmartListBinExpression(
            returnList: MutableList<Pair<ASTNode, Int>?>,
            rightBinList: List<Pair<ASTNode, Int>>,
            typesList: List<IElementType>,
            configuration: LineLengthConfiguration
        ) {
            val expression = rightBinList.firstOrNull { (it, offset) ->
                val binOperationReference = it.getFirstChildWithType(OPERATION_REFERENCE)
                offset + (it.getFirstChildWithType(OPERATION_REFERENCE)?.text?.length ?: 0) <= configuration.lineLength + 1 &&
                        binOperationReference?.firstChildNode?.elementType in typesList
            }
            returnList.add(expression)
        }
    }

    /**
     * Class FunAndProperty show that long line should be split in Fun Or Property: after EQ (between head and body this function)
     */
    private class FunAndProperty(node: ASTNode) : LongLineFixableCases(node) {
        override fun fix() {
            node.appendNewlineMergingWhiteSpace(null, node.findChildByType(EQ)?.treeNext)
        }

        override fun unFix() {
            node.findChildAfter(EQ, WHITE_SPACE)?.let { correctWhiteSpace ->
                if (correctWhiteSpace.textContains('\n')) {
                    correctWhiteSpace.nextSibling()?.let { wrongWhiteSpace ->
                        if (wrongWhiteSpace.textContains('\n')) {
                            node.removeChild(wrongWhiteSpace)
                        }
                    }
                }
            }
        }
    }

    /**
     * Class Lambda show that long line should be split in Lambda: in space after [LBRACE] node and before [RBRACE] node
     */
    private class Lambda(node: ASTNode) : LongLineFixableCases(node) {
        /**
         * Splits Lambda expressions - add splits lines, thereby making the lambda expression a separate line
         */
        override fun fix() {
            node.appendNewlineMergingWhiteSpace(node.findChildByType(LBRACE)?.treeNext, node.findChildByType(LBRACE)?.treeNext)
            node.appendNewlineMergingWhiteSpace(node.findChildByType(RBRACE)?.treePrev, node.findChildByType(RBRACE)?.treePrev)
        }
    }

    /**
     * Class DotQualifiedExpression show that line should be split in DotQualifiedExpression
     */
    private class DotQualifiedExpression(node: ASTNode) : LongLineFixableCases(node) {
        override fun fix() {
            val dot = node.getFirstChildWithType(DOT)
            val safeAccess = node.getFirstChildWithType(SAFE_ACCESS)
            val splitNode = if ((dot?.startOffset ?: 0) > (safeAccess?.startOffset ?: 0)) {
                dot
            } else {
                safeAccess
            }
            val nodeBeforeDot = splitNode?.treePrev
            node.appendNewlineMergingWhiteSpace(nodeBeforeDot, splitNode)
        }
    }

    /**
     * Class ValueArgumentList show that line should be split in ValueArgumentList:
     * @property maximumLineLength - max line length
     * @property positionByOffset
     */
    private class ValueArgumentList(
        node: ASTNode,
        val maximumLineLength: LineLengthConfiguration,
        var positionByOffset: (Int) -> Pair<Int, Int>
    ) : LongLineFixableCases(node) {
        override fun fix() {
            val lineLength = maximumLineLength.lineLength
            val offset = fixFirst()
            val listComma = node.getAllChildrenWithType(COMMA).map {
                it to positionByOffset(it.startOffset - offset).second
            }.sortedBy { it.second }
            var lineNumber = 1
            listComma.forEachIndexed { index, pair ->
                if (pair.second >= lineNumber * lineLength) {
                    lineNumber++
                    val commaSplit = if (index > 0) {
                        listComma[index - 1].first
                    } else {
                        pair.first
                    }
                    node.appendNewlineMergingWhiteSpace(commaSplit.treeNext, commaSplit.treeNext)
                }
            }
            node.getFirstChildWithType(RPAR)?.let { child ->
                if (positionByOffset(child.treePrev.startOffset).second + child.treePrev.text.length - offset > lineLength * lineNumber && listComma.isNotEmpty()) {
                    listComma.last().first.let {
                        node.appendNewlineMergingWhiteSpace(it.treeNext, it.treeNext)
                    }
                }
            }
        }

        override fun unFix() {
            node.findChildBefore(RPAR, WHITE_SPACE)?.let { correctWhiteSpace ->
                if (correctWhiteSpace.textContains('\n')) {
                    correctWhiteSpace.prevSibling()?.let { wrongWhiteSpace ->
                        if (wrongWhiteSpace.textContains('\n')) {
                            node.removeChild(wrongWhiteSpace)
                        }
                    }
                }
            }
        }

        private fun fixFirst(): Int {
            val lineLength = maximumLineLength.lineLength
            var startOffset = 0
            node.getFirstChildWithType(COMMA)?.let {
                if (positionByOffset(it.startOffset).second > lineLength) {
                    node.appendNewlineMergingWhiteSpace(node.findChildByType(LPAR)?.treeNext, node.findChildByType(LPAR)?.treeNext)
                    node.appendNewlineMergingWhiteSpace(node.findChildByType(RPAR), node.findChildByType(RPAR))
                    startOffset = this.maximumLineLength.lineLength.toInt()
                }
            } ?: node.getFirstChildWithType(RPAR)?.let {
                node.appendNewlineMergingWhiteSpace(node.findChildByType(LPAR)?.treeNext, node.findChildByType(LPAR)?.treeNext)
                node.appendNewlineMergingWhiteSpace(node.findChildByType(RPAR), node.findChildByType(RPAR))
                startOffset = this.maximumLineLength.lineLength.toInt()
            }
            return startOffset
        }
    }

    /**
     * Class WhenEntry show that line should be split in WhenEntry node:
     * - Added [LBRACE] and [RBRACE] nodes
     * - Split line in space after [LBRACE] node and before [RBRACE] node
     */
    private class WhenEntry(node: ASTNode) : LongLineFixableCases(node) {
        override fun fix() {
            node.getFirstChildWithType(ARROW)?.let {
                node.appendNewlineMergingWhiteSpace(it.treeNext, it.treeNext)
            }
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}
        private const val MAX_FIX_NUMBER = 10
        private const val MAX_LENGTH = 120L
        const val NAME_ID = "line-length"
    }
}
