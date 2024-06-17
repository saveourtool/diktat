package com.saveourtool.diktat.ruleset.rules.chapter3

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.ENUMS_SEPARATED
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.AstNodePredicate
import com.saveourtool.diktat.ruleset.utils.allSiblings
import com.saveourtool.diktat.ruleset.utils.appendNewline
import com.saveourtool.diktat.ruleset.utils.appendNewlineMergingWhiteSpace
import com.saveourtool.diktat.ruleset.utils.getAllChildrenWithType
import com.saveourtool.diktat.ruleset.utils.hasChildOfType
import com.saveourtool.diktat.ruleset.utils.isClassEnum
import com.saveourtool.diktat.ruleset.utils.isWhiteSpaceWithNewline

import org.jetbrains.kotlin.KtNodeTypes.CLASS
import org.jetbrains.kotlin.KtNodeTypes.CLASS_BODY
import org.jetbrains.kotlin.KtNodeTypes.ENUM_ENTRY
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.lexer.KtTokens.BLOCK_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.COMMA
import org.jetbrains.kotlin.lexer.KtTokens.EOL_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.IDENTIFIER
import org.jetbrains.kotlin.lexer.KtTokens.LBRACE
import org.jetbrains.kotlin.lexer.KtTokens.RBRACE
import org.jetbrains.kotlin.lexer.KtTokens.SEMICOLON
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE

/**
 * Rule that checks enum classes formatting
 */
class EnumsSeparated(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(ENUMS_SEPARATED),
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == CLASS && node.hasChildOfType(CLASS_BODY) && node.isClassEnum()) {
            checkEnumEntry(node)
        }
    }

    // Fixme prefer enum classes if it is possible instead of variables
    @Suppress("UnsafeCallOnNullableType")
    private fun checkEnumEntry(node: ASTNode) {
        val enumEntries = node.findChildByType(CLASS_BODY)!!.getAllChildrenWithType(ENUM_ENTRY)
        if (enumEntries.isEmpty() || (isEnumSimple(enumEntries) && isEnumOneLine(enumEntries))) {
            return
        }
        enumEntries.forEach { enumEntry ->
            if (!enumEntry.treeNext.isWhiteSpaceWithNewline()) {
                ENUMS_SEPARATED.warnAndFix(configRules, emitWarn, isFixMode, "enum entries must end with a line break",
                    enumEntry.startOffset, enumEntry) {
                    enumEntry.appendNewline()
                }
            }
        }
        checkLastEnum(enumEntries.last())
    }

    private fun isEnumOneLine(nodes: List<ASTNode>) =
        nodes.dropLast(1).none { it.treeNext.isWhiteSpaceWithNewline() }

    private fun isEnumSimple(enumEntries: List<ASTNode>): Boolean {
        enumEntries.forEach { node ->
            if (!simpleValue.containsAll(node.getChildren(null).map { it.elementType })) {
                return false
            }
        }
        return simpleEnum.containsAll(enumEntries
            .last()
            .allSiblings(withSelf = true)
            .map { it.elementType })
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkLastEnum(node: ASTNode) {
        if (!node.hasChildOfType(SEMICOLON)) {
            ENUMS_SEPARATED.warnAndFix(configRules, emitWarn, isFixMode, "enums must end with semicolon",
                node.startOffset, node) {
                node.addChild(LeafPsiElement(SEMICOLON, ";"), null)
                node.addChild(PsiWhiteSpaceImpl("\n"), node.findChildByType(SEMICOLON)!!)
            }
        } else if (!node.findChildByType(SEMICOLON)!!.treePrev.isWhiteSpaceWithNewline()) {
            ENUMS_SEPARATED.warnAndFix(configRules, emitWarn, isFixMode, "semicolon must be on a new line",
                node.startOffset, node) {
                node.appendNewlineMergingWhiteSpace(node.findChildByType(SEMICOLON)!!, node.findChildByType(SEMICOLON)!!)
            }
        }
        if (!node.hasChildOfType(COMMA)) {
            ENUMS_SEPARATED.warnAndFix(configRules, emitWarn, isFixMode, "last enum entry must end with a comma",
                node.startOffset, node) {
                val commaLocation = node.findChildByType(SEMICOLON)!!.findLatestTreePrevMatching {
                    it.elementType !in setOf(EOL_COMMENT, BLOCK_COMMENT, WHITE_SPACE)
                }
                node.addChild(LeafPsiElement(COMMA, ","), commaLocation.treeNext)
            }
        }
    }

    private fun ASTNode.findLatestTreePrevMatching(predicate: AstNodePredicate): ASTNode {
        val result = this.treePrev
        return if (predicate(result)) result else result.findLatestTreePrevMatching(predicate)
    }

    companion object {
        const val NAME_ID = "enum-separated"
        private val simpleValue = listOf(IDENTIFIER, WHITE_SPACE, COMMA, SEMICOLON)
        private val simpleEnum = listOf(ENUM_ENTRY, WHITE_SPACE, LBRACE, RBRACE)
    }
}
