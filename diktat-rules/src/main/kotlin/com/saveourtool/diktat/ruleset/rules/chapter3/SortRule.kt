package com.saveourtool.diktat.ruleset.rules.chapter3

import com.saveourtool.diktat.common.config.rules.RuleConfiguration
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getRuleConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.WRONG_DECLARATIONS_ORDER
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.findAllDescendantsWithSpecificType
import com.saveourtool.diktat.ruleset.utils.hasChildOfType
import com.saveourtool.diktat.ruleset.utils.isClassEnum
import com.saveourtool.diktat.ruleset.utils.isPartOfComment
import com.saveourtool.diktat.ruleset.utils.isWhiteSpace
import com.saveourtool.diktat.ruleset.utils.isWhiteSpaceWithNewline
import com.saveourtool.diktat.ruleset.utils.nextSibling

import org.jetbrains.kotlin.KtNodeTypes.CLASS_BODY
import org.jetbrains.kotlin.KtNodeTypes.ENUM_ENTRY
import org.jetbrains.kotlin.KtNodeTypes.MODIFIER_LIST
import org.jetbrains.kotlin.KtNodeTypes.PROPERTY
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.lexer.KtTokens.COMMA
import org.jetbrains.kotlin.lexer.KtTokens.CONST_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.EOL_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.IDENTIFIER
import org.jetbrains.kotlin.lexer.KtTokens.SEMICOLON
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.psiUtil.siblings

/**
 * Rule that sorts class properties and enum members alphabetically
 */
class SortRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(WRONG_DECLARATIONS_ORDER),
) {
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

    @OptIn(ExperimentalStdlibApi::class)
    private fun swapSortNodes(
        sortList: List<ASTNode>,
        nonSortList: List<ASTNode>,
        node: ASTNode
    ) {
        val isEnum = nonSortList.first().elementType == ENUM_ENTRY
        val spaceBefore = if (node.findAllDescendantsWithSpecificType(EOL_COMMENT).isNotEmpty() && isEnum) {
            nonSortList.last().run {
                if (this.hasChildOfType(EOL_COMMENT) && !this.hasChildOfType(COMMA)) {
                    this.addChild(LeafPsiElement(COMMA, ","), this.findChildByType(EOL_COMMENT))
                }
            }
            buildList(nonSortList.size) {
                add(null)
                repeat(nonSortList.size - 1) {
                    add(listOf(PsiWhiteSpaceImpl("\n")))
                }
            }
        } else {
            nonSortList.map { astNode ->
                astNode
                    .siblings(false)
                    .toList()
                    .takeWhile { it.isWhiteSpace() || it.isPartOfComment() }
                    .ifEmpty { null }
            }
        }
        val nodeInsertBefore: ASTNode? = nonSortList.last().treeNext
        node.removeRange(nonSortList.first(), nonSortList.last().treeNext)
        sortList.mapIndexed { index, astNode ->
            spaceBefore[index]?.let { prevList -> prevList.map { node.addChild(it, nodeInsertBefore) } }
            if (!astNode.hasChildOfType(COMMA) && isEnum) {
                astNode.addChild(LeafPsiElement(COMMA, ","), null)
            }
            node.addChild(astNode, nodeInsertBefore)
        }
    }

    @Suppress("TYPE_ALIAS")
    private fun createOrderListOfList(propertyList: List<ASTNode>): List<List<ASTNode>> {
        val oneOrderList = mutableListOf(propertyList.first())
        val orderListOfList: MutableList<MutableList<ASTNode>> = mutableListOf()
        propertyList.zipWithNext().forEach { (currentProperty, nextProperty) ->
            if (currentProperty.nextSibling { it.elementType == PROPERTY } != nextProperty) {
                orderListOfList.add(oneOrderList.toMutableList())
                oneOrderList.clear()
            }
            oneOrderList.add(nextProperty)
        }
        orderListOfList.add(oneOrderList)
        return orderListOfList.toList()
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun sortEnum(node: ASTNode) {
        val enumEntryList = node.getChildren(null).filter { it.elementType == ENUM_ENTRY }
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
                    val lastEntry = node.findAllDescendantsWithSpecificType(ENUM_ENTRY).last()
                    lastEntry.removeChild(lastEntry.findChildByType(COMMA)!!)
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

    companion object {
        const val NAME_ID = "sort-rule"
    }
}
