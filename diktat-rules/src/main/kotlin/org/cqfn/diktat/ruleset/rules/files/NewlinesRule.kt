package org.cqfn.diktat.ruleset.rules.files

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.ANDAND
import com.pinterest.ktlint.core.ast.ElementType.ARROW
import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.CALLABLE_REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.COLON
import com.pinterest.ktlint.core.ast.ElementType.COLONCOLON
import com.pinterest.ktlint.core.ast.ElementType.COMMA
import com.pinterest.ktlint.core.ast.ElementType.DIV
import com.pinterest.ktlint.core.ast.ElementType.DIVEQ
import com.pinterest.ktlint.core.ast.ElementType.DOT
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.ELVIS
import com.pinterest.ktlint.core.ast.ElementType.ENUM_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.EQ
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.FUNCTION_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.IF
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.LAMBDA_ARGUMENT
import com.pinterest.ktlint.core.ast.ElementType.LPAR
import com.pinterest.ktlint.core.ast.ElementType.MINUS
import com.pinterest.ktlint.core.ast.ElementType.MINUSEQ
import com.pinterest.ktlint.core.ast.ElementType.MUL
import com.pinterest.ktlint.core.ast.ElementType.MULTEQ
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.OROR
import com.pinterest.ktlint.core.ast.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.PLUS
import com.pinterest.ktlint.core.ast.ElementType.PLUSEQ
import com.pinterest.ktlint.core.ast.ElementType.PRIMARY_CONSTRUCTOR
import com.pinterest.ktlint.core.ast.ElementType.RETURN
import com.pinterest.ktlint.core.ast.ElementType.RETURN_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.SAFE_ACCESS
import com.pinterest.ktlint.core.ast.ElementType.SAFE_ACCESS_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.SECONDARY_CONSTRUCTOR
import com.pinterest.ktlint.core.ast.ElementType.SEMICOLON
import com.pinterest.ktlint.core.ast.ElementType.VALUE_ARGUMENT_LIST
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER_LIST
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.nextCodeSibling
import com.pinterest.ktlint.core.ast.parent
import com.pinterest.ktlint.core.ast.prevCodeSibling
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.REDUNDANT_SEMICOLON
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_NEWLINES
import org.cqfn.diktat.ruleset.utils.appendNewlineMergingWhiteSpace
import org.cqfn.diktat.ruleset.utils.emptyBlockList
import org.cqfn.diktat.ruleset.utils.extractLineOfText
import org.cqfn.diktat.ruleset.utils.getAllLeafsWithSpecificType
import org.cqfn.diktat.ruleset.utils.isBeginByNewline
import org.cqfn.diktat.ruleset.utils.isEol
import org.cqfn.diktat.ruleset.utils.isFollowedByNewline
import org.cqfn.diktat.ruleset.utils.isSingleLineIfElse
import org.cqfn.diktat.ruleset.utils.leaveOnlyOneNewLine
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.psi.psiUtil.children
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.siblings

/**
 * Rule that checks line break styles.
 * 1. Prohibits usage of semicolons at the end of line
 * 2. Checks that some operators are followed by newline, while others are prepended by it
 * 3. Statements that follow `!!` behave in the same way
 * 4. Forces functional style of chained dot call expressions with exception
 * 5. Checks that newline is placed after assignment operator, not before
 * 6. Ensures that function or constructor name isn't separated from `(` by space or newline
 * 7. Ensures that in multiline lambda newline follows arrow or, in case of lambda without explicit parameters, opening brace
 * 8. Checks that functions with single `return` are simplified to functions with expression body
 */
@Suppress("ForbiddenComment")
class NewlinesRule(private val configRules: List<RulesConfig>) : Rule("newlines") {
    companion object {
        // fixme: these token sets can be not full, need to add new once as corresponding cases are discovered.
        // error is raised if these operators are prepended by newline
        private val lineBreakAfterOperators = TokenSet.create(ANDAND, OROR, PLUS, PLUSEQ, MINUS, MINUSEQ, MUL, MULTEQ, DIV, DIVEQ)

        // error is raised if these operators are followed by newline
        private val lineBreakBeforeOperators = TokenSet.create(DOT, SAFE_ACCESS, ELVIS, COLONCOLON)

        private val expressionTypes = TokenSet.create(DOT_QUALIFIED_EXPRESSION, SAFE_ACCESS_EXPRESSION, CALLABLE_REFERENCE_EXPRESSION, BINARY_EXPRESSION)
        private val chainExpressionTypes = TokenSet.create(DOT_QUALIFIED_EXPRESSION, SAFE_ACCESS_EXPRESSION)
    }

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        isFixMode = autoCorrect
        emitWarn = emit

        when (node.elementType) {
            SEMICOLON -> handleSemicolon(node)
            OPERATION_REFERENCE, EQ -> handleOperatorWithLineBreakAfter(node)
            in lineBreakBeforeOperators -> handleOperatorWithLineBreakBefore(node)
            LPAR -> handleOpeningParentheses(node)
            COMMA -> handleComma(node)
            BLOCK -> handleLambdaBody(node)
            RETURN -> handleReturnStatement(node)
        }
    }

    private fun handleSemicolon(node: ASTNode) {
        if (node.isEol() && node.treeParent.elementType != ENUM_ENTRY) {
            // semicolon at the end of line which is not part of enum members declarations
            REDUNDANT_SEMICOLON.warnAndFix(configRules, emitWarn, isFixMode, node.extractLineOfText(), node.startOffset, node) {
                node.treeParent.removeChild(node)
            }
        }
    }

    private fun handleOperatorWithLineBreakAfter(node: ASTNode) {
        // [node] should be either EQ or OPERATION_REFERENCE which has single child
        if (!(node.elementType == EQ || node.firstChildNode.elementType in lineBreakAfterOperators || node.isInfixCall())) {
            return
        }

        // We need to check newline only if prevCodeSibling exists. It can be not the case for unary operators, which are placed
        // at the beginning of the line.
        if (node.prevCodeSibling()?.isFollowedByNewline() == true) {
            WRONG_NEWLINES.warnAndFix(configRules, emitWarn, isFixMode,
                    "should break a line after and not before ${node.text}", node.startOffset, node) {
                node.run {
                    treeParent.removeChild(treePrev)
                    if (!isFollowedByNewline()) {
                        treeParent.appendNewlineMergingWhiteSpace(treeNext.takeIf { it.elementType == WHITE_SPACE }, treeNext)
                    }
                }
            }
        }
    }

    private fun handleOperatorWithLineBreakBefore(node: ASTNode) {
        if (node.isDotFromPackageOrImport()) {
            return
        }
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
            WRONG_NEWLINES.warnAndFix(configRules, emitWarn, isFixMode, freeText, node.startOffset, node) {
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

    private fun handleOpeningParentheses(node: ASTNode) {
        val parent = node.treeParent
        if (parent.elementType in listOf(VALUE_ARGUMENT_LIST, VALUE_PARAMETER_LIST)) {
            val prevWhiteSpace = node
                    .parent({ it.treePrev != null }, strict = false)
                    ?.treePrev
                    ?.takeIf { it.elementType == WHITE_SPACE }
            val isNotAnonymous = parent.treeParent.elementType in listOf(CALL_EXPRESSION, PRIMARY_CONSTRUCTOR, SECONDARY_CONSTRUCTOR, FUN)
            if (prevWhiteSpace != null && isNotAnonymous) {
                WRONG_NEWLINES.warnAndFix(configRules, emitWarn, isFixMode,
                        "opening parentheses should not be separated from constructor or function name", node.startOffset, node) {
                    prevWhiteSpace.treeParent.removeChild(prevWhiteSpace)
                }
            }
        }
    }

    private fun handleComma(node: ASTNode) {
        val prevNewLine = node
                .parent({ it.treePrev != null }, strict = false)
                ?.treePrev
                ?.takeIf {
                    it.elementType == WHITE_SPACE && it.text.contains("\n")
                }
        if (prevNewLine != null) {
            WRONG_NEWLINES.warnAndFix(configRules, emitWarn, isFixMode, "newline should be placed only after comma", node.startOffset, node) {
                prevNewLine.treeParent.removeChild(prevNewLine)
            }
        }
    }

    private fun handleLambdaBody(node: ASTNode) {
        if (node.treeParent.elementType == FUNCTION_LITERAL) {
            val isSingleLineLambda = node.treeParent.text.lines().size == 1
            val arrowNode = node.siblings(false).find { it.elementType == ARROW }
            if (!isSingleLineLambda && arrowNode != null) {
                // lambda with explicit arguments
                val newlinesBeforeArrow = arrowNode
                        .siblings(false)
                        .filter { it.elementType == WHITE_SPACE && it.textContains('\n') }
                        .toList()
                if (newlinesBeforeArrow.isNotEmpty() || !arrowNode.isFollowedByNewline()) {
                    WRONG_NEWLINES.warnAndFix(configRules, emitWarn, isFixMode,
                            "in lambda with several lines in body newline should be placed after an arrow", arrowNode.startOffset, arrowNode) {
                        // fixme: replacement logic can be sophisticated for better appearance?
                        newlinesBeforeArrow.forEach { it.treeParent.replaceChild(it, PsiWhiteSpaceImpl(" ")) }
                        arrowNode.treeNext.takeIf { it.elementType == WHITE_SPACE }?.leaveOnlyOneNewLine()
                    }
                }
            } else if (!isSingleLineLambda && arrowNode == null) {
                // lambda without arguments
                val lbraceNode = node.treeParent.firstChildNode
                if (!lbraceNode.isFollowedByNewline()) {
                    WRONG_NEWLINES.warnAndFix(configRules, emitWarn, isFixMode,
                            "in lambda with several lines in body newline should be placed after an opening brace", lbraceNode.startOffset, lbraceNode) {
                        lbraceNode.treeNext.let {
                            if (it.elementType == WHITE_SPACE) {
                                it.leaveOnlyOneNewLine()
                            } else {
                                it.treeParent.addChild(PsiWhiteSpaceImpl("\n"), it)
                            }
                        }
                    }
                }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleReturnStatement(node: ASTNode) {
        val blockNode = node.treeParent.takeIf { it.elementType == BLOCK && it.treeParent.elementType == FUN }
        val returnsUnit = node.children().count() == 1  // the only child is RETURN_KEYWORD
        if (blockNode == null || returnsUnit) {
            // function is either already with expression body or definitely can't be converted to it
            return
        }
        blockNode
                .children()
                .filterNot { it.elementType in emptyBlockList }
                .toList()
                .takeIf { it.size == 1 }
                ?.also {
                    WRONG_NEWLINES.warnAndFix(configRules, emitWarn, isFixMode,
                            "functions with single return statement should be simplified to expression body", node.startOffset, node) {
                        val funNode = blockNode.treeParent
                        // if return type is not Unit, then there should be type specification
                        // otherwise code won't compile and colon being null is correctly invalid
                        val colon = funNode.findChildByType(COLON)!!
                        val expression = node.findChildByType(RETURN_KEYWORD)!!.nextCodeSibling()!!
                        funNode.apply {
                            removeRange(colon, null)
                            addChild(PsiWhiteSpaceImpl(" "), null)
                            addChild(LeafPsiElement(EQ, "="), null)
                            addChild(PsiWhiteSpaceImpl(" "), null)
                            addChild(expression.clone() as ASTNode, null)
                        }
                    }
                }
    }

    /**
     * This function is needed because many operators are represented as a single child of [OPERATION_REFERENCE] node
     * e.g. [ANDAND] is a single child of [OPERATION_REFERENCE]
     */
    private fun ASTNode.selfOrOperationReferenceParent() = treeParent.takeIf { it.elementType == OPERATION_REFERENCE } ?: this

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

    /**
     * This method should be called on OPERATION_REFERENCE in the middle of BINARY_EXPRESSION
     */
    private fun ASTNode.isInfixCall() = elementType == OPERATION_REFERENCE &&
            firstChildNode.elementType == IDENTIFIER &&
            treeParent.elementType == BINARY_EXPRESSION
}
