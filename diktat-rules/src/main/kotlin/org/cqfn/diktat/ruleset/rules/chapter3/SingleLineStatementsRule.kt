package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.MORE_THAN_ONE_STATEMENT_PER_LINE
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.appendNewlineMergingWhiteSpace
import org.cqfn.diktat.ruleset.utils.extractLineOfText
import org.cqfn.diktat.ruleset.utils.isBeginByNewline
import org.cqfn.diktat.ruleset.utils.isFollowedByNewline

import com.pinterest.ktlint.core.ast.ElementType.ENUM_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.SEMICOLON
import com.pinterest.ktlint.core.ast.parent
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet

/**
 * Rule that looks for multiple statements on a single line separated with a `;` and splits them in multiple lines.
 */
class SingleLineStatementsRule(configRules: List<RulesConfig>) : DiktatRule(
    "017-statement",
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
                            val nextNode = astNode.parent({ parent -> parent.treeNext != null }, strict = false)?.treeNext
                            node.appendNewlineMergingWhiteSpace(nextNode, astNode)
                        }
                        node.removeChild(astNode)
                    }
                }
            }
        }
    }

    companion object {
        private val semicolonToken = TokenSet.create(SEMICOLON)
    }
}
