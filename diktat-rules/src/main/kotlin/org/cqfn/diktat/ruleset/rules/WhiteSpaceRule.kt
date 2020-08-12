package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.CATCH_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.CONSTRUCTOR_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.DO_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.ELSE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.FINALLY_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.FOR_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.FUNCTION_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.IF_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.INIT_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.LAMBDA_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.TRY_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.VALUE_ARGUMENT
import com.pinterest.ktlint.core.ast.ElementType.WHEN_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.WHILE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.parent
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_WHITESPACE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.psi.psiUtil.parents

class WhiteSpaceRule : Rule("horizontal-whitespace") {
    companion object {
        private val keywordsWithBraces = listOf(ELSE_KEYWORD, TRY_KEYWORD, DO_KEYWORD, WHEN_KEYWORD, FINALLY_KEYWORD, INIT_KEYWORD)
        private val keywordsWithPar = listOf(FOR_KEYWORD, IF_KEYWORD, WHILE_KEYWORD, CATCH_KEYWORD, WHEN_KEYWORD)
    }

    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        configRules = params.getDiktatConfigRules()
        emitWarn = emit
        isFixMode = autoCorrect

        when (node.elementType) {
            CONSTRUCTOR_KEYWORD -> handleConstructor(node)
            in keywordsWithPar -> handleKeywordWithParOrBrace(node)
            LBRACE -> handleLbrace(node)
            OPERATION_REFERENCE -> if (node.treeParent.elementType == BINARY_EXPRESSION) handleBinaryOperator(node)
        }
    }

    private fun handleConstructor(node: ASTNode) {
        if (node.treeNext.numWhiteSpaces() > 0) {
            WRONG_WHITESPACE.warnAndFix(configRules, emitWarn, isFixMode, "keyword '${node.text}' should not be separated from " +
                    "'(' with a whitespace", node.startOffset) {
                node.treeParent.removeChild(node.treeNext)
            }
        }
    }

    private fun handleKeywordWithParOrBrace(node: ASTNode) {
        if (node.treeNext.numWhiteSpaces() != 1) {
            WRONG_WHITESPACE.warnAndFix(configRules, emitWarn, isFixMode, "keyword '${node.text}' should be separated from '(' with a whitespace", node.startOffset) {
                node.leaveSingleWhiteSpace()
            }
        }
    }

    private fun handleLbrace(node: ASTNode) {
        val whitespaceOrPrevNode = node.parent({ it.treePrev != null }, strict = false)!!.treePrev
        val isFromLambdaAsArgument = node.parents().take(3).toList().let {
            it[0].elementType == FUNCTION_LITERAL &&
                    it[1].elementType == LAMBDA_EXPRESSION &&
                    it[2].elementType == VALUE_ARGUMENT
        }

        if (isFromLambdaAsArgument) {
            if (whitespaceOrPrevNode.numWhiteSpaces() != 0) {
                WRONG_WHITESPACE.warnAndFix(configRules, emitWarn, isFixMode, "there should be no whitespace before '{' of lambda" +
                        " inside argument list", node.startOffset) {
                    whitespaceOrPrevNode.treeParent.removeChild(whitespaceOrPrevNode)
                }
            }
        } else {
            if (whitespaceOrPrevNode.numWhiteSpaces() != 1) {
                val prevNode = whitespaceOrPrevNode.let { if (it.elementType == WHITE_SPACE) it.treePrev else it }
                val freeText = if (prevNode.elementType in keywordsWithBraces) {
                    "keyword '${prevNode.text}' should be separated from '{' with a whitespace"
                } else {
                    "there should be a whitespace before '{'"
                }
                WRONG_WHITESPACE.warnAndFix(configRules, emitWarn, isFixMode, freeText, node.startOffset) {
                    prevNode.leaveSingleWhiteSpace()
                }
            }
        }
    }

    private fun handleBinaryOperator(node: ASTNode) {

    }

    private fun ASTNode.numWhiteSpaces() = if (elementType == WHITE_SPACE) text.count { it == ' ' } else 0

    private fun ASTNode.leaveSingleWhiteSpace() {
        if (treeNext.elementType == WHITE_SPACE) {
            (treeNext as LeafElement).replaceWithText(" ")
        } else {
            treeParent.addChild(PsiWhiteSpaceImpl(" "), treeNext)
        }
    }
}
