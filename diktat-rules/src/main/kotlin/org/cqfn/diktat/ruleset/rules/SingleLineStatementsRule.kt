package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.ENUM_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.SEMICOLON
import com.pinterest.ktlint.core.ast.parent
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.MORE_THAN_ONE_STATEMENT_PER_LINE
import org.cqfn.diktat.ruleset.utils.appendNewlineMergingWhiteSpace
import org.cqfn.diktat.ruleset.utils.extractLineOfText
import org.cqfn.diktat.ruleset.utils.isBeginByNewline
import org.cqfn.diktat.ruleset.utils.isFollowedByNewline
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet

class SingleLineStatementsRule(private val configRules: List<RulesConfig>) : Rule("statement") {

    companion object {
        private val semicolonToken = TokenSet.create(SEMICOLON)
    }

    private lateinit var emitWarn: EmitType
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

        checkSemicolon(node)
    }

    private fun checkSemicolon(node: ASTNode) {
        node.getChildren(semicolonToken).forEach {
            if (!it.isFollowedByNewline()) {
                MORE_THAN_ONE_STATEMENT_PER_LINE.warnAndFix(configRules, emitWarn, isFixMode, it.extractLineOfText(),
                        it.startOffset, it) {
                    if (it.treeParent.elementType == ENUM_ENTRY) {
                        node.treeParent.addChild(PsiWhiteSpaceImpl("\n"), node.treeNext)
                    } else {
                        if (!it.isBeginByNewline()) {
                            val nextNode = it.parent({ parent -> parent.treeNext != null }, strict = false)?.treeNext
                            node.appendNewlineMergingWhiteSpace(nextNode, it)
                        }
                        node.removeChild(it)
                    }
                }
            }
        }
    }
}
