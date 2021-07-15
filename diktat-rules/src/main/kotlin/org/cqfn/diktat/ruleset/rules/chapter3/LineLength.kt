package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.LONG_LINE
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.KotlinParser
import org.cqfn.diktat.ruleset.utils.appendNewlineMergingWhiteSpace
import org.cqfn.diktat.ruleset.utils.calculateLineColByOffset
import org.cqfn.diktat.ruleset.utils.findParentNodeWithSpecificType
import org.cqfn.diktat.ruleset.utils.hasChildOfType

import com.pinterest.ktlint.core.ast.ElementType.ANNOTATION_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.BOOLEAN_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.CHARACTER_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.CONDITION
import com.pinterest.ktlint.core.ast.ElementType.DOT
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
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
import com.pinterest.ktlint.core.ast.ElementType.LAMBDA_EXPRESSION
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
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.RPAR
import com.pinterest.ktlint.core.ast.ElementType.SAFE_ACCESS
import com.pinterest.ktlint.core.ast.ElementType.SAFE_ACCESS_EXPRESSION
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
    "line-length",
    configRules,
    listOf(LONG_LINE)) {
    private lateinit var positionByOffset: (Int) -> Pair<Int, Int>

    override fun logic(node: ASTNode) {
        val configuration = LineLengthConfiguration(
            configRules.getRuleConfig(LONG_LINE)?.configuration ?: emptyMap())

        if (node.elementType == FILE) {
            node.getChildren(null).forEach {
                if (it.elementType != PACKAGE_DIRECTIVE || it.elementType != IMPORT_LIST) {
                    checkLength(it, configuration)
                }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkLength(node: ASTNode, configuration: LineLengthConfiguration) {
        var offset = 0
        node.text.lines().forEach { line ->
            if (line.length > configuration.lineLength) {
                val newNode = node.psi.findElementAt(offset + configuration.lineLength.toInt() - 1)!!.node
                println("\n------------------\n[isFixMode: ${isFixMode}] NEW_NODE ${newNode.elementType} | ${newNode.text} IN ${node.psi.text.substring(offset + configuration.lineLength.toInt() - 1, minOf(offset + configuration.lineLength.toInt() + 5, offset + line.length))}")
                if ((newNode.elementType != KDOC_TEXT && newNode.elementType != KDOC_MARKDOWN_INLINE_LINK) ||
                        !isKdocValid(newNode)) {
                    positionByOffset = node.treeParent.calculateLineColByOffset()
                    val fixableType = isFixable(newNode, configuration)
                    println("CAN BE FIXED? ${fixableType != LongLineFixableCases.None}")
                    LONG_LINE.warnAndFix(configRules, emitWarn, isFixMode,
                        "max line length ${configuration.lineLength}, but was ${line.length}",
                        offset + node.startOffset, node, fixableType != LongLineFixableCases.None) {
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

    private fun isFixable(wrongNode: ASTNode, configuration: LineLengthConfiguration): LongLineFixableCases {
        var parent = wrongNode
        do {
            when (parent.elementType) {
                FUN -> return checkFun(parent)
                CONDITION -> return checkCondition(parent, configuration)
                PROPERTY -> return checkProperty(parent, configuration)
                EOL_COMMENT -> return checkComment(parent, configuration)
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
        return LongLineFixableCases.None
    }

    /**
     * This class finds where the string can be split
     *
     * @return StringTemplate - if the string can be split,
     *         BinaryExpression - if there is two concatenated strings and new line should be inserted after `+`
     *         None - if the string can't be split
     */
    @Suppress("UnsafeCallOnNullableType", "TOO_LONG_FUNCTION")
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
                return LongLineFixableCases.BinaryExpression(node.treeParent)
            }
            // can't fix this case
            return LongLineFixableCases.None
        }
        println("DEL INDEX ${delimiterIndex} ${node.text.substring(0, delimiterIndex)}")
        // check, that space to split is a part of text - not code
        // If the space split is part of the code, then there is a chance of breaking the code when fixing, that why we should ignore it
        val isSpaceIsWhiteSpace = node.psi.findElementAt(delimiterIndex)!!.node.isWhiteSpace()
        if (isSpaceIsWhiteSpace) {
            return LongLineFixableCases.None
        }
        // minus 2 here as we are inserting ` +` and we don't want it to exceed line length
        val shouldAddTwoSpaces = (multiLineOffset == 0) && (leftOffset + delimiterIndex > configuration.lineLength.toInt() - 2)
        val correcterDelimiter = if (shouldAddTwoSpaces) {
            node.text.substring(0, delimiterIndex - 2).lastIndexOf(' ')
        } else {
            delimiterIndex
        }
        if (correcterDelimiter == -1) {
            return LongLineFixableCases.None
        }
        return LongLineFixableCases.StringTemplate(node, correcterDelimiter, multiLineOffset == 0)
    }

    private fun checkFun(wrongNode: ASTNode) =
            if (wrongNode.hasChildOfType(EQ)) LongLineFixableCases.Fun(wrongNode) else LongLineFixableCases.None

    private fun checkComment(wrongNode: ASTNode, configuration: LineLengthConfiguration): LongLineFixableCases {
        val leftOffset = positionByOffset(wrongNode.startOffset).second
        val stringBeforeCommentContent = wrongNode.text.takeWhile { it == ' ' || it == '/' }
        if (stringBeforeCommentContent.length >= configuration.lineLength.toInt() - leftOffset) {
            return LongLineFixableCases.None
        }
        val indexLastSpace = wrongNode.text.substring(stringBeforeCommentContent.length, configuration.lineLength.toInt() - leftOffset).lastIndexOf(' ')
        val isNewLine = wrongNode.treePrev?.isWhiteSpaceWithNewline() ?: wrongNode.treeParent?.treePrev?.isWhiteSpaceWithNewline() ?: false
        if (isNewLine && indexLastSpace == -1) {
            return LongLineFixableCases.None
        }
        return LongLineFixableCases.Comment(wrongNode, isNewLine, indexLastSpace + stringBeforeCommentContent.length)
    }

    private fun checkCondition(wrongNode: ASTNode, configuration: LineLengthConfiguration): LongLineFixableCases {
        val leftOffset = positionByOffset(wrongNode.firstChildNode.startOffset).second
        val binList: MutableList<ASTNode> = mutableListOf()
        searchBinaryExpression(wrongNode, binList)
        if (binList.size == 1) {
            return LongLineFixableCases.None
        }
        return LongLineFixableCases.Condition(configuration.lineLength, leftOffset, binList)
    }

    @Suppress("UnsafeCallOnNullableType", "TOO_LONG_FUNCTION")
    private fun checkProperty(wrongNode: ASTNode, configuration: LineLengthConfiguration): LongLineFixableCases {
        println("Check property")
        var newParent = wrongNode
        while (newParent.hasChildOfType(PARENTHESIZED)) {
            newParent = wrongNode.findChildByType(PARENTHESIZED)!!
        }
        if (!newParent.hasChildOfType(STRING_TEMPLATE)) {
            if (newParent.hasChildOfType(BINARY_EXPRESSION)) {
                val leftOffset = positionByOffset(newParent.findChildByType(BINARY_EXPRESSION)!!.startOffset).second
                val binList: MutableList<ASTNode> = mutableListOf()
                println("WRONG NODE : ${wrongNode.text}")
                dfsForProperty(wrongNode, binList)
                print("BINARY EXPR: [")
                binList.forEach { print("${it.text} | ") }
                print("]\n")
                if (binList.size == 1) {
                    return LongLineFixableCases.None
                }
                return LongLineFixableCases.Condition(configuration.lineLength, leftOffset, binList)
            } else {
                return LongLineFixableCases.None
            }
        } else {
            val leftOffset = positionByOffset(newParent.findChildByType(STRING_TEMPLATE)!!.startOffset).second
            if (leftOffset > configuration.lineLength - STRING_PART_OFFSET) {
                return LongLineFixableCases.None
            }
            val text = wrongNode.findChildByType(STRING_TEMPLATE)!!.text.trim('"')
            val lastCharIndex = configuration.lineLength.toInt() - leftOffset - STRING_PART_OFFSET
            val indexLastSpace = (text.substring(0, lastCharIndex).lastIndexOf(' '))
            if (indexLastSpace == -1) {
                return LongLineFixableCases.None
            }
            return LongLineFixableCases.Property(wrongNode, indexLastSpace, text)
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
            is LongLineFixableCases.Fun -> fixableType.node.appendNewlineMergingWhiteSpace(null, fixableType.node.findChildByType(EQ)!!.treeNext)
            is LongLineFixableCases.Comment -> fixComment(fixableType)
            is LongLineFixableCases.Condition -> fixLongBinaryExpression(fixableType)
            is LongLineFixableCases.Property -> createSplitProperty(fixableType)
            is LongLineFixableCases.StringTemplate -> fixStringTemplate(fixableType)
            is LongLineFixableCases.BinaryExpression -> fixBinaryExpression(fixableType.node)
            is LongLineFixableCases.None -> return
        }
    }

    private fun fixComment(wrongComment: LongLineFixableCases.Comment) {
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
            wrongNode.treeParent.treeParent?.let {
                val parent = wrongNode.treeParent
                if (wrongNode.treePrev.isWhiteSpace()) {
                    parent.removeChild(wrongNode.treePrev)
                }
                parent.removeChild(wrongNode)
                it.addChild(wrongNode, parent)
                it.addChild(PsiWhiteSpaceImpl("\n"), parent)
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun fixBinaryExpression(node: ASTNode) {
        val whiteSpaceAfterPlus = node.findChildByType(OPERATION_REFERENCE)!!.treeNext
        node.replaceChild(whiteSpaceAfterPlus, PsiWhiteSpaceImpl("\n"))
    }

    @Suppress("UnsafeCallOnNullableType", "COMMENT_WHITE_SPACE")
    private fun fixStringTemplate(wrongStringTemplate: LongLineFixableCases.StringTemplate) {
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
     * we collect their if their length less than max.
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun fixLongBinaryExpression(wrongBinaryExpression: LongLineFixableCases.Condition) {
        val leftOffset = wrongBinaryExpression.leftOffset
        val binList = wrongBinaryExpression.binList
        var binaryText = ""
        binList.forEachIndexed { index, astNode ->
            binaryText += findAllText(astNode)
            println("binaryText [${leftOffset + binaryText.length } vs ${wrongBinaryExpression.maximumLineLength}] [${binaryText}]")
            if (leftOffset + binaryText.length > wrongBinaryExpression.maximumLineLength && index != 0) {
                println("---")
                val commonParent = astNode.parent({ println("${it.elementType} ${it.text} ast ${astNode.elementType}"); it.elementType == BINARY_EXPRESSION && it in binList[index - 1].parents() })!!
                println("\nASTNODE ${astNode.elementType} | ${astNode.text}")
                println("COMMON PARENT ${commonParent.elementType} | ${commonParent.text}")
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

    // Depth-first search. Collect all children to the right of the equal sign with specific type (propertyList), by which
    // we can split expression. Such logic needed because AST representation of complex conditions is quite strange
    private fun dfsForProperty(node: ASTNode, binList: MutableList<ASTNode>) {
        node.getChildren(null).forEach {
            //println("CHILD: ${it.elementType} | ${it.text}")
            if (it.elementType in propertyList) {
                val parentType = it.treeParent?.elementType
                if (it.elementType == REFERENCE_EXPRESSION &&
                    (parentType == CALL_EXPRESSION)
                            //|| parentType == DOT_QUALIFIED_EXPRESSION || parentType == SAFE_ACCESS_EXPRESSION)
                ) {
                    //println("PAR: ${it.treeParent.elementType} | ${it.treeParent.text}")
                    //if (it.treeParent?.treeParent?.elementType != DOT_QUALIFIED_EXPRESSION && it.treeParent?.treeParent?.elementType != SAFE_ACCESS_EXPRESSION) {
                        //println("PAR: ${it.treeParent.elementType} | ${it.treeParent.text}")
                        //println("GRAND PAR: ${it.treeParent.treeParent.elementType} | ${it.treeParent.treeParent.text}")
                        binList.tryAdd(it.treeParent)
                    //}
                } else {
                    //println("CHILD: ${it.elementType} | ${it.text}")
                    //binList.add(it)
                    binList.tryAdd(it)
                }
            }
            dfsForProperty(it, binList)
        }
    }

    private fun MutableList<ASTNode>.tryAdd(node: ASTNode) {
        //println(node.treeParent?.elementType)
        if (node !in this) {
            this.add(node)
        }
    }

    private fun createSplitProperty(wrongProperty: LongLineFixableCases.Property) {
        val node = wrongProperty.node
        val indexLastSpace = wrongProperty.indexLastSpace
        val text = wrongProperty.text
        splitTextAndCreateNode(node, text, indexLastSpace)
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun splitTextAndCreateNode(
        node: ASTNode,
        text: String,
        index: Int) {
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

    @Suppress("KDOC_NO_CONSTRUCTOR_PROPERTY", "MISSING_KDOC_CLASS_ELEMENTS")  // todo add proper docs
    sealed class LongLineFixableCases {
        object None : LongLineFixableCases()

        /**
         * @property node node
         * @property hasNewLineBefore flag to handle type of comment: ordinary comment (long part of which should be moved to the next line)
         * and inline comments (which should be moved entirely to the previous line)
         * @property indexLastSpace index of last space to substring comment
         */
        class Comment(
            val node: ASTNode,
            val hasNewLineBefore: Boolean,
            val indexLastSpace: Int = 0) : LongLineFixableCases()

        /**
         * @property node node
         * @property delimiterIndex index to split
         * @property isOneLineString flag is string is one line
         */
        class StringTemplate(
            val node: ASTNode,
            val delimiterIndex: Int,
            val isOneLineString: Boolean) : LongLineFixableCases()

        class BinaryExpression(val node: ASTNode) : LongLineFixableCases()

        class Condition(
            val maximumLineLength: Long,
            val leftOffset: Int,
            val binList: MutableList<ASTNode>) : LongLineFixableCases()

        class Fun(val node: ASTNode) : LongLineFixableCases()

        class Property(
            val node: ASTNode,
            val indexLastSpace: Int,
            val text: String) : LongLineFixableCases()
    }

    /**
     * val text = "first part" +
     * "second part" +
     * "third part"
     * STRING_PART_OFFSET equal to the left offset of first string part("first part") =
     * white space + close quote (open quote removed by trim) + white space + plus sign
     */
    companion object {
        private const val MAX_LENGTH = 120L
        private const val STRING_PART_OFFSET = 4
        private val propertyList = listOf(INTEGER_CONSTANT, LITERAL_STRING_TEMPLATE_ENTRY, FLOAT_CONSTANT,
            CHARACTER_CONSTANT, REFERENCE_EXPRESSION, BOOLEAN_CONSTANT, LONG_STRING_TEMPLATE_ENTRY,
            SHORT_STRING_TEMPLATE_ENTRY, NULL)
    }
}
