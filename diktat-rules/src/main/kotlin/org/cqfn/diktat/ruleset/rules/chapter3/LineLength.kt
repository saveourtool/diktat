package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.LONG_LINE
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.*

import com.pinterest.ktlint.core.ast.ElementType.ANDAND
import com.pinterest.ktlint.core.ast.ElementType.ARROW
import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.BOOLEAN_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.CHARACTER_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.COMMA
import com.pinterest.ktlint.core.ast.ElementType.CONDITION
import com.pinterest.ktlint.core.ast.ElementType.DOT
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.ELVIS
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.EQ
import com.pinterest.ktlint.core.ast.ElementType.EQEQ
import com.pinterest.ktlint.core.ast.ElementType.EXCL
import com.pinterest.ktlint.core.ast.ElementType.EXCLEQ
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.FLOAT_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.FUNCTION_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.GT
import com.pinterest.ktlint.core.ast.ElementType.GTEQ
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_LIST
import com.pinterest.ktlint.core.ast.ElementType.INTEGER_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.KDOC_MARKDOWN_INLINE_LINK
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TEXT
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.LITERAL_STRING_TEMPLATE_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.LONG_STRING_TEMPLATE_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.LPAR
import com.pinterest.ktlint.core.ast.ElementType.LT
import com.pinterest.ktlint.core.ast.ElementType.LTEQ
import com.pinterest.ktlint.core.ast.ElementType.NULL
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.OROR
import com.pinterest.ktlint.core.ast.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.PARENTHESIZED
import com.pinterest.ktlint.core.ast.ElementType.POSTFIX_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.PREFIX_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.RBRACE
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.RPAR
import com.pinterest.ktlint.core.ast.ElementType.SHORT_STRING_TEMPLATE_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.STRING_TEMPLATE
import com.pinterest.ktlint.core.ast.ElementType.VALUE_ARGUMENT_LIST
import com.pinterest.ktlint.core.ast.ElementType.WHEN_CONDITION_WITH_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.WHEN_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.isWhiteSpace
import com.pinterest.ktlint.core.ast.isWhiteSpaceWithNewline
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType

import java.net.MalformedURLException
import java.net.URL

/**
 * The rule checks for lines in the file that exceed the maximum length.
 * Rule ignores URL in KDoc. Rule also can fix some cases.
 * Rule can fix long binary expressions in condition inside `if` and in property declarations and one line functions
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
        if (node.elementType == FILE) {
            node.getChildren(null).forEach {
                if (it.elementType != PACKAGE_DIRECTIVE && it.elementType != IMPORT_LIST) {
                    checkLength(it, configuration)
                }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType", "TOO_LONG_FUNCTION")
    private fun checkLength(node: ASTNode, configuration: LineLengthConfiguration) {
        var offset = 0
        node.text.lines().forEach { line ->
            if (line.length > configuration.lineLength) {
                val newNode = node.psi.findElementAt(offset + configuration.lineLength.toInt() - 1)!!.node
                if ((newNode.elementType != KDOC_TEXT && newNode.elementType != KDOC_MARKDOWN_INLINE_LINK) ||
                        !isKdocValid(newNode)
                ) {
                    positionByOffset = node.treeParent.calculateLineColByOffset()
                    val fixableType = isFixable(newNode, configuration)
                    LONG_LINE.warnAndFix(configRules, emitWarn, isFixMode,
                        "max line length ${configuration.lineLength}, but was ${line.length}",
                        offset + node.startOffset, node, fixableType !is None
                    ) {
                        // we should keep in mind, that in the course of fixing we change the offset
                        val textLenBeforeFix = node.textLength
                        fixError(fixableType)
                        val textLenAfterFix = node.textLength
                        // offset for all next nodes changed to this delta
                        offset += (textLenAfterFix - textLenBeforeFix)
                    }
                }
            }
            offset += line.length + 1
        }
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
                    val parentIsValArgListOrFunLitOrWhenEntry = listOf<IElementType>(VALUE_ARGUMENT_LIST, FUNCTION_LITERAL, WHEN_CONDITION_WITH_EXPRESSION)
                    findParentNodeWithSpecificTypeMany(parent, parentIsValArgListOrFunLitOrWhenEntry)?.let {
                        parent = it
                    } ?: run {
                        val splitOffset = searchRightSplitAfterType(parent, configuration, OPERATION_REFERENCE)?.second
                        splitOffset?.let {
                            val parentIsBiExprOrParenthesized = parent.treeParent.elementType in listOf(BINARY_EXPRESSION, PARENTHESIZED)
                            val parentIsFunOrProperty = parent.treeParent.elementType in listOf(FUN, PROPERTY)
                            if (parentIsBiExprOrParenthesized) {
                                parent = parent.treeParent
                            } else if (parentIsFunOrProperty && splitOffset >= configuration.lineLength) {
                                stringOrDot?.let {
                                    val returnElem = checkStringTemplateAndDotQualifiedExpression(parent, configuration)
                                    if (returnElem !is None) {
                                        return returnElem
                                    }
                                }
                                parent = parent.treeParent
                            } else {
                                return checkBinaryExpression(parent, configuration)
                            }
                        }
                            ?: run { parent = parent.treeParent }
                    }
                }
                FUN, PROPERTY -> return checkFunAndProperty(parent)
                CONDITION -> return checkCondition(parent, configuration)
                VALUE_ARGUMENT_LIST -> parent.findParentNodeWithSpecificType(BINARY_EXPRESSION)?.let {
                    parent = it
                } ?: run {
                    if (parent.treeParent.treeParent.elementType == WHEN_ENTRY) {
                        return WhenEntry(parent.treeParent.treeParent)
                    }
                    return ValueArgumentList(parent, configuration)
                }
                WHEN_CONDITION_WITH_EXPRESSION -> return None()
                EOL_COMMENT -> return checkComment(parent, configuration)
                FUNCTION_LITERAL -> return Lambda(parent)
                STRING_TEMPLATE, DOT_QUALIFIED_EXPRESSION -> {
                    stringOrDot = parent
                    val parentIsBinExpOrValArgListOrWhenEntry = listOf<IElementType>(BINARY_EXPRESSION, VALUE_ARGUMENT_LIST, WHEN_CONDITION_WITH_EXPRESSION)
                    findParentNodeWithSpecificTypeMany(parent, parentIsBinExpOrValArgListOrWhenEntry)?.let {
                        parent = it
                    } ?: run {
                        val parentIsPropertyOrFun = listOf<IElementType>(PROPERTY, FUN)
                        val parenPropertyOrFunNode = findParentNodeWithSpecificTypeMany(parent, parentIsPropertyOrFun)
                        val returnElem = checkStringTemplateAndDotQualifiedExpression(parent, configuration, parenPropertyOrFunNode)
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

    private fun findParentNodeWithSpecificTypeMany(node: ASTNode, listType: List<IElementType>): ASTNode? {
        listType.forEach { type ->
            node.findParentNodeWithSpecificType(type)?.let {
                return it
            }
        }
        return null
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
        return LongBinaryExpression(node, configuration, leftOffset, binList)
    }

    @Suppress("TOO_MANY_LINES_IN_LAMBDA")
    private fun checkStringTemplateAndDotQualifiedExpression(
        node: ASTNode,
        configuration: LineLengthConfiguration,
        funOrPropertyNode: ASTNode? = null
    ): LongLineFixableCases {
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
        val delimiterIndex = node.text.substring(0, multiLineOffset + configuration.lineLength.toInt() - leftOffset).lastIndexOf(' ')
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
        val isSpaceIsWhiteSpace = node.psi.findElementAt(delimiterIndex)!!.node.isWhiteSpace()
        if (isSpaceIsWhiteSpace) {
            return None()
        }
        // minus 2 here as we are inserting ` +` and we don't want it to exceed line length
        val shouldAddTwoSpaces = (multiLineOffset == 0) && (leftOffset + delimiterIndex > configuration.lineLength.toInt() - 2)
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

    private fun parserDotQualifiedExpression(wrongNode: ASTNode, configuration: LineLengthConfiguration): LongLineFixableCases {
        val nodeDot = searchRightSplitAfterType(wrongNode, configuration, DOT)?.first
        nodeDot?.let {
            return DotQualifiedExpression(wrongNode)
        } ?: return None()
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

    private fun checkCondition(wrongNode: ASTNode, configuration: LineLengthConfiguration): LongLineFixableCases {
        val leftOffset = positionByOffset(wrongNode.firstChildNode.startOffset).second
        val binList: MutableList<ASTNode> = mutableListOf()
        searchBinaryExpression(wrongNode, binList)
        if (binList.size == 1) {
            return BinaryExpression(wrongNode)
        }
        return LongBinaryExpression(wrongNode, configuration, leftOffset, binList)
    }

    // fixme json method
    private fun isKdocValid(node: ASTNode) = try {
        if (node.elementType == KDOC_TEXT) {
            URL(node.text.split("\\s".toRegex()).last { it.isNotEmpty() })
        } else {
            URL(node.text.substring(node.text.indexOfFirst { it == ']' } + 2, node.textLength - 1))
        }
        true
    } catch (e: MalformedURLException) {
        false
    }

    @Suppress("UnsafeCallOnNullableType", "WHEN_WITHOUT_ELSE")
    private fun fixError(fixableType: LongLineFixableCases) {
        when (fixableType) {
            is FunAndProperty -> fixableType.node.appendNewlineMergingWhiteSpace(null, fixableType.node.findChildByType(EQ)!!.treeNext)
            is Comment -> fixComment(fixableType)
            is LongBinaryExpression -> fixLongBinaryExpression(fixableType)
            is BinaryExpression -> fixBinaryExpression(fixableType.node)
            is StringTemplate -> fixStringTemplate(fixableType)
            is DotQualifiedExpression -> fixDotQualifiedExpression(fixableType)
            is ValueArgumentList -> fixArgumentList(fixableType)
            is Lambda -> fixLambda(fixableType.node)
            is WhenEntry -> fixWhenEntry(fixableType)
            is None -> return
        }
    }

    private fun fixWhenEntry(wrongWhenEntry: WhenEntry) {
        val node = wrongWhenEntry.node
        node.getFirstChildWithType(ARROW)?.let {
            node.addChild(PsiWhiteSpaceImpl(" "), it.treeNext)
            node.addChild(PsiWhiteSpaceImpl("{"), it.treeNext.treeNext)
            node.appendNewlineMergingWhiteSpace(it.treeNext.treeNext.treeNext, it.treeNext.treeNext.treeNext)
        }
        node.appendNewlineMergingWhiteSpace(node.lastChildNode.treeNext, node.lastChildNode.treeNext)
        node.addChild(PsiWhiteSpaceImpl("}"), node.lastChildNode.treeNext)
    }

    private fun fixDotQualifiedExpression(wrongDotQualifiedExpression: DotQualifiedExpression) {
        val node = wrongDotQualifiedExpression.node
        val dot = node.getFirstChildWithType(DOT)
        node.appendNewlineMergingWhiteSpace(dot, dot)
    }

    @Suppress("UnsafeCallOnNullableType", "MagicNumber")
    private fun fixArgumentsListFirstArgument(wrongArgumentList: ValueArgumentList): Int {
        val lineLength = wrongArgumentList.maximumLineLength.lineLength
        val node = wrongArgumentList.node
        var startOffset = 0
        node.getFirstChildWithType(COMMA)?.let {
            if (positionByOffset(it.startOffset).second > lineLength) {
                node.appendNewlineMergingWhiteSpace(node.findChildByType(LPAR)!!.treeNext, node.findChildByType(LPAR)!!.treeNext)
                node.appendNewlineMergingWhiteSpace(node.findChildByType(RPAR), node.findChildByType(RPAR))
                startOffset = wrongArgumentList.maximumLineLength.lineLength.toInt()
            }
        } ?: node.getFirstChildWithType(RPAR)?.let {
            node.appendNewlineMergingWhiteSpace(node.findChildByType(LPAR)!!.treeNext, node.findChildByType(LPAR)!!.treeNext)
            node.appendNewlineMergingWhiteSpace(node.findChildByType(RPAR), node.findChildByType(RPAR))
            startOffset = wrongArgumentList.maximumLineLength.lineLength.toInt()
        }
        return startOffset
    }

    /**
     * Fix arguments in arguments list
     */
    private fun fixArgumentList(wrongArgumentList: ValueArgumentList) {
        val lineLength = wrongArgumentList.maximumLineLength.lineLength
        val node = wrongArgumentList.node
        val offset = fixArgumentsListFirstArgument(wrongArgumentList)
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
        node.getFirstChildWithType(RPAR)?.let {
            if (positionByOffset(it.treePrev.startOffset).second + it.treePrev.text.length - offset > lineLength * lineNumber && listComma.isNotEmpty()) {
                listComma.last().first.run {
                    node.appendNewlineMergingWhiteSpace(this.treeNext, this.treeNext)
                }
            }
        }
    }

    private fun fixComment(wrongComment: Comment) {
        val wrongNode = wrongComment.node
        if (wrongComment.hasNewLineBefore) {
            val indexLastSpace = wrongComment.indexLastSpace
            val nodeText = "//${wrongNode.text.substring(indexLastSpace, wrongNode.text.length)}"
            wrongNode.treeParent.apply {
                addChild(LeafPsiElement(EOL_COMMENT, wrongNode.text.substring(0, indexLastSpace)), wrongNode)
                addChild(PsiWhiteSpaceImpl("\n"), wrongNode)
                addChild(LeafPsiElement(EOL_COMMENT, nodeText), wrongNode)
                removeChild(wrongNode)
            }
        } else {
            if (wrongNode.treePrev.isWhiteSpace()) {
                wrongNode.treeParent.removeChild(wrongNode.treePrev)
            }

            val newLineNodeOnPreviousLine = wrongNode.findAllNodesWithConditionOnLine(wrongNode.getLineNumber() - 1) {
                it.elementType == WHITE_SPACE && it.textContains('\n')
            }?.lastOrNull()

            newLineNodeOnPreviousLine?.let {
                val parent = wrongNode.treeParent
                parent.removeChild(wrongNode)
                newLineNodeOnPreviousLine.treeParent.addChild(wrongNode, newLineNodeOnPreviousLine.treeNext)
                newLineNodeOnPreviousLine.treeParent.addChild(PsiWhiteSpaceImpl("\n"), newLineNodeOnPreviousLine.treeNext.treeNext)
            }
        }
    }

    /**
     * Fix a binary expression -
     * If the transfer is done on the Elvis operator, then transfers it to a new line
     * If not on the Elvis operator, then transfers it to a new line after the operation reference
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun fixBinaryExpression(node: ASTNode) {
        val nodeOperationReference = node.findChildByType(OPERATION_REFERENCE)
        val nextNode = if (nodeOperationReference!!.firstChildNode.elementType != ELVIS) {
            nodeOperationReference.treeNext
        } else {
            if (nodeOperationReference.treePrev.elementType == WHITE_SPACE) {
                nodeOperationReference.treePrev
            } else {
                nodeOperationReference
            }
        }
        node.appendNewlineMergingWhiteSpace(nextNode, nextNode)
    }

    /**
     * Splits Lambda expressions - add splits lines, thereby making the lambda expression a separate line
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun fixLambda(node: ASTNode) {
        node.appendNewlineMergingWhiteSpace(node.findChildByType(LBRACE)!!.treeNext, node.findChildByType(LBRACE)!!.treeNext)
        node.appendNewlineMergingWhiteSpace(node.findChildByType(RBRACE)!!.treePrev, node.findChildByType(RBRACE)!!.treePrev)
    }

    @Suppress("UnsafeCallOnNullableType", "COMMENT_WHITE_SPACE")
    private fun fixStringTemplate(wrongStringTemplate: StringTemplate) {
        val incorrectText = wrongStringTemplate.node.text
        val firstPart = incorrectText.substring(0, wrongStringTemplate.delimiterIndex)
        val secondPart = incorrectText.substring(wrongStringTemplate.delimiterIndex, incorrectText.length)
        val textBetweenParts =
                if (wrongStringTemplate.isOneLineString) {
                    "\" +\n\""
                } else {
                    "\n"
                }
        val correctNode = KotlinParser().createNode("$firstPart$textBetweenParts$secondPart")
        wrongStringTemplate.node.treeParent.replaceChild(wrongStringTemplate.node, correctNode)
    }

    /**
     * Finds where it is better to fix a Binary expression and fixes it
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun fixLongBinaryExpression(wrongBinaryExpression: LongBinaryExpression) {
        val anySplitNode = searchSomeSplitInBinaryExpression(wrongBinaryExpression.node, wrongBinaryExpression.maximumLineLength)
        val rigthSplitnode = anySplitNode[0] ?: anySplitNode[1] ?: anySplitNode[2]
        val nodeOperationReference = rigthSplitnode?.first?.getFirstChildWithType(OPERATION_REFERENCE)
        rigthSplitnode?.let {
            val nextNode = if (nodeOperationReference!!.firstChildNode.elementType != ELVIS) {
                nodeOperationReference.treeNext
            } else {
                if (nodeOperationReference.treePrev.elementType == WHITE_SPACE) {
                    nodeOperationReference.treePrev
                } else {
                    nodeOperationReference
                }
            }
            if (!nextNode.text.contains(("\n"))) {
                rigthSplitnode.first.appendNewlineMergingWhiteSpace(nextNode, nextNode)
            }
        }
    }

    /**
     * This method uses recursion to store binary node in the order in which they are located
     * Also binList contains nodes with PREFIX_EXPRESSION element type ( !isFoo(), !isValid)
     *
     *@param node node in which to search
     *@param binList mutable list of ASTNode to store nodes
     */
    private fun searchBinaryExpression(node: ASTNode, binList: MutableList<ASTNode>) {
        if (node.hasChildOfType(BINARY_EXPRESSION) || node.hasChildOfType(PARENTHESIZED) ||
                node.hasChildOfType(POSTFIX_EXPRESSION)) {
            node.getChildren(null)
                .filter {
                    it.elementType == BINARY_EXPRESSION || it.elementType == PARENTHESIZED ||
                            it.elementType == POSTFIX_EXPRESSION
                }
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
     * Also dotList contains nodes with PREFIX_EXPRESSION element type ( !isFoo(), !isValid)
     *
     *@param node node in which to search
     *@param dotList mutable list of ASTNode to store nodes
     */
    private fun searchDot(node: ASTNode, dotList: MutableList<ASTNode>) {
        if (node.elementType == DOT_QUALIFIED_EXPRESSION) {
            node.getChildren(null)
                .filter {
                    it.elementType == DOT_QUALIFIED_EXPRESSION
                }
                .forEach {
                    searchDot(it, dotList)
                }
            dotList.add(node)
        }
    }

    /**
     * This method stored all the nodes that have BINARY_EXPRESSION or PREFIX_EXPRESSION element type.
     * Return List of the Pair <node, offset>
     * First elem in List - Logic Binary Expression (&&  ||)
     * Second elem in List - Comparison Binary Expression (> < == >= <= !=)
     * Other types (Arithmetical and Bit operation) (+ - * / % >> << *= += -= /= %= ++ -- ! in !in etc)
     */
    @Suppress("TYPE_ALIAS", "UnsafeCallOnNullableType")
    private fun searchSomeSplitInBinaryExpression(parent: ASTNode, configuration: LineLengthConfiguration): List<Pair<ASTNode, Int>?> {
        val logicListOperationReference = listOf(OROR, ANDAND)
        val compressionListOperationReference = listOf(GT, LT, EQEQ, GTEQ, LTEQ, EXCLEQ)
        val binList: MutableList<ASTNode> = mutableListOf()
        searchBinaryExpression(parent, binList)
        val rightBinList = binList.map {
            it to positionByOffset(it.getFirstChildWithType(OPERATION_REFERENCE)!!.startOffset).second
        }
            .sortedBy { it.second }
            .reversed()
        val returnList: MutableList<Pair<ASTNode, Int>?> = mutableListOf()
        addInSmartListBinExpression(returnList, rightBinList, logicListOperationReference, configuration)
        addInSmartListBinExpression(returnList, rightBinList, compressionListOperationReference, configuration)
        val expression = rightBinList.firstOrNull { (it, offset) ->
            val binOperationReference = it.getFirstChildWithType(OPERATION_REFERENCE)!!.firstChildNode.elementType
            offset + (it.getFirstChildWithType(OPERATION_REFERENCE)?.text!!.length ?: 0) <= configuration.lineLength + 1 &&
                    binOperationReference !in logicListOperationReference && binOperationReference !in compressionListOperationReference && binOperationReference != EXCL
        }
        returnList.add(expression)
        return returnList
    }

    /**
     * Runs through the sorted list [rightBinList], finds its last element, the type of which is included in the set [typesList] and adds it in the list [returnList]
     */
    @Suppress("TYPE_ALIAS", "UnsafeCallOnNullableType")
    private fun addInSmartListBinExpression(
        returnList: MutableList<Pair<ASTNode, Int>?>,
        rightBinList: List<Pair<ASTNode, Int>>,
        typesList: List<IElementType>,
        configuration: LineLengthConfiguration
    ) {
        val expression = rightBinList.firstOrNull { (it, offset) ->
            val binOperationReference = it.getFirstChildWithType(OPERATION_REFERENCE)
            offset + (it.getFirstChildWithType(OPERATION_REFERENCE)?.text!!.length ?: 0) <= configuration.lineLength + 1 &&
                    binOperationReference!!.firstChildNode.elementType in typesList
        }
        returnList.add(expression)
    }

    /**
     * Finds the first binary expression or dat closer to the separator
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun searchRightSplitAfterType(
        parent: ASTNode,
        configuration: LineLengthConfiguration,
        type: IElementType
    ): Pair<ASTNode, Int>? {
        val list: MutableList<ASTNode> = mutableListOf()
        when (type) {
            OPERATION_REFERENCE -> searchBinaryExpression(parent, list)
            DOT -> searchDot(parent, list)
        }
        return list.map {
            it to positionByOffset(it.getFirstChildWithType(type)?.startOffset ?: configuration.lineLength.toInt()).second
        }
            .sortedBy { it.second }
            .reversed()
            .firstOrNull { (it, offset) ->
                offset + (it.getFirstChildWithType(type)?.text?.length ?: 0) <= configuration.lineLength + 1
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
    sealed class LongLineFixableCases(val node: ASTNode)

    /**
     * Class None show error long line have unidentified type or something else that we can't analyze
     */
    private class None : LongLineFixableCases(KotlinParser().createNode("ERROR"))

    /**
     * @property node node
     * @property hasNewLineBefore flag to handle type of comment: ordinary comment (long part of which should be moved to the next line)
     * and inline comments (which should be moved entirely to the previous line)
     * @property indexLastSpace index of last space to substring comment
     */

    /**
     * Class Comment show that long line should be split in comment
     * @property hasNewLineBefore
     * @property indexLastSpace
     */
    private class Comment(
        node: ASTNode,
        val hasNewLineBefore: Boolean,
        val indexLastSpace: Int = 0
    ) : LongLineFixableCases(node)

    /**
     * @property node node
     * @property delimiterIndex index to split
     * @property isOneLineString flag is string is one line
     */

    /**
     * Class StringTemplate show that long line should be split in string template
     * @property delimiterIndex
     * @property isOneLineString
     */
    private class StringTemplate(
        node: ASTNode,
        val delimiterIndex: Int,
        val isOneLineString: Boolean
    ) : LongLineFixableCases(node)

    /**
     * Class BinaryExpression show that long line should be split in short binary expression? after operation reference
     */
    private class BinaryExpression(node: ASTNode) : LongLineFixableCases(node)

    /**
     * Class LongBinaryExpression show that long line should be split between other parts long binary expression,
     * after one of operation reference
     * @property maximumLineLength
     * @property leftOffset
     * @property binList
     */
    private class LongBinaryExpression(
        node: ASTNode,
        val maximumLineLength: LineLengthConfiguration,
        val leftOffset: Int,
        val binList: MutableList<ASTNode>
    ) : LongLineFixableCases(node)

    /**
     * Class FunAndProperty show that long line should be split in Fun Or Property: after EQ (between head and body this function)
     */
    private class FunAndProperty(node: ASTNode) : LongLineFixableCases(node)

    /**
     * Class Lambda show that long line should be split in Lambda: in space after LBRACE node and before RBRACE node
     */
    private class Lambda(node: ASTNode) : LongLineFixableCases(node)

    /**
     * Class DotQualifiedExpression show that line should be split in DotQualifiedExpression
     */
    private class DotQualifiedExpression(node: ASTNode) : LongLineFixableCases(node)

    /**
     * Class ValueArgumentList show that line should be split in ValueArgumentList:
     * @property maximumLineLength - max line length
     */
    private class ValueArgumentList(node: ASTNode, val maximumLineLength: LineLengthConfiguration) : LongLineFixableCases(node)

    /**
     * Class WhenEntry show that line should be split in WhenEntry node:
     * Added LBRACE and RBRACE nodes
     * Split line in space after LBRACE node and before RBRACE node
     */
    private class WhenEntry(node: ASTNode) : LongLineFixableCases(node)

    /**
     * val text = "first part" +
     * "second part" +
     * "third part"
     * STRING_PART_OFFSET equal to the left offset of first string part("first part") =
     * white space + close quote (open quote removed by trim) + white space + plus sign
     */
    companion object {
        private const val MAX_LENGTH = 120L
        const val NAME_ID = "abv-line-length"
        private const val STRING_PART_OFFSET = 4
        private val propertyList = listOf(INTEGER_CONSTANT, LITERAL_STRING_TEMPLATE_ENTRY, FLOAT_CONSTANT,
            CHARACTER_CONSTANT, REFERENCE_EXPRESSION, BOOLEAN_CONSTANT, LONG_STRING_TEMPLATE_ENTRY,
            SHORT_STRING_TEMPLATE_ENTRY, NULL)
    }
}
