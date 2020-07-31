package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.ENUM_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.SEMICOLON
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.parent
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.MORE_THAN_ONE_STATEMENT_PER_LINE
import org.cqfn.diktat.ruleset.utils.isBeginByNewline
import org.cqfn.diktat.ruleset.utils.isFollowedByNewline
import org.cqfn.diktat.ruleset.utils.takeWhileInclusive
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.psi.psiUtil.siblings

class SingleLineStatementsRule : Rule("statement") {

    companion object {
        val semicolonToken = TokenSet.create(SEMICOLON)
        const val minListTextSize = 1
    }

    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var fileName: String? = null
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        configRules = params.getDiktatConfigRules()
        fileName = params.fileName
        emitWarn = emit
        isFixMode = autoCorrect

        checkSemicolon(node)
    }

    private fun checkSemicolon(node: ASTNode) {
        node.getChildren(semicolonToken).forEach {
            if (!isSemicolonAtEndOfLine(it)) {
                if (isError(it)) {
                    MORE_THAN_ONE_STATEMENT_PER_LINE.warnAndFix(configRules, emitWarn, isFixMode, findWrongText(it),
                            it.startOffset) {
                        if (it.treeParent.elementType == ENUM_ENTRY){
                            node.treeParent.addChild(PsiWhiteSpaceImpl("\n"), node.treeNext)
                        } else {
                            if (!it.isBeginByNewline()) {
                                val nextNode = it.parent({ parent -> parent.treeNext != null}, strict=false)?.treeNext
                                if ( nextNode != null && nextNode.elementType == WHITE_SPACE)
                                    (nextNode as LeafPsiElement).replaceWithText("\n${nextNode.text}")
                                else
                                    node.addChild(PsiWhiteSpaceImpl("\n"), it)
                            }
                            node.removeChild(it)
                        }
                    }
                }
            }
        }
    }

    /**
     * Sometimes the semicolon is the last leaf of the tree at a given level, so you need to go up a few levels to check if there is something behind the semicolon
     */
    private fun isError (node: ASTNode) = node.parent({ it.treeNext != null}, strict=false)?.let { !it.isFollowedByNewline() } ?: false


    private fun isSemicolonAtEndOfLine(node: ASTNode) = (node.treeNext != null && node.isFollowedByNewline())

    private fun findWrongText(node: ASTNode): String {
        var text = mutableListOf<String>()
        val nextNode = node.parent({ it.treeNext != null }, false) ?: node
        node.siblings(false)
                .map { it.text.split("\n") }
                .takeWhileInclusive { it.size <= minListTextSize }
                .forEach { text.add(it.last()) }
        text = text.asReversed()
        text.add(node.text)
        nextNode.siblings(true)
            .map { it.text.split("\n") }
            .takeWhileInclusive { it.size <= minListTextSize }
            .forEach { text.add(it.first()) }
        return text.joinToString(separator = "")
    }
}
