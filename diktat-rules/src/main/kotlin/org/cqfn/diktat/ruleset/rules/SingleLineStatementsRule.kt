package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.SEMICOLON
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.MORE_THAN_ONE_STATEMENT_PER_LINE
import org.cqfn.diktat.ruleset.utils.*
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet

class SingleLineStatementsRule : Rule("statement") {

    companion object {
        val semicolonToken = TokenSet.create(SEMICOLON)
        val minListTextSize = 1
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
            if (!isSemicolonInMidLine(it)) {
                if (isError(it)) {
                    MORE_THAN_ONE_STATEMENT_PER_LINE.warnAndFix(configRules, emitWarn, isFixMode, findWrongText(it),
                            it.startOffset) {
                        node.addChild(PsiWhiteSpaceImpl("\n"), it)
                        node.removeChild(it)
                    }
                }
            }
        }
    }

    private fun isError (node: ASTNode) = (node.treeNext != null && !node.isFollowedByNewline())
            || (node.treeParent.treeNext != null && !node.treeParent.isFollowedByNewline())

    private fun isSemicolonInMidLine(node: ASTNode) = node.isBeginByNewline() || (node.treeNext != null && node.isFollowedByNewline())

    private fun findWrongText(node: ASTNode): String {
        var text: MutableList<String> = mutableListOf()
        var prevNode: ASTNode? = node.treePrev
        var nextNode: ASTNode? = node
        while (prevNode != null) {
            val listText = prevNode.text.split("\n")
            text.add(listText.last())
            if (listText.size > 1)
                break
            prevNode = prevNode.treePrev
        }
        text = text.asReversed()
        text.add(node.text)
        if (nextNode!!.treeNext == null) {
            do {
                nextNode = nextNode!!.treeParent
            } while (nextNode!!.treeNext == null && nextNode.treeParent != null)
        }
        if (nextNode.treeNext === null)
            return text.joinToString(separator = "")
        nextNode = nextNode.treeNext
        while (nextNode != null) {
            val listText = nextNode.text.split("\n")
            text.add(listText.first())
            if (listText.size > minListTextSize) {
                break
            }
            nextNode = nextNode.treeNext
        }
        return text.joinToString(separator = "")
    }
}
