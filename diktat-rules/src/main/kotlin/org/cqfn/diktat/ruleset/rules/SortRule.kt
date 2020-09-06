package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.COMMA
import com.pinterest.ktlint.core.ast.ElementType.CONST_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.SEMICOLON
import com.pinterest.ktlint.core.ast.isWhiteSpaceWithNewline
import com.pinterest.ktlint.core.ast.nextSibling
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_DECLARATIONS_ORDER
import org.cqfn.diktat.ruleset.utils.*
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.psi.KtObjectDeclaration

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

        val configuration = SortRuleConfiguration(
                configRules.getRuleConfig(WRONG_DECLARATIONS_ORDER)?.configuration ?: mapOf()
        )

        if (node.isClassEnum() && node.hasChildOfType(CLASS_BODY) && configuration.sortEnum)
            sortEnum(node.findChildByType(CLASS_BODY)!!)
        if (((node.psi as? KtObjectDeclaration)?.isCompanion() == true) && node.hasChildOfType(CLASS_BODY) &&
                configuration.sortProperty)
            sortProperty(node.findChildByType(CLASS_BODY)!!)
    }

    private fun sortProperty(node: ASTNode) {
        val propertyList = node.getChildren(null)
                .filter { it.elementType == PROPERTY }
                .filter { it.hasChildOfType(MODIFIER_LIST) }
                .filter { it.findChildByType(MODIFIER_LIST)!!.hasChildOfType(CONST_KEYWORD) }
        if (propertyList.size <= 1)
            return
        val consecutivePropertiesGroups = createOrderListOfList(propertyList)
        val sortedListOfList = consecutivePropertiesGroups.map { group ->
            group.sortedBy { it.findChildByType(IDENTIFIER)!!.text }
        }
        consecutivePropertiesGroups.forEachIndexed { index, mutableList ->
            if (mutableList != sortedListOfList[index]) {
                WRONG_DECLARATIONS_ORDER.warnAndFix(configRules, emitWarn, isFixMode,
                        "constant properties inside companion object order is incorrect",
                        mutableList.first().startOffset) {
                    swapSortNodes(sortedListOfList[index], mutableList, node)
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

    private fun createOrderListOfList(propertyList: List<ASTNode>): List<List<ASTNode>> {
        val orderListOfList = mutableListOf<MutableList<ASTNode>>()
        var oneOrderList = mutableListOf(propertyList.first())
        propertyList.zipWithNext().forEach { (currentProperty, nextProperty) ->
            if (currentProperty.nextSibling { it.elementType == PROPERTY } == nextProperty)
                oneOrderList.add(nextProperty)
            else {
                orderListOfList.add(oneOrderList)
                oneOrderList = mutableListOf(nextProperty)
            }
        }
        orderListOfList.add(oneOrderList)
        return orderListOfList.toList()
    }

    private fun sortEnum(node: ASTNode) {
        val enumEntryList = node.getChildren(null).filter { it.elementType == ElementType.ENUM_ENTRY }
        if (enumEntryList.size <= 1)
            return
        val sortList = enumEntryList.sortedBy { it.findChildByType(IDENTIFIER)!!.text }
        if (enumEntryList != sortList) {
            WRONG_DECLARATIONS_ORDER.warnAndFix(configRules, emitWarn, isFixMode, "enum entries order is incorrect", node.startOffset) {
                val (isEndSemicolon, isEndSpace) = removeLastSemicolonAndSpace(enumEntryList.last())
                val hasTrailingComma  = (sortList.last() != enumEntryList.last() && enumEntryList.last().hasChildOfType(COMMA))
                swapSortNodes(sortList, enumEntryList, node)
                if (!hasTrailingComma ) {
                    enumEntryList.last().addChild(LeafPsiElement(COMMA, ","), null)
                    sortList.last().removeChild(sortList.last().findChildByType(COMMA)!!)
                }
                if (isEndSpace) sortList.last().addChild(PsiWhiteSpaceImpl("\n"), null)
                if (isEndSemicolon) sortList.last().addChild(LeafPsiElement(SEMICOLON, ";"), null)
            }
        }
    }

    private fun removeLastSemicolonAndSpace(node: ASTNode): Pair<Boolean, Boolean> {
        val isSemicolon = node.hasChildOfType(SEMICOLON)
        if (isSemicolon)
            node.removeChild(node.findChildByType(SEMICOLON)!!)
        val isSpace = node.lastChildNode.isWhiteSpaceWithNewline()
        if (isSpace)
            node.removeChild(node.lastChildNode)
        return Pair(isSemicolon, isSpace)
    }

    class SortRuleConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val sortEnum = config["sortEnum"]?.toBoolean() ?: false
        val sortProperty = config["sortProperty"]?.toBoolean() ?: false
    }
}
