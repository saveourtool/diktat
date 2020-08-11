package org.cqfn.diktat.ruleset.rules.files

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.ANDAND
import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.CALLABLE_REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.COLONCOLON
import com.pinterest.ktlint.core.ast.ElementType.DIV
import com.pinterest.ktlint.core.ast.ElementType.DIVEQ
import com.pinterest.ktlint.core.ast.ElementType.DOT
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.ELVIS
import com.pinterest.ktlint.core.ast.ElementType.ENUM_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.IF
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.LAMBDA_ARGUMENT
import com.pinterest.ktlint.core.ast.ElementType.MINUS
import com.pinterest.ktlint.core.ast.ElementType.MINUSEQ
import com.pinterest.ktlint.core.ast.ElementType.MUL
import com.pinterest.ktlint.core.ast.ElementType.MULTEQ
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.OROR
import com.pinterest.ktlint.core.ast.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.PLUS
import com.pinterest.ktlint.core.ast.ElementType.PLUSEQ
import com.pinterest.ktlint.core.ast.ElementType.SAFE_ACCESS
import com.pinterest.ktlint.core.ast.ElementType.SAFE_ACCESS_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.SEMICOLON
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.parent
import com.pinterest.ktlint.core.ast.prevCodeSibling
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.REDUNDANT_SEMICOLON
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_NEWLINES
import org.cqfn.diktat.ruleset.rules.getDiktatConfigRules
import org.cqfn.diktat.ruleset.utils.appendNewlineMergingWhiteSpace
import org.cqfn.diktat.ruleset.utils.extractLineOfText
import org.cqfn.diktat.ruleset.utils.getAllLeafsWithSpecificType
import org.cqfn.diktat.ruleset.utils.isBeginByNewline
import org.cqfn.diktat.ruleset.utils.isEol
import org.cqfn.diktat.ruleset.utils.isFollowedByNewline
import org.cqfn.diktat.ruleset.utils.isSingleLineIfElse
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * Rule that checks line break styles.
 * 1. Prohibits usage of semicolons at the end of line
 * 2. Checks that some operators are followed by newline, while others are prepended by it
 * 3. Statements that follow `!!` behave in the same way
 * 4. Forces functional style of chained dot call expressions with exception
 */
@Suppress("ForbiddenComment")
class NewlinesRule : Rule("newlines") {
    companion object {
        // fixme: these token sets can be not full, need to add new once as corresponding cases are discovered.
        // error is raised if these operators are prepended by newline
        private val lineBreakAfterOperators = TokenSet.create(ANDAND, OROR, PLUS, PLUSEQ, MINUS, MINUSEQ, MUL, MULTEQ, DIV, DIVEQ)

        // error is raised if these operators are followed by newline
        private val lineBreakBeforeOperators = TokenSet.create(DOT, SAFE_ACCESS, ELVIS, COLONCOLON)

        private val expressionTypes = TokenSet.create(DOT_QUALIFIED_EXPRESSION, SAFE_ACCESS_EXPRESSION, CALLABLE_REFERENCE_EXPRESSION, BINARY_EXPRESSION)
        private val chainExpressionTypes = TokenSet.create(DOT_QUALIFIED_EXPRESSION, SAFE_ACCESS_EXPRESSION)
    }

    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        configRules = params.getDiktatConfigRules()
        isFixMode = autoCorrect
        emitWarn = emit

        if (node.elementType == SEMICOLON) {
            handleSemicolon(node)
        } else if (node.elementType in lineBreakAfterOperators) {
            handleOperatorWithLineBreakAfter(node)
        } else if (node.elementType in lineBreakBeforeOperators && !node.isDotFromPackageOrImport()) {
            handleOperatorWithLineBreakBefore(node)
        }
    }

    private fun handleSemicolon(node: ASTNode) {
        if (node.isEol() && node.treeParent.elementType != ENUM_ENTRY) {
            // semicolon at the end of line which is not part of enum members declarations
            REDUNDANT_SEMICOLON.warnAndFix(configRules, emitWarn, isFixMode, node.extractLineOfText(), node.startOffset) {
                node.treeParent.removeChild(node)
            }
        }
    }

    private fun handleOperatorWithLineBreakAfter(node: ASTNode) {
        // We need to check newline only if prevCodeSibling exists. It can be not the case for unary operators, which are placed
        // at the beginning of the line.
        if (node.selfOrOperationReferenceParent().prevCodeSibling()?.isFollowedByNewline() == true) {
            WRONG_NEWLINES.warnAndFix(configRules, emitWarn, isFixMode, "should break a line after and not before ${node.text}", node.startOffset) {
                node.selfOrOperationReferenceParent().run {
                    treeParent.removeChild(treePrev)
                    if (!isFollowedByNewline()) {
                        treeParent.appendNewlineMergingWhiteSpace(treeNext.takeIf { it.elementType == WHITE_SPACE }, treeNext)
                    }
                }
            }
        }
    }

    private fun handleOperatorWithLineBreakBefore(node: ASTNode) {
        val isIncorrect = node.run {
            if (isCallsChain()) {
                val isSingleLineIfElse = parent({ it.elementType == IF }, true)?.isSingleLineIfElse() ?: false
                // to follow functional style these operators should be started by newline
                (isFollowedByNewline() || !isBeginByNewline()) && !isSingleLineIfElse
            } else {
                // unless statement is simple and on single line, these operators cannot have newline after
                isFollowedByNewline() && !isSingleDotStatementOnSingleLine()
            }
        }
        if (isIncorrect) {
            val freeText = if (node.isCallsChain()) {
                "should follow functional style at ${node.text}"
            } else {
                "should break a line before and not after ${node.text}"
            }
            WRONG_NEWLINES.warnAndFix(configRules, emitWarn, isFixMode, freeText, node.startOffset) {
                node.selfOrOperationReferenceParent().run {
                    if (!isBeginByNewline()) {
                        // prepend newline
                        treeParent.appendNewlineMergingWhiteSpace(treePrev.takeIf { it.elementType == WHITE_SPACE }, this)
                    }
                    if (isFollowedByNewline()) {
                        // remove newline after
                        parent({ it.treeNext != null }, false)?.let {
                            it.treeParent.removeChild(it.treeNext)
                        }
                    }
                }
            }
        }
    }

    /**
     * This function is needed because many operators are represented as a single child of [OPERATION_REFERENCE] node
     * e.g. [ANDAND] is a single child of [OPERATION_REFERENCE]
     */
    private fun ASTNode.selfOrOperationReferenceParent() =
            treeParent.takeIf { it.elementType in listOf(OPERATION_REFERENCE) } ?: this

    private fun ASTNode.isSingleDotStatementOnSingleLine() = parents()
            .takeWhile { it.elementType in expressionTypes }
            .singleOrNull()
            ?.let { it.text.lines().count() == 1 }
            ?: false

    // fixme: there could be other cases when dot means something else
    private fun ASTNode.isDotFromPackageOrImport() = elementType == DOT &&
            parent({ it.elementType == IMPORT_DIRECTIVE || it.elementType == PACKAGE_DIRECTIVE }, true) != null

    /**
     *  taking all expressions inside complex expression until we reach lambda arguments
     */
    private fun ASTNode.isCallsChain() = getParentExpressions()
            .lastOrNull()
            ?.run {
                mutableListOf<ASTNode>().also {
                    getAllLeafsWithSpecificType(DOT, it)
                    getAllLeafsWithSpecificType(SAFE_ACCESS, it)
                }
            }
            ?.filter { it.getParentExpressions().count() > 1 }
            ?.count()
            ?.let { it > 1 }
            ?: false

    private fun ASTNode.getParentExpressions() =
            parents().takeWhile { it.elementType in chainExpressionTypes && it.elementType != LAMBDA_ARGUMENT }
}
