package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.CONST_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.SEMICOLON
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.SORT_RULE
import org.cqfn.diktat.ruleset.utils.*
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl

class SortRule : Rule("sort-rule") {

    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        configRules = params.getDiktatConfigRules()
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.isClassEnum() && node.hasChildOfType(CLASS_BODY))
            sortEnum(node.findChildByType(CLASS_BODY)!!)
        if (node.isCompanionObject() &&  node.hasChildOfType(CLASS_BODY))
            sortProperty(node.findChildByType(CLASS_BODY)!!)
    }

    private fun sortProperty(node: ASTNode) {
        val propertyList = node.getChildren(null)
                .filter { it.elementType == PROPERTY }
                .filter { it.hasChildOfType(MODIFIER_LIST) }
                .filter { it.findChildByType(MODIFIER_LIST)!!.hasChildOfType(CONST_KEYWORD) }
        if (propertyList.size <= 1)
            return
        val orderListOfList = createOrderListOfList(propertyList)
        val sortedListOfList = mutableListOf<MutableList<ASTNode>>()
        orderListOfList.forEach { nodesList ->
            val sortList = nodesList.toMutableList()
            sortList.sortBy { it.findChildByType(IDENTIFIER)!!.text }
            sortedListOfList.add(sortList)
        }
        if (orderListOfList != sortedListOfList) {
            SORT_RULE.warnAndFix(configRules, emitWarn, isFixMode,
                    "Constant properties inside companion object order is incorrect", node.startOffset) {
                sortedListOfList.forEachIndexed { index, mutableList ->
                    swapSortNodes(mutableList, orderListOfList[index], node)
                }
            }
        }
    }

    private fun swapSortNodes(sortList: List<ASTNode>, nonSortList: List<ASTNode>, node: ASTNode) {
        val nodeBefore: ASTNode? = nonSortList.last().treeNext
        node.removeRange(nonSortList.first(), nonSortList.last().treeNext)
        sortList.forEachIndexed { nodeIndex, astNode ->
            if (nodeIndex != 0)
                node.addChild(PsiWhiteSpaceImpl("\n"), nodeBefore)
            node.addChild(astNode, nodeBefore)
        }
    }

    private fun createOrderListOfList(propertyList: List<ASTNode>): MutableList<MutableList<ASTNode>> {
        val orderListOfList = mutableListOf<MutableList<ASTNode>>()
        var oneOrderList = mutableListOf(propertyList.first())
        propertyList.forEachIndexed { index, astNode ->
            if (index != propertyList.size - 1) {
                if (astNode.treeNext.treeNext == propertyList[index + 1]) {
                    oneOrderList.add(propertyList[index + 1])
                } else {
                    orderListOfList.add(oneOrderList)
                    oneOrderList = mutableListOf(propertyList[index + 1])
                }
            }
        }
        orderListOfList.add(oneOrderList)
        return orderListOfList
    }

    private fun sortEnum(node: ASTNode) {
        val enumEntryList = node.getChildren(null).filter {  it.elementType == ElementType.ENUM_ENTRY }
        if (enumEntryList.size <= 1)
            return
        val sortList = enumEntryList.sortedBy { it.findChildByType(IDENTIFIER)!!.text }
        if (enumEntryList != sortList) {
            SORT_RULE.warnAndFix(configRules, emitWarn, isFixMode, "Enums order is incorrect", node.startOffset) {
                removeLastSemicolonAndSpace(enumEntryList.last())
                swapSortNodes(sortList, enumEntryList, node)
                sortList.last().addChild(PsiWhiteSpaceImpl("\n"), null)
                sortList.last().addChild(LeafPsiElement(SEMICOLON, ";"), null)
            }
        }
    }

    private fun removeLastSemicolonAndSpace(node: ASTNode) {
        node.removeChild(node.findChildByType(SEMICOLON)!!)
        node.removeChild(node.lastChildNode)
    }
}
