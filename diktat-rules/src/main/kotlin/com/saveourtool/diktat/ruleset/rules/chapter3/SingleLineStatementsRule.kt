package com.saveourtool.diktat.ruleset.rules.chapter3

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.MORE_THAN_ONE_STATEMENT_PER_LINE
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.appendNewlineMergingWhiteSpace
import com.saveourtool.diktat.ruleset.utils.extractLineOfText
import com.saveourtool.diktat.ruleset.utils.isBeginByNewline
import com.saveourtool.diktat.ruleset.utils.isFollowedByNewline
import com.saveourtool.diktat.ruleset.utils.parent

import org.jetbrains.kotlin.KtNodeTypes.ENUM_ENTRY
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.lexer.KtTokens.SEMICOLON

/**
 * Rule that looks for multiple statements on a single line separated with a `;` and splits them in multiple lines.
 */
class SingleLineStatementsRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(MORE_THAN_ONE_STATEMENT_PER_LINE)
) {
    override fun logic(node: ASTNode) {
        checkSemicolon(node)
    }

    private fun checkSemicolon(node: ASTNode) {
        node.getChildren(semicolonToken).forEach { astNode ->
            if (!astNode.isFollowedByNewline()) {
                MORE_THAN_ONE_STATEMENT_PER_LINE.warnAndFix(configRules, emitWarn, isFixMode, astNode.extractLineOfText(),
                    astNode.startOffset, astNode) {
                    if (astNode.treeParent.elementType == ENUM_ENTRY) {
                        node.treeParent.addChild(PsiWhiteSpaceImpl("\n"), node.treeNext)
                    } else {
                        if (!astNode.isBeginByNewline()) {
                            val nextNode = astNode.parent(false) { parent -> parent.treeNext != null }?.treeNext
                            node.appendNewlineMergingWhiteSpace(nextNode, astNode)
                        }
                        node.removeChild(astNode)
                    }
                }
            }
        }
    }

    companion object {
        const val NAME_ID = "statement"
        private val semicolonToken = TokenSet.create(SEMICOLON)
    }
}
