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
import com.pinterest.ktlint.core.ast.ElementType.OPEN_QUOTE
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.PARENTHESIZED
import com.pinterest.ktlint.core.ast.ElementType.PLUS
import com.pinterest.ktlint.core.ast.ElementType.PREFIX_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.REGULAR_STRING_PART
import com.pinterest.ktlint.core.ast.ElementType.STRING_TEMPLATE
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.LONG_LINE
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.cqfn.diktat.ruleset.utils.prettyPrint
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import java.net.MalformedURLException
import java.net.URL

@Suppress("ForbiddenComment")
class LineLength : Rule("line-length") {

    /**
     * LEFT_OFFSET equal to the left offset
     * if ( x > 6 ||
     *      y > 5) the distance between y and left edge equal 13
     *
     * STRING_SPACE needed to split string. This value is open and close quotes, white space and
     * plus sign text length
     */
    companion object {
        private const val MAX_LENGTH = 120L
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
                val newNode = node.psi.findElementAt(offset + it.trim().length - 1)!!.node
                if ((newNode.elementType != KDOC_TEXT && newNode.elementType != KDOC_MARKDOWN_INLINE_LINK) ||
                        !isKDocValid(newNode)) {
                    LONG_LINE.warnAndFix(configRules, emitWarn, isFixMode,
                            "max line length ${configuration.lineLength}, but was ${it.length}",
                            offset + node.startOffset) {
                        if (!this::positionByOffset.isInitialized)
                            positionByOffset =calculateLineColByOffset(node.treeParent.text)
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
        if (wrongNode.elementType == EOL_COMMENT) {
            fixComment(wrongNode, configuration)
            return
        }
        var parent = wrongNode
        do {
            when (parent.elementType) {
                FUN -> {
                    if (!parent.hasChildOfType(BLOCK))
                        parent.addChild(PsiWhiteSpaceImpl("\n"), parent.findChildByType(EQ)!!.treeNext)
                    return
                }
                CONDITION -> {
                    fixCondition(parent, configuration)
                    return
                }
                PROPERTY -> {
                    fixProperty(parent, configuration)
                    return
                }
                else -> parent = parent.treeParent
            }
        } while (parent.treeParent != null)
    }

    private fun fixComment(wrongNode: ASTNode, configuration: LineLengthConfiguration){
        var node = wrongNode
        do {
            val commentText = node.text
            var indexLastSpace = configuration.lineLength.toInt()
            while (commentText[indexLastSpace] != ' ') {
                --indexLastSpace
                if (indexLastSpace == 0)
                    return
            }
            val nodeText = "//${commentText.substring(indexLastSpace, commentText.length)}"
            val newNode = LeafPsiElement(EOL_COMMENT, nodeText)
            node.treeParent.run {
                addChild(LeafPsiElement(EOL_COMMENT, commentText.substring(0, indexLastSpace)), node)
                addChild(PsiWhiteSpaceImpl("\n"), node)
                addChild(newNode, node)
                removeChild(node)
            }
            node = newNode
        } while (node.text.length > configuration.lineLength)
    }

    private fun fixCondition(wrongNode: ASTNode, configuration: LineLengthConfiguration,
                             leftInitOffset: Int = -1, isProperty: Boolean = false) {
        var leftOffset = if (leftInitOffset < 0){
            positionByOffset(wrongNode.firstChildNode.psi.startOffset).second
        } else leftInitOffset
        val binList = mutableListOf<ASTNode>()
        if (isProperty)
            dfsForProperty(wrongNode, binList)
        else
            searchBinaryExpression(wrongNode, binList)
        lateinit var binaryText: String
        binList.forEachIndexed { index, astNode ->
            if (index == 0){
                binaryText = astNode.text
            } else {
                val operationRef = astNode.treeParent.findChildByType(OPERATION_REFERENCE)!!
                if (operationRef.treePrev.elementType == WHITE_SPACE)
                    binaryText += operationRef.treePrev.text
                binaryText += operationRef.text
                if (operationRef.treeNext.elementType == WHITE_SPACE)
                    binaryText += operationRef.treeNext.text
                binaryText+= astNode.text
                if (leftOffset + binaryText.length > configuration.lineLength) {
                    val commonParent = findCommonParent(astNode, binList[index-1])
                    val newLine = PsiWhiteSpaceImpl("\n")
                    commonParent.addChild(newLine, commonParent.findChildByType(OPERATION_REFERENCE)!!.treeNext)
                    leftOffset = 0
                    binaryText = astNode.text
                }
            }
        }
    }

    private fun findCommonParent(firstNode: ASTNode ,secondNode: ASTNode): ASTNode{
        var firstParent: ASTNode? = firstNode
        while (firstParent!= null) {
            if (depthFirstSearch(firstParent, secondNode)!= null)
                return firstParent
            firstParent = firstParent.treeParent
        }
        return firstParent ?: firstNode
    }

    private fun depthFirstSearch(node: ASTNode, finder: ASTNode): ASTNode?{
        if (node.getChildren(null).find { it == finder } != null)
            return node
        node.getChildren(null).forEach {
            val answer = depthFirstSearch(it, finder)
            if (answer != null) return answer
        }
        return null
    }

    /**
     * This method stored all the nodes that have BINARY_EXPRESSION element type.
     * This method uses recursion to store binary node in the order in which they are located
     * Also binList contains nodes with PREFIX_EXPRESSION element type ( !isFoo(), !isValid)
     *@param node node in which to search
     *@param binList mutable list of ASTNode to store nodes
     */
    private fun searchBinaryExpression(node: ASTNode, binList: MutableList<ASTNode>) {
        when {
            node.hasChildOfType(BINARY_EXPRESSION) -> {
                node.getAllChildrenWithType(BINARY_EXPRESSION).forEach {
                    searchBinaryExpression(it, binList)
                }
            }
            node.hasChildOfType(PARENTHESIZED) -> {
                node.getAllChildrenWithType(PARENTHESIZED).forEach {
                    searchBinaryExpression(it, binList)
                }
            }
            else -> {
                binList.add(node)
                binList.add(node.treeParent.findChildByType(PREFIX_EXPRESSION) ?: return)
            }
        }
    }

    private fun dfsForProperty(node: ASTNode, binList: MutableList<ASTNode>) {
        node.getChildren(null).forEach {
            if (it.elementType in PROPERTY_LIST)
                binList.add(it)
            dfsForProperty(it, binList)
        }
    }

    private fun fixProperty(parent: ASTNode, configuration: LineLengthConfiguration){
        println(parent.prettyPrint())
        if (!parent.hasChildOfType(STRING_TEMPLATE)) {
            if (parent.hasChildOfType(BINARY_EXPRESSION)) {
                val leftOffset = positionByOffset(parent.findChildByType(BINARY_EXPRESSION)!!.psi.textOffset).second
                fixCondition(parent, configuration, leftOffset, true)
            }
            if (parent.hasChildOfType(PARENTHESIZED)){
                val newParent = parent.findChildByType(PARENTHESIZED)!!
                val leftOffset = positionByOffset(newParent.findChildByType(BINARY_EXPRESSION)!!.psi.textOffset).second
                fixCondition(newParent, configuration, leftOffset)
            }
        }
        else
            createNodes(parent, configuration)
    }

    private fun createNodes(parent: ASTNode, configuration: LineLengthConfiguration) {
        var text = parent.findChildByType(STRING_TEMPLATE)!!.text
        text = text.trimStart(text.first()).trimEnd(text.last())
        val col = positionByOffset(parent.findChildByType(STRING_TEMPLATE)!!.psi.textOffset).second
        parent.removeChild(parent.findChildByType(STRING_TEMPLATE)!!)
        val correctTextList = mutableListOf<String>()
        val startOffset = 4
        val regularOffset = 5
        correctTextList.add(text.substring(0, configuration.lineLength.toInt() - col - startOffset))
        text = text.substring(configuration.lineLength.toInt() - col - startOffset)
        correctTextList.addAll(text.chunked(configuration.lineLength.toInt() - regularOffset))
        var prevExp = CompositeElement(BINARY_EXPRESSION)
        parent.addChild(prevExp, null)
        correctTextList.reversed().forEachIndexed { index, textBinExpress ->
            if (index == correctTextList.size - 1)
                return@forEachIndexed
            createNodesBetweenStringTemplates(prevExp)
            createStringTemplate(textBinExpress, prevExp)
            val newExp = CompositeElement(BINARY_EXPRESSION)
            prevExp.addChild(newExp, prevExp.firstChildNode)
            prevExp = newExp
        }
        createStringTemplate(correctTextList.first(), prevExp)
    }

    private fun createStringTemplate(text: String, prevExp: CompositeElement) {
        val litString = CompositeElement(LITERAL_STRING_TEMPLATE_ENTRY)
        val stringTemplate = CompositeElement(STRING_TEMPLATE)
        val closeQuote = LeafPsiElement(CLOSING_QUOTE, "\"")
        prevExp.addChild(litString)
        litString.addChild(LeafPsiElement(REGULAR_STRING_PART, text))
        prevExp.addChild(stringTemplate)
        stringTemplate.addChild(closeQuote)
        stringTemplate.addChild(litString, closeQuote)
        stringTemplate.addChild(LeafPsiElement(OPEN_QUOTE, "\""), litString)
    }

    private fun createNodesBetweenStringTemplates(prevExp: CompositeElement) {
        prevExp.addChild(PsiWhiteSpaceImpl(" "))
        val plusOperator = CompositeElement(OPERATION_REFERENCE)
        prevExp.addChild(plusOperator)
        plusOperator.addChild(LeafPsiElement(PLUS, "+"))
        prevExp.addChild(PsiWhiteSpaceImpl("\n"))
    }

    class LineLengthConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val lineLength = config["lineLength"]?.toLongOrNull() ?: MAX_LENGTH
    }
}
