package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.KtLint.calculateLineColByOffset
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.BOOLEAN_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.CHARACTER_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.CLOSING_QUOTE
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
import com.pinterest.ktlint.core.ast.ElementType.LPAR
import com.pinterest.ktlint.core.ast.ElementType.OPEN_QUOTE
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.PARENTHESIZED
import com.pinterest.ktlint.core.ast.ElementType.PLUS
import com.pinterest.ktlint.core.ast.ElementType.POSTFIX_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.PREFIX_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.REGULAR_STRING_PART
import com.pinterest.ktlint.core.ast.ElementType.RPAR
import com.pinterest.ktlint.core.ast.ElementType.STRING_TEMPLATE
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.nextSibling
import com.pinterest.ktlint.core.ast.parent
import com.pinterest.ktlint.core.ast.prevSibling
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.LONG_LINE
import org.cqfn.diktat.ruleset.utils.appendNewlineMergingWhiteSpace
import org.cqfn.diktat.ruleset.utils.createOperationReference
import org.cqfn.diktat.ruleset.utils.hasChildOfType
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
class LineLength : Rule("line-length") {

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
        private val PROPERTY_LIST = listOf(INTEGER_CONSTANT, STRING_TEMPLATE, FLOAT_CONSTANT,
                CHARACTER_CONSTANT, REFERENCE_EXPRESSION, BOOLEAN_CONSTANT)
    }

    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var fileName: String? = null
    private var isFixMode: Boolean = false
    private lateinit var positionByOffset: (Int) -> Pair<Int, Int>
    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        configRules = params.getDiktatConfigRules()
        fileName = params.fileName
        emitWarn = emit
        isFixMode = autoCorrect

        val configuration = LineLengthConfiguration(
                configRules.getRuleConfig(LONG_LINE)?.configuration ?: mapOf())


        if (node.elementType == FILE) {
            node.getChildren(null).forEach {
                if (it.elementType != PACKAGE_DIRECTIVE || it.elementType != IMPORT_LIST)
                    checkLength(it, configuration)
            }
        }
    }

    private fun checkLength(node: ASTNode, configuration: LineLengthConfiguration) {
        var offset = 0
        node.text.lines().forEach {
            if (it.length > configuration.lineLength) {
                val newNode = node.psi.findElementAt(offset + configuration.lineLength.toInt())!!.node
                if ((newNode.elementType != KDOC_TEXT && newNode.elementType != KDOC_MARKDOWN_INLINE_LINK) ||
                        !isKDocValid(newNode)) {
                    LONG_LINE.warnAndFix(configRules, emitWarn, isFixMode,
                            "max line length ${configuration.lineLength}, but was ${it.length}",
                            offset + node.startOffset) {
                        positionByOffset = calculateLineColByOffset(node.treeParent.text)
                        fixError(newNode, configuration)
                    }
                }
            }
            offset += it.length + 1
        }
    }

    // fixme json method
    private fun isKDocValid(node: ASTNode): Boolean {
        return try {
            if (node.elementType == KDOC_TEXT)
                URL(node.text.split("\\s".toRegex()).last { it.isNotEmpty() })
            else
                URL(node.text.substring(node.text.indexOfFirst { it == ']' } + 2, node.textLength - 1))
            true
        } catch (e: MalformedURLException) {
            false
        }
    }

    private fun fixError(wrongNode: ASTNode, configuration: LineLengthConfiguration) {
        var parent = wrongNode
        do {
            when (parent.elementType) {
                FUN -> {
                    if (!parent.hasChildOfType(BLOCK))
                        parent.appendNewlineMergingWhiteSpace(null, parent.findChildByType(EQ)!!.treeNext)
                    return
                }
                CONDITION -> {
                    fixLongBinaryExpression(parent, configuration)
                    return
                }
                PROPERTY -> {
                    fixProperty(parent, configuration)
                    return
                }
                EOL_COMMENT -> {
                    fixComment(wrongNode, configuration)
                    return
                }
                else -> parent = parent.treeParent
            }
        } while (parent.treeParent != null)
    }

    private fun fixComment(wrongNode: ASTNode, configuration: LineLengthConfiguration) {
        val leftOffset = positionByOffset(wrongNode.startOffset).second
        val indexLastSpace = wrongNode.text.substring(0, configuration.lineLength.toInt() - leftOffset).lastIndexOf(' ')
        if (indexLastSpace == -1)
            return
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
     * @param wrongNode node that should be split
     * @param configuration max length configuration
     * @param leftInitOffset is a left offset, it need when we split binary expression in right value case
     * @param isProperty if wrongNode is a property and it in brackets
     *
     * In this method we collect all binary expression in correct order and then
     * we collect their if their length less then max.
     */
    private fun fixLongBinaryExpression(wrongNode: ASTNode, configuration: LineLengthConfiguration,
                                        leftInitOffset: Int = -1, isProperty: Boolean = false) {
        val leftOffset = if (leftInitOffset < 0) {
            positionByOffset(wrongNode.firstChildNode.startOffset).second
        } else leftInitOffset
        val binList = mutableListOf<ASTNode>()
        if (isProperty)
            dfsForProperty(wrongNode, binList)
        else
            searchBinaryExpression(wrongNode, binList)
        if (binList.size == 1)
            return
        var binaryText = ""
        binList.forEachIndexed { index, astNode ->
            binaryText += findAllText(astNode)
            if (leftOffset + binaryText.length > configuration.lineLength && index != 0) {
                val commonParent = astNode.parent({ it in binList[index - 1].parents() })!!
                val nextNode = commonParent.findChildByType(OPERATION_REFERENCE)!!.treeNext
                if (!nextNode.text.contains("\n"))
                    commonParent.appendNewlineMergingWhiteSpace(nextNode, nextNode)
                return
            }
        }
    }

    private fun findAllText(astNode: ASTNode): String {
        var text = ""
        var node = astNode
        var prevNode: ASTNode
        do {
            prevNode = node
            node = node.treeParent
            if (node.elementType == PARENTHESIZED)
                text += getTextFromParenthesized(node)
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
        if (node.treePrev.elementType == WHITE_SPACE)
            text += node.treePrev.text
        text += node.text
        return text
    }

    private fun getBraceAndBeforeText(node: ASTNode, prevNode: ASTNode): String {
        var text = ""
        val par = if (prevNode.prevSibling { it.elementType == OPERATION_REFERENCE } == null) LPAR else RPAR
        if (node.findChildByType(par)!!.treePrev != null && node.findChildByType(par)!!.treePrev.elementType == WHITE_SPACE)
            text += node.findChildByType(par)!!.treePrev.text
        text += node.findChildByType(par)!!.text
        return text
    }

    private fun getTextFromParenthesized(node: ASTNode): String {
        var text = ""
        text += node.findChildByType(LPAR)!!.text
        if (node.findChildByType(LPAR)!!.treeNext.elementType == WHITE_SPACE)
            text += node.findChildByType(LPAR)!!.treeNext.text
        if (node.findChildByType(RPAR)!!.treePrev.elementType == WHITE_SPACE)
            text += node.findChildByType(RPAR)!!.treePrev.text
        text += node.findChildByType(RPAR)!!.text
        return text
    }

    /**
     * This method stored all the nodes that have BINARY_EXPRESSION or PREFIX_EXPRESSION element type.
     * This method uses recursion to store binary node in the order in which they are located
     * Also binList contains nodes with PREFIX_EXPRESSION element type ( !isFoo(), !isValid)
     *@param node node in which to search
     *@param binList mutable list of ASTNode to store nodes
     */
    private fun searchBinaryExpression(node: ASTNode, binList: MutableList<ASTNode>) {
        if (node.hasChildOfType(BINARY_EXPRESSION) || node.hasChildOfType(PARENTHESIZED) || node.hasChildOfType(POSTFIX_EXPRESSION)) {
            node.getChildren(null)
                    .filter { it.elementType == BINARY_EXPRESSION || it.elementType == PARENTHESIZED || it.elementType == POSTFIX_EXPRESSION }
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
            if (it.elementType in PROPERTY_LIST)
                binList.add(it)
            dfsForProperty(it, binList)
        }
    }

    private fun fixProperty(parent: ASTNode, configuration: LineLengthConfiguration) {
        if (!parent.hasChildOfType(STRING_TEMPLATE)) {
            val newParent = parent.findChildByType(PARENTHESIZED) ?: parent
            if (newParent.hasChildOfType(BINARY_EXPRESSION)) {
                val leftOffset = positionByOffset(newParent.findChildByType(BINARY_EXPRESSION)!!.startOffset).second
                fixLongBinaryExpression(newParent, configuration, leftOffset, true)
            }
        } else {
            createNodes(parent, configuration)
        }
    }

    private fun createNodes(node: ASTNode, configuration: LineLengthConfiguration) {
        val leftOffset = positionByOffset(node.findChildByType(STRING_TEMPLATE)!!.startOffset).second
        if (leftOffset > configuration.lineLength)
            return
        val text = node.findChildByType(STRING_TEMPLATE)!!.text.trim('"')
        val lastCharIndex = configuration.lineLength.toInt() - leftOffset - STRING_PART_OFFSET
        val indexLastSpace = (text.substring(0, lastCharIndex).lastIndexOf(' '))
        if (indexLastSpace == -1)
            return
        node.removeChild(node.findChildByType(STRING_TEMPLATE)!!)
        val prevExp = CompositeElement(BINARY_EXPRESSION)
        node.addChild(prevExp, null)
        createStringTemplate(text.substring(0, indexLastSpace), prevExp)
        createNodesBetweenStringTemplates(prevExp)
        createStringTemplate(text.substring(indexLastSpace), prevExp)
    }

    private fun createStringTemplate(text: String, prevExp: CompositeElement) {
        val litString = CompositeElement(LITERAL_STRING_TEMPLATE_ENTRY)
        val stringTemplate = CompositeElement(STRING_TEMPLATE)
        val closeQuote = LeafPsiElement(CLOSING_QUOTE, "\"")
        prevExp.addChild(stringTemplate)
        stringTemplate.addChild(closeQuote)
        stringTemplate.addChild(litString, closeQuote)
        litString.addChild(LeafPsiElement(REGULAR_STRING_PART, text))
        stringTemplate.addChild(LeafPsiElement(OPEN_QUOTE, "\""), litString)
    }

    private fun createNodesBetweenStringTemplates(prevExp: CompositeElement) {
        prevExp.addChild(PsiWhiteSpaceImpl(" "))
        prevExp.createOperationReference(PLUS, "+")
        prevExp.addChild(PsiWhiteSpaceImpl("\n"))
    }

    class LineLengthConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val lineLength = config["lineLength"]?.toLongOrNull() ?: MAX_LENGTH
    }
}
