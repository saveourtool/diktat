package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.ARROW
import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.CATCH_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.COLON
import com.pinterest.ktlint.core.ast.ElementType.COLONCOLON
import com.pinterest.ktlint.core.ast.ElementType.CONSTRUCTOR_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.DOT
import com.pinterest.ktlint.core.ast.ElementType.DO_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.ELSE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.FINALLY_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.FOR_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.FUNCTION_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.IF_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.INIT_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.LAMBDA_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.LPAR
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.RANGE
import com.pinterest.ktlint.core.ast.ElementType.TRY_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.TYPE_CONSTRAINT
import com.pinterest.ktlint.core.ast.ElementType.VALUE_ARGUMENT
import com.pinterest.ktlint.core.ast.ElementType.WHEN_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.WHILE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.nextCodeLeaf
import com.pinterest.ktlint.core.ast.parent
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_WHITESPACE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * This rule checks usage of whitespaces for horizontal code separation
 * 1. There should be single space between keyword and (, unless keyword is `constructor`
 * 2. There should be single space between keyword and {
 */
@Suppress("ForbiddenComment")
class WhiteSpaceRule : Rule("horizontal-whitespace") {
    companion object {
        private val keywordsWithSpaceAfter = TokenSet.create(
                // these keywords are followed by {
                ELSE_KEYWORD, TRY_KEYWORD, DO_KEYWORD, FINALLY_KEYWORD, INIT_KEYWORD,
                // these keywords are followed by (
                FOR_KEYWORD, IF_KEYWORD, WHILE_KEYWORD, CATCH_KEYWORD,
                // these keywords can be followed by either { or (
                WHEN_KEYWORD
        )

        // this is the number of parent nodes needed to check if this node is lambda from argument list
        private const val numParentsForLambda = 3
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
            in keywordsWithSpaceAfter -> handleKeywordWithParOrBrace(node)
            LBRACE -> handleLbrace(node)
            OPERATION_REFERENCE, COLON, COLONCOLON, DOT, ARROW -> handleBinaryOperator(node)
        }
    }

    private fun handleConstructor(node: ASTNode) {
        if (node.treeNext.numWhiteSpaces()?.let { it > 0 } == true) {
            // there is either whitespace or newline after constructor keyword
            WRONG_WHITESPACE.warnAndFix(configRules, emitWarn, isFixMode, "keyword '${node.text}' should not be separated from " +
                    "'(' with a whitespace", node.startOffset) {
                node.treeParent.removeChild(node.treeNext)
            }
        }
    }

    private fun handleKeywordWithParOrBrace(node: ASTNode) {
        if (node.treeNext.numWhiteSpaces() != 1) {
            // there is either not single whitespace or newline after keyword
            val nextCodeLeaf = node.nextCodeLeaf()!!
            if (nextCodeLeaf.elementType != LPAR && nextCodeLeaf.elementType != LBRACE) {
                // keywords without code block, e.g. else. Fixme: should be handled too
                return
            }
            WRONG_WHITESPACE.warnAndFix(configRules, emitWarn, isFixMode, "keyword '${node.text}' should be separated from " +
                    "'${nextCodeLeaf.text}' with a whitespace", nextCodeLeaf.startOffset) {
                node.leaveSingleWhiteSpace()
            }
        }
    }

    /**
     * This method covers all other opening braces, not covered in [handleKeywordWithParOrBrace].
     */
    private fun handleLbrace(node: ASTNode) {
        val whitespaceOrPrevNode = node.parent({ it.treePrev != null }, strict = false)!!.treePrev
        val isFromLambdaAsArgument = node
                .parents()
                .take(numParentsForLambda)
                .toList()
                .takeIf { it.size == numParentsForLambda }
                ?.let {
                    it[0].elementType == FUNCTION_LITERAL &&
                            it[1].elementType == LAMBDA_EXPRESSION &&
                            it[2].elementType == VALUE_ARGUMENT
                }
                ?: false
        val prevNode = whitespaceOrPrevNode.let { if (it.elementType == WHITE_SPACE) it.treePrev else it }
        val numWhiteSpace = whitespaceOrPrevNode.numWhiteSpaces()

        if (isFromLambdaAsArgument) {
            if (numWhiteSpace != 0) {
                WRONG_WHITESPACE.warnAndFix(configRules, emitWarn, isFixMode, "there should be no whitespace before '{' of lambda" +
                        " inside argument list", node.startOffset) {
                    whitespaceOrPrevNode.treeParent.removeChild(whitespaceOrPrevNode)
                }
            }
        } else if (prevNode.elementType !in keywordsWithSpaceAfter) {
            if (numWhiteSpace != 1) {
                WRONG_WHITESPACE.warnAndFix(configRules, emitWarn, isFixMode, "there should be a whitespace before '{'", node.startOffset) {
                    prevNode.leaveSingleWhiteSpace()
                }
            }
        }
    }

    private fun handleBinaryOperator(node: ASTNode) {
        if (node.elementType == COLON && node.treeParent.elementType != TYPE_CONSTRAINT) {
            return
        }

        val operatorsWithNoWhitespace = TokenSet.create(DOT, RANGE, COLONCOLON)
        val operatorNode = if (node.elementType == OPERATION_REFERENCE) node.firstChildNode else node

        if (node.elementType == OPERATION_REFERENCE && node.treeParent.elementType == BINARY_EXPRESSION || node.elementType != OPERATION_REFERENCE) {
            val spacesBefore = node.treePrev.numWhiteSpaces()
            val spacesAfter = node.treeNext.numWhiteSpaces()
            val requiredNumSpaces = if (operatorNode.elementType in operatorsWithNoWhitespace) 0 else 1
            if (spacesBefore != null && spacesBefore != requiredNumSpaces || spacesAfter != null && spacesAfter != requiredNumSpaces) {
                val freeText = "${node.text} should ${if (requiredNumSpaces == 0) "not " else ""}be surrounded by whitespaces"
                WRONG_WHITESPACE.warnAndFix(configRules, emitWarn, isFixMode, freeText, node.startOffset) {
                    node.fixSpaceAround(requiredNumSpaces)
                }
            }
        }
    }

    private fun ASTNode.fixSpaceAround(requiredNumSpaces: Int) {
        if (requiredNumSpaces == 1) {
            treePrev.let { if (it.elementType == WHITE_SPACE) it.treePrev else it }.leaveSingleWhiteSpace()
            leaveSingleWhiteSpace()
        } else if (requiredNumSpaces == 0) {
            treePrev.removeIfWhiteSpace()
            treeNext.removeIfWhiteSpace()
        }
    }

    /**
     * This method counts whitespaces near this node. If neighbor node is WHITE_SPACE with a newline, then count of spaces is
     * meaningless and null is returned instead. If neighbor node is not a WHITE_SPACE, 0 is returned because there are zero white spaces.
     */
    private fun ASTNode.numWhiteSpaces(): Int? = if (elementType != WHITE_SPACE) {
        0
    } else {
        if (textContains('\n')) null else text.count { it == ' ' }
    }

    private fun ASTNode.leaveSingleWhiteSpace() {
        if (treeNext.elementType == WHITE_SPACE) {
            (treeNext as LeafElement).replaceWithText(" ")
        } else {
            treeParent.addChild(PsiWhiteSpaceImpl(" "), treeNext)
        }
    }

    private fun ASTNode.removeIfWhiteSpace() = takeIf { it.elementType == WHITE_SPACE }?.let { it.treeParent.removeChild(it) }
}
