package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_LIST
import com.pinterest.ktlint.core.ast.ElementType.SEMICOLON
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.MORE_THAN_ONE_STATEMENT_PER_LINE
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.cqfn.diktat.ruleset.utils.isFollowedByNewline
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet

class SingleLineStatementsRule : Rule("statement") {

    companion object {
        val semicolonToken = TokenSet.create(SEMICOLON)
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

        if (node.hasChildOfType(IMPORT_LIST)) checkImport(node.findChildByType(IMPORT_LIST)!!) else checkIsSemicolon(node)
    }

    private fun checkIsSemicolon(node: ASTNode) {
        node.getChildren(semicolonToken).forEach {
            if (it.treeNext != null && !it.isFollowedByNewline()) {
                MORE_THAN_ONE_STATEMENT_PER_LINE.warnAndFix(configRules, emitWarn, isFixMode, findWrongText(it) ?:"No more than one statement per line",
                        it.startOffset) {
                    node.addChild(PsiWhiteSpaceImpl("\n"), it)
                    node.removeChild(it)
                }
            }
        }
    }

    /**
     * This method was created, because to find semicolon in import, we should check text and fall two levels bellow
     */
    private fun checkImport(node: ASTNode) {
        if (checkImportText(node.text)) {
            node.findAllNodesWithSpecificType(IMPORT_DIRECTIVE).takeIf { it.size > 1 }?.forEach {
                val semicolon = it.findChildByType(SEMICOLON)
                if (semicolon != null) {
                    MORE_THAN_ONE_STATEMENT_PER_LINE.warnAndFix(configRules, emitWarn, isFixMode, findWrongText(it) ?: "No more than one statement per line",
                            semicolon.startOffset) {
                        it.addChild(PsiWhiteSpaceImpl("\n"), semicolon)
                        it.removeChild(semicolon)
                    }
                }
            }
        }
    }

    private fun checkImportText(text: String) = text.contains(";") &&
            text.indexOf(";") != text.length - 1

    private fun findWrongText(node: ASTNode): String? {
        var text: String? = ""
        var prevNode: ASTNode? = node
        var nextNode: ASTNode? = node
        while (prevNode!!.treePrev != null && !prevNode.treePrev.text.contains("\n")) {
            prevNode = prevNode.treePrev
        }
        while (nextNode!!.treeNext != null && !nextNode.treeNext.text.contains("\n")) {
            nextNode = nextNode.treeNext
        }
        do {
            text += prevNode!!.text
            prevNode = prevNode.treeNext
        } while (prevNode != nextNode)
        text += nextNode.text
        return if (text == "") null else text
    }
}
