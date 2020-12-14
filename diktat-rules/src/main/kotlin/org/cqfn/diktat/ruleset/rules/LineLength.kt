package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.LONG_LINE
import org.cqfn.diktat.ruleset.utils.KotlinParser
import org.cqfn.diktat.ruleset.utils.appendNewlineMergingWhiteSpace
import org.cqfn.diktat.ruleset.utils.calculateLineColByOffset
import org.cqfn.diktat.ruleset.utils.hasChildOfType

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.BOOLEAN_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.CHARACTER_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.CONDITION
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.EQ
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.FLOAT_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_LIST
import com.pinterest.ktlint.core.ast.ElementType.INTEGER_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.KDOC_MARKDOWN_INLINE_LINK
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TEXT
import com.pinterest.ktlint.core.ast.ElementType.LITERAL_STRING_TEMPLATE_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.LONG_STRING_TEMPLATE_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.LPAR
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.PARENTHESIZED
import com.pinterest.ktlint.core.ast.ElementType.POSTFIX_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.PREFIX_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.RPAR
import com.pinterest.ktlint.core.ast.ElementType.SHORT_STRING_TEMPLATE_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.STRING_TEMPLATE
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
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
class LineLength(private val configRules: List<RulesConfig>) : Rule("line-length") {
    private var isFixMode: Boolean = false
    private lateinit var positionByOffset: (Int) -> Pair<Int, Int>
    private lateinit var emitWarn: EmitType

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

        val configuration = LineLengthConfiguration(
            configRules.getRuleConfig(LONG_LINE)?.configuration ?: mapOf())

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
        node.text.lines().forEach {
            if (it.length > configuration.lineLength) {
                val newNode = node.psi.findElementAt(offset + configuration.lineLength.toInt())!!.node
                if ((newNode.elementType != KDOC_TEXT && newNode.elementType != KDOC_MARKDOWN_INLINE_LINK) ||
                        !isKdocValid(newNode)) {
                    positionByOffset = node.treeParent.calculateLineColByOffset()
                    val fixableType = isFixable(newNode, configuration)
                    LONG_LINE.warnAndFix(configRules, emitWarn, isFixMode,
                        "max line length ${configuration.lineLength}, but was ${it.length}",
                        offset + node.startOffset, node, fixableType != LongLineFixableCases.None) {
                        fixError(fixableType)
                    }
                }
            }
            offset += it.length + 1
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
                else -> parent = parent.treeParent
            }
        } while (parent.treeParent != null)
        return LongLineFixableCases.None
    }

    private fun checkFun(wrongNode: ASTNode) =
            if (!wrongNode.hasChildOfType(BLOCK)) LongLineFixableCases.Fun(wrongNode) else LongLineFixableCases.None

    private fun checkComment(wrongNode: ASTNode, configuration: LineLengthConfiguration): LongLineFixableCases {
        val leftOffset = positionByOffset(wrongNode.startOffset).second
        val indexLastSpace = wrongNode.text.substring(0, configuration.lineLength.toInt() - leftOffset).lastIndexOf(' ')
        if (indexLastSpace == -1) {
            return LongLineFixableCases.None
        }
        return LongLineFixableCases.Comment(wrongNode, indexLastSpace)
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
                    return LongLineFixableCases.None
                }
                return LongLineFixableCases.Condition(configuration.lineLength, leftOffset, binList)
            } else {
                return LongLineFixableCases.None
            }
        } else {
            val leftOffset = positionByOffset(newParent.findChildByType(STRING_TEMPLATE)!!.startOffset).second
            if (newParent.findChildByType(STRING_TEMPLATE)!!.hasChildOfType(LONG_STRING_TEMPLATE_ENTRY)) {
                val binList = wrongNode.findChildByType(STRING_TEMPLATE)!!.getChildren(null).filter { it.elementType in propertyList }
                if (binList.size == 1) {
                    return LongLineFixableCases.None
                }
                return LongLineFixableCases.PropertyWithTemplateEntry(wrongNode, configuration.lineLength, leftOffset, binList.toMutableList())
            } else {
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
            is LongLineFixableCases.PropertyWithTemplateEntry -> fixLongString(fixableType)
            is LongLineFixableCases.None -> return
        }
    }

    private fun fixComment(wrongComment: LongLineFixableCases.Comment) {
        val wrongNode = wrongComment.node
        val indexLastSpace = wrongComment.indexLastSpace
        val nodeText = "//${wrongNode.text.substring(indexLastSpace, wrongNode.text.length)}"
        wrongNode.treeParent.run {
            addChild(LeafPsiElement(EOL_COMMENT, wrongNode.text.substring(0, indexLastSpace)), wrongNode)
            addChild(PsiWhiteSpaceImpl("\n"), wrongNode)
            addChild(LeafPsiElement(EOL_COMMENT, nodeText), wrongNode)
            removeChild(wrongNode)
        }
    }

    /**
     * This method fix too long binary expression: split after OPERATION_REFERENCE closest to max length
     *
     * In this method we collect all binary expression in correct order and then
     * we collect their if their length less then max.
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun fixLongBinaryExpression(wrongBinaryExpression: LongLineFixableCases.Condition) {
        val leftOffset = wrongBinaryExpression.leftOffset
        val binList = wrongBinaryExpression.binList
        var binaryText = ""
        binList.forEachIndexed { index, astNode ->
            binaryText += findAllText(astNode)
            if (leftOffset + binaryText.length > wrongBinaryExpression.maximumLineLength && index != 0) {
                val commonParent = astNode.parent({ it in binList[index - 1].parents() })!!
                val nextNode = commonParent.findChildByType(OPERATION_REFERENCE)!!.treeNext
                if (!nextNode.text.contains("\n")) {
                    commonParent.appendNewlineMergingWhiteSpace(nextNode, nextNode)
                }
                return
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType", "LOCAL_VARIABLE_EARLY_DECLARATION")
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

    @Suppress("UnsafeCallOnNullableType", "LOCAL_VARIABLE_EARLY_DECLARATION")
    private fun getBraceAndBeforeText(node: ASTNode, prevNode: ASTNode): String {
        var text = ""
        val par = prevNode.prevSibling { it.elementType == OPERATION_REFERENCE }?.let { LPAR } ?: RPAR
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

    private fun dfsForProperty(node: ASTNode, binList: MutableList<ASTNode>) {
        node.getChildren(null).forEach {
            if (it.elementType in propertyList) {
                if (it.elementType == REFERENCE_EXPRESSION && it.treeParent.elementType == CALL_EXPRESSION) {
                    binList.add(it.treeParent)
                } else {
                    binList.add(it)
                }
            }
            dfsForProperty(it, binList)
        }
    }

    private fun fixLongString(wrongProperty: LongLineFixableCases.PropertyWithTemplateEntry) {
        val wrongNode = wrongProperty.node
        val leftOffset = wrongProperty.leftOffset
        val lineLength = wrongProperty.maximumLineLength
        val binList = wrongProperty.binList
        val allText = binList.joinToString("") { it.text }
        var binaryText = ""
        binList.forEachIndexed { index, astNode ->
            binaryText += astNode.text
            if (STRING_PART_OFFSET + leftOffset + binaryText.length > lineLength) {
                if (astNode.elementType == LITERAL_STRING_TEMPLATE_ENTRY) {
                    val lastCharIndex = lineLength.toInt() - leftOffset - STRING_PART_OFFSET
                    val indexLastSpace = (allText.substring(0, lastCharIndex).lastIndexOf(' '))
                    if (indexLastSpace == -1) {
                        return
                    }
                    splitTextAndCreateNode(wrongNode, allText, indexLastSpace)
                    return
                } else if (index == 0) {
                    return
                }
                splitTextAndCreateNode(wrongNode, allText, binaryText.length)
                return
            }
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

        class Comment(val node: ASTNode, val indexLastSpace: Int) : LongLineFixableCases()

        class Condition(
            val maximumLineLength: Long,
            val leftOffset: Int,
            val binList: MutableList<ASTNode>) : LongLineFixableCases()

        class Fun(val node: ASTNode) : LongLineFixableCases()

        class Property(
            val node: ASTNode,
            val indexLastSpace: Int,
            val text: String) : LongLineFixableCases()

        class PropertyWithTemplateEntry(
            val node: ASTNode,
            val maximumLineLength: Long,
            val leftOffset: Int,
            val binList: MutableList<ASTNode>) : LongLineFixableCases()
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
            SHORT_STRING_TEMPLATE_ENTRY)
    }
}
