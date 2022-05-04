package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.LONG_LINE
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.*

import com.pinterest.ktlint.core.ast.ElementType.ANNOTATION_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.BOOLEAN_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.CHARACTER_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.CONDITION
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.EQ
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.FLOAT_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.FUNCTION_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.IF
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_LIST
import com.pinterest.ktlint.core.ast.ElementType.INTEGER_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.KDOC_MARKDOWN_INLINE_LINK
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TEXT
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.LITERAL_STRING_TEMPLATE_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.LONG_STRING_TEMPLATE_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.LPAR
import com.pinterest.ktlint.core.ast.ElementType.NULL
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
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
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.isWhiteSpace
import com.pinterest.ktlint.core.ast.isWhiteSpaceWithNewline
import com.pinterest.ktlint.core.ast.nextSibling
import com.pinterest.ktlint.core.ast.parent
import com.pinterest.ktlint.core.ast.prevSibling
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.psi.psiUtil.parents

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
            // println(node.prettyPrint())
            // println(node.text)
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
                    LONG_LINE.warnAndFix(
                        configRules, emitWarn, isFixMode,
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
                    // if (fixableType !is None) {
                    // checkLength(fixableType.node, configuration)
                    // }
                }
            }
            offset += line.length + 1
        }
    }

    @Suppress(
        "TOO_LONG_FUNCTION",
        "ComplexMethod",
        "UnsafeCallOnNullableType"
    )
    private fun isFixable(wrongNode: ASTNode, configuration: LineLengthConfiguration): LongLineFixableCases {
        var parent = wrongNode
        do {
            when (parent.elementType) {
                BINARY_EXPRESSION -> {
                    val leftOffset = positionByOffset(parent.getFirstChildWithType(OPERATION_REFERENCE)!!.startOffset).second
                    if (leftOffset > configuration.lineLength) {
                        parent = parent.treeParent
                    } else if (parent.text.length < configuration.lineLength) {
                        val listParentSearch = listOf(BINARY_EXPRESSION, PARENTHESIZED, FUN)
                        if (parent.treeParent.elementType in listParentSearch || parent.treeParent.treeParent.elementType == FUNCTION_LITERAL) {
                            parent = parent.treeParent
                        } else {
                            return checkBinaryExpression(parent, configuration)
                        }
                    } else {
                        return checkBinaryExpression(parent, configuration)
                    }
                }
                FUN -> return checkFun(parent)
                CONDITION -> return checkCondition(parent, configuration)
                PROPERTY -> return checkProperty(parent)
                EOL_COMMENT -> return checkComment(parent, configuration)
                FUNCTION_LITERAL -> return Lambda(parent)
                STRING_TEMPLATE -> {
                    // as we are going from bottom to top we are excluding
                    // 1. IF, because it seems that string template is in condition
                    // 2. FUN with EQ, it seems that new line should be inserted after `=`
                    parent.findParentNodeWithSpecificType(IF)?.let {
                        parent = parent.treeParent
                    } ?: parent.findParentNodeWithSpecificType(FUN)?.let { node ->
                        // checking that string template is not in annotation
                        if (node.hasChildOfType(EQ) && !wrongNode.parents().any { it.elementType == ANNOTATION_ENTRY }) {
                            parent = node
                        } else {
                            return checkStringTemplate(parent, configuration)
                        }
                    } ?: return checkStringTemplate(parent, configuration)
                }
                else -> parent = parent.treeParent
            }
        } while (parent.treeParent != null)
        return None()
    }

    /**
     * This class finds where the string can be split
     *
     * @return StringTemplate - if the string can be split,
     *         BinaryExpression - if there is two concatenated strings and new line should be inserted after `+`
     *         None - if the string can't be split
     */
    private fun checkBinaryExpression(node: ASTNode, configuration: LineLengthConfiguration): LongLineFixableCases {
        val leftOffset = positionByOffset(node.firstChildNode.startOffset).second
        val binList: MutableList<ASTNode> = mutableListOf()
        searchBinaryExpression(node, binList)
        if (binList.size == 1) {
            return BinaryExpression(node)
        }
        return LongBinaryExpression(node, configuration.lineLength, leftOffset, binList)
    }

    @Suppress("TOO_LONG_FUNCTION", "UnsafeCallOnNullableType")
    private fun checkStringTemplate(node: ASTNode, configuration: LineLengthConfiguration): LongLineFixableCases {
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

    private fun checkFun(wrongNode: ASTNode) =
            if (wrongNode.hasChildOfType(EQ)) Fun(wrongNode) else None()

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
            return None()
        }
        return LongBinaryExpression(wrongNode, configuration.lineLength, leftOffset, binList)
    }

    private fun checkProperty(wrongNode: ASTNode) =
            if (wrongNode.hasChildOfType(EQ)) Fun(wrongNode) else None()

    @Suppress("UnsafeCallOnNullableType", "TOO_LONG_FUNCTION")
    private fun checkProperty(wrongNode: ASTNode, configuration: LineLengthConfiguration): LongLineFixableCases {
        var newParent = wrongNode
        while (newParent.hasChildOfType(PARENTHESIZED)) {
            newParent = wrongNode.findChildByType(PARENTHESIZED)!!
        }
        if (!newParent.hasChildOfType(STRING_TEMPLATE)) {
            if (newParent.hasChildOfType(BINARY_EXPRESSION)) {
                val leftOffset = positionByOffset(newParent.findChildByType(BINARY_EXPRESSION)!!.startOffset).second
                val binList: MutableList<ASTNode> = mutableListOf()
                dfsForProperty(wrongNode, binList)
                if (binList.size == 1) {
                    return BinaryExpression(wrongNode)
                }
                return LongBinaryExpression(wrongNode, configuration.lineLength, leftOffset, binList)
            } else {
                return None()
            }
        } else {
            val leftOffset = positionByOffset(newParent.findChildByType(STRING_TEMPLATE)!!.startOffset).second
            if (leftOffset > configuration.lineLength - STRING_PART_OFFSET) {
                return None()
            }
            val text = wrongNode.findChildByType(STRING_TEMPLATE)!!.text.trim('"')
            val lastCharIndex = configuration.lineLength.toInt() - leftOffset - STRING_PART_OFFSET
            val indexLastSpace = (text.substring(0, lastCharIndex).lastIndexOf(' '))
            if (indexLastSpace == -1) {
                return None()
            }
            return Property(wrongNode, indexLastSpace, text)
        }
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
            is Fun -> fixableType.node.appendNewlineMergingWhiteSpace(null, fixableType.node.findChildByType(EQ)!!.treeNext)
            is Comment -> fixComment(fixableType)
            is LongBinaryExpression -> fixLongBinaryExpression(fixableType)
            is BinaryExpression -> fixBinaryExpression(fixableType.node)
            is Property -> createSplitProperty(fixableType)
            is StringTemplate -> fixStringTemplate(fixableType)
            is Lambda -> fixLambda(fixableType.node)
            is None -> return
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun fixFun(node: ASTNode): Int {
        val lineOffset = positionByOffset(node.findChildByType(EQ)!!.startOffset).second
        node.appendNewlineMergingWhiteSpace(null, node.findChildByType(EQ)!!.treeNext)
        return lineOffset
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

    @Suppress("UnsafeCallOnNullableType")
    private fun fixBinaryExpression(node: ASTNode) {
        val whiteSpaceAfterPlus = node.findChildByType(OPERATION_REFERENCE)!!.treeNext
        node.appendNewlineMergingWhiteSpace(whiteSpaceAfterPlus, whiteSpaceAfterPlus)
    }

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
        val textBetwenParts =
                if (wrongStringTemplate.isOneLineString) {
                    "\" +\n\""
                } else {
                    "\n"
                }
        val correctNode = KotlinParser().createNode("$firstPart$textBetwenParts$secondPart")
        wrongStringTemplate.node.treeParent.replaceChild(wrongStringTemplate.node, correctNode)
    }

    /**
     * This method fix too long binary expression: split after OPERATION_REFERENCE closest to max length
     *
     * In this method we collect all binary expression in correct order and then
     * we collect their if their length less then max.
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun fixLongBinaryExpression(wrongBinaryExpression: LongBinaryExpression) {
        val leftOffset = wrongBinaryExpression.leftOffset
        val binList = wrongBinaryExpression.binList
        var binaryText = ""
        binList.forEachIndexed { index, astNode ->
            binaryText += findAllText(astNode)
            if (leftOffset + binaryText.length > wrongBinaryExpression.maximumLineLength && index != 0) {
                val commonParent = astNode.parent({ it.elementType == BINARY_EXPRESSION && it in binList[index - 1].parents() })!!
                val nextNode = commonParent.findChildByType(OPERATION_REFERENCE)!!.treeNext
                if (!nextNode.text.contains("\n")) {
                    commonParent.appendNewlineMergingWhiteSpace(nextNode, nextNode)
                }
                return
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun findAllText(astNode: ASTNode): String {
        var text = ""
        var node = astNode
        var prevNode: ASTNode
        do {
            prevNode = node
            node = node.treeParent
            if (node.elementType == PARENTHESIZED) {
                text += getTextFromParenthesized(node)
            }
        } while (node.elementType != BINARY_EXPRESSION)

        if (node.firstChildNode == prevNode) {
            if (node.treePrev != null && node.treePrev.elementType == WHITE_SPACE) {
                text += node.treePrev.text
            }
        } else {
            if (prevNode.treePrev != null && prevNode.treePrev.elementType == WHITE_SPACE) {
                text += prevNode.treePrev.text
            }
        }
        while (node.treeParent.elementType == PARENTHESIZED) {
            node = node.treeParent
            text += getBraceAndBeforeText(node, prevNode)
        }
        text += astNode.text
        node = astNode.parent({ newNode -> newNode.nextSibling { it.elementType == OPERATION_REFERENCE } != null },
            strict = false)
            ?: return text
        node = node.nextSibling { it.elementType == OPERATION_REFERENCE }!!
        if (node.treePrev.elementType == WHITE_SPACE) {
            text += node.treePrev.text
        }
        text += node.text
        return text
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun getBraceAndBeforeText(node: ASTNode, prevNode: ASTNode): String {
        val par = prevNode.prevSibling { it.elementType == OPERATION_REFERENCE }?.let { LPAR } ?: RPAR
        var text = ""
        if (node.findChildByType(par)!!.treePrev != null &&
                node.findChildByType(par)!!.treePrev.elementType == WHITE_SPACE) {
            text += node.findChildByType(par)!!.treePrev.text
        }
        text += node.findChildByType(par)!!.text
        return text
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun getTextFromParenthesized(node: ASTNode): String {
        var text = ""
        text += node.findChildByType(LPAR)!!.text
        if (node.findChildByType(LPAR)!!.treeNext.elementType == WHITE_SPACE) {
            text += node.findChildByType(LPAR)!!.treeNext.text
        }
        if (node.findChildByType(RPAR)!!.treePrev.elementType == WHITE_SPACE) {
            text += node.findChildByType(RPAR)!!.treePrev.text
        }
        text += node.findChildByType(RPAR)!!.text
        return text
    }

    /**
     * This method stored all the nodes that have BINARY_EXPRESSION or PREFIX_EXPRESSION element type.
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
        } else {
            binList.add(node)
            binList.add(node.treeParent.findChildByType(PREFIX_EXPRESSION) ?: return)
        }
    }

    /**
     * Collect by Depth-first search (DFS) all children to the right side of the equal sign with specific type [propertyList],
     * by which we can split expression.
     * Such logic needed, because AST representation of complex conditions is quite loaded
     *
     * @param node target node to be processed
     * @param binList where to store the corresponding results
     */
    private fun dfsForProperty(node: ASTNode, binList: MutableList<ASTNode>) {
        node.getChildren(null).forEach {
            if (it.elementType in propertyList) {
                if (it.elementType == REFERENCE_EXPRESSION && it.treeParent?.elementType == CALL_EXPRESSION) {
                    binList.add(it.treeParent)
                } else {
                    binList.add(it)
                }
            }
            dfsForProperty(it, binList)
        }
    }

    private fun createSplitProperty(wrongProperty: Property) {
        val node = wrongProperty.node
        val indexLastSpace = wrongProperty.indexLastSpace
        val text = wrongProperty.text
        splitTextAndCreateNode(node, text, indexLastSpace)
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun splitTextAndCreateNode(
        node: ASTNode,
        text: String,
        index: Int
    ) {
        val resultText = "\"${text.substring(0, index)}\" +\n\"${text.substring(index)}\""
        val newNode = KotlinParser().createNode(resultText)
        node.removeChild(node.findChildByType(STRING_TEMPLATE)!!)
        val prevExp = CompositeElement(BINARY_EXPRESSION)
        node.addChild(prevExp, null)
        prevExp.addChild(newNode, null)
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
    open class LongLineFixableCases(val node: ASTNode)

    /**
     * Class None show error long line have unidentified type or something else that we can't analyze
     */
    class None : LongLineFixableCases(KotlinParser().createNode("ERROR"))

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
    class Comment(
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
    class StringTemplate(
        node: ASTNode,
        val delimiterIndex: Int,
        val isOneLineString: Boolean
    ) : LongLineFixableCases(node)

    /**
     * Class BinaryExpression show that long line should be split in short binary expression? after operation reference
     */
    class BinaryExpression(node: ASTNode) : LongLineFixableCases(node)

    /**
     * Class LongBinaryExpression show that long line should be split between other parts long binary expression,
     * after one of operation reference
     * @property maximumLineLength
     * @property leftOffset
     * @property binList
     */
    class LongBinaryExpression(
        node: ASTNode,
        val maximumLineLength: Long,
        val leftOffset: Int,
        val binList: MutableList<ASTNode>
    ) : LongLineFixableCases(node)

    /**
     * Class Fun show that long line should be split in Fun: after EQ (between head and body this function)
     */
    class Fun(node: ASTNode) : LongLineFixableCases(node)

    /**
     * Class Lambda show that long line should be split in Comment: in space between two words
     */
    class Lambda(node: ASTNode) : LongLineFixableCases(node)

    /**
     * Class Property show that long line should be split in property: after a EQ
     * @property indexLastSpace
     * @property text
     */
    class Property(
        node: ASTNode,
        val indexLastSpace: Int,
        val text: String
    ) : LongLineFixableCases(node)

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
