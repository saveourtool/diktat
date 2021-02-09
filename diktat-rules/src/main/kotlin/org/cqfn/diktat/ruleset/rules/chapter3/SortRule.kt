package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_DECLARATIONS_ORDER
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.cqfn.diktat.ruleset.utils.isClassEnum

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
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.psi.KtObjectDeclaration

/**
 * Rule that sorts class properties and enum members alphabetically
 */
class SortRule(configRules: List<RulesConfig>) : DiktatRule("sort-rule",
                                                            configRules,
                                                            listOf(WRONG_DECLARATIONS_ORDER)) {
    override fun logic(node: ASTNode) {
        val configuration = SortRuleConfiguration(
            configRules.getRuleConfig(WRONG_DECLARATIONS_ORDER)?.configuration ?: emptyMap()
        )

        val classBody = node.findChildByType(CLASS_BODY)
        if (node.isClassEnum() && classBody != null && configuration.sortEnum) {
            sortEnum(classBody)
        }
        if ((node.psi as? KtObjectDeclaration)?.isCompanion() == true && classBody != null &&
                configuration.sortProperty) {
            sortProperty(classBody)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun sortProperty(node: ASTNode) {
        val propertyList = node
            .getChildren(null)
            .filter { it.elementType == PROPERTY }
            .filter { it.hasChildOfType(MODIFIER_LIST) }
            .filter { it.findChildByType(MODIFIER_LIST)!!.hasChildOfType(CONST_KEYWORD) }
        if (propertyList.size <= 1) {
            return
        }
        val consecutivePropertiesGroups = createOrderListOfList(propertyList)
        val sortedListOfList = consecutivePropertiesGroups.map { group ->
            group.sortedBy { it.findChildByType(IDENTIFIER)!!.text }
        }
        consecutivePropertiesGroups.forEachIndexed { index, mutableList ->
            if (mutableList != sortedListOfList[index]) {
                WRONG_DECLARATIONS_ORDER.warnAndFix(configRules, emitWarn, isFixMode,
                    "constant properties inside companion object order is incorrect",
                    mutableList.first().startOffset, mutableList.first()) {
                    swapSortNodes(sortedListOfList[index], mutableList, node)
                }
            }
        }
    }

    private fun swapSortNodes(
        sortList: List<ASTNode>,
        nonSortList: List<ASTNode>,
        node: ASTNode) {
        val nodeBefore: ASTNode? = nonSortList.last().treeNext
        node.removeRange(nonSortList.first(), nonSortList.last().treeNext)
        sortList.forEachIndexed { nodeIndex, astNode ->
            if (nodeIndex != 0) {
                node.addChild(PsiWhiteSpaceImpl("\n"), nodeBefore)
            }
            node.addChild(astNode, nodeBefore)
        }
    }

    @Suppress("TYPE_ALIAS")
    private fun createOrderListOfList(propertyList: List<ASTNode>): List<List<ASTNode>> {
        var oneOrderList = mutableListOf(propertyList.first())
        val orderListOfList: MutableList<MutableList<ASTNode>> = mutableListOf()
        propertyList.zipWithNext().forEach { (currentProperty, nextProperty) ->
            if (currentProperty.nextSibling { it.elementType == PROPERTY } == nextProperty) {
                oneOrderList.add(nextProperty)
            } else {
                orderListOfList.add(oneOrderList)
                oneOrderList = mutableListOf(nextProperty)
            }
        }
        orderListOfList.add(oneOrderList)
        return orderListOfList.toList()
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun sortEnum(node: ASTNode) {
        val enumEntryList = node.getChildren(null).filter { it.elementType == ElementType.ENUM_ENTRY }
        if (enumEntryList.size <= 1) {
            return
        }
        val sortList = enumEntryList.sortedBy { it.findChildByType(IDENTIFIER)!!.text }
        if (enumEntryList != sortList) {
            WRONG_DECLARATIONS_ORDER.warnAndFix(configRules, emitWarn, isFixMode, "enum entries order is incorrect", node.startOffset, node) {
                val (isEndSemicolon, isEndSpace) = removeLastSemicolonAndSpace(enumEntryList.last())
                val hasTrailingComma = (sortList.last() != enumEntryList.last() && enumEntryList.last().hasChildOfType(COMMA))
                swapSortNodes(sortList, enumEntryList, node)
                if (!hasTrailingComma) {
                    enumEntryList.last().addChild(LeafPsiElement(COMMA, ","), null)
                    sortList.last().removeChild(sortList.last().findChildByType(COMMA)!!)
                }
                if (isEndSpace) {
                    sortList.last().addChild(PsiWhiteSpaceImpl("\n"), null)
                }
                if (isEndSemicolon) {
                    sortList.last().addChild(LeafPsiElement(SEMICOLON, ";"), null)
                }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun removeLastSemicolonAndSpace(node: ASTNode): Pair<Boolean, Boolean> {
        val isSemicolon = node.hasChildOfType(SEMICOLON)
        if (isSemicolon) {
            node.removeChild(node.findChildByType(SEMICOLON)!!)
        }
        val isSpace = node.lastChildNode.isWhiteSpaceWithNewline()
        if (isSpace) {
            node.removeChild(node.lastChildNode)
        }
        return Pair(isSemicolon, isSpace)
    }

    /**
     * [RuleConfiguration] for rule that sorts class members
     */
    class SortRuleConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Whether enum members should be sorted alphabetically
         */
        val sortEnum = config["sortEnum"]?.toBoolean() ?: false

        /**
         * Whether class properties should be sorted alphabetically
         */
        val sortProperty = config["sortProperty"]?.toBoolean() ?: false
    }
}
