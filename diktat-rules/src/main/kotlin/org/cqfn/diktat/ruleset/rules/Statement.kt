package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_LIST
import com.pinterest.ktlint.core.ast.ElementType.SEMICOLON
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.MORE_ONE_STATEMENT_PER_LINE
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet

class Statement : Rule("statement") {

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
        node.getChildren(TokenSet.create(SEMICOLON)).let { arrayOfASTNodes ->
            arrayOfASTNodes.forEach {
                if (it.treeNext != null && !checkIsSemicolonEndOfLine(it)) {
                    MORE_ONE_STATEMENT_PER_LINE.warnAndFix(configRules, emitWarn, isFixMode, "No more than one statement per line",
                            it.startOffset) {
                        node.addChild(PsiWhiteSpaceImpl("\n"), it.treeNext)
                    }
                }
            }
        }
    }

    //This method was created, because to find semicolon in import, we should check text and fall two levels bellow
    private fun checkImport(node: ASTNode) {
        if (checkImportText(node.text)) {
            node.findAllNodesWithSpecificType(IMPORT_DIRECTIVE).takeIf { it.size > 1 }?.forEach {
                val semiColon = it.findChildByType(SEMICOLON)
                if (semiColon != null) {
                    MORE_ONE_STATEMENT_PER_LINE.warnAndFix(configRules, emitWarn, isFixMode, "No more than one statement per line",
                            semiColon.startOffset) {
                        it.addChild(PsiWhiteSpaceImpl("\n"), semiColon)
                        it.addChild(LeafPsiElement(SEMICOLON, ";"), it.lastChildNode.treePrev)
                        it.removeChild(it.lastChildNode)
                    }
                }
            }
        }
    }

    private fun checkIsSemicolonEndOfLine(node: ASTNode) = node.treeNext.elementType == WHITE_SPACE && node.treeNext.text.contains("\n")

    private fun checkImportText(text: String) = text.replace(" ", "").contains(";") &&
            text.replace(" ", "").indexOf(";") != text.replace(" ", "").length - 1
}
