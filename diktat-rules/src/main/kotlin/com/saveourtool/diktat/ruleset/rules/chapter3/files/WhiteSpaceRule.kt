package com.saveourtool.diktat.ruleset.rules.chapter3.files

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getRuleConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.LONG_LINE
import com.saveourtool.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import com.saveourtool.diktat.ruleset.constants.Warnings.WRONG_WHITESPACE
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.rules.chapter3.LineLength
import com.saveourtool.diktat.ruleset.utils.appendNewlineMergingWhiteSpace
import com.saveourtool.diktat.ruleset.utils.calculateLineColByOffset
import com.saveourtool.diktat.ruleset.utils.findParentNodeWithSpecificType
import com.saveourtool.diktat.ruleset.utils.hasChildOfType
import com.saveourtool.diktat.ruleset.utils.isPartOfComment
import com.saveourtool.diktat.ruleset.utils.isWhiteSpace
import com.saveourtool.diktat.ruleset.utils.nextCodeLeaf
import com.saveourtool.diktat.ruleset.utils.parent
import com.saveourtool.diktat.ruleset.utils.prevSibling

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.kotlin.KtNodeTypes.ANNOTATION_ENTRY
import org.jetbrains.kotlin.KtNodeTypes.BINARY_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.BLOCK
import org.jetbrains.kotlin.KtNodeTypes.CALLABLE_REFERENCE_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.CALL_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.CLASS
import org.jetbrains.kotlin.KtNodeTypes.COLLECTION_LITERAL_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.CONSTRUCTOR_DELEGATION_CALL
import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.FUNCTION_LITERAL
import org.jetbrains.kotlin.KtNodeTypes.LAMBDA_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.NULLABLE_TYPE
import org.jetbrains.kotlin.KtNodeTypes.OBJECT_DECLARATION
import org.jetbrains.kotlin.KtNodeTypes.OPERATION_REFERENCE
import org.jetbrains.kotlin.KtNodeTypes.POSTFIX_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.PRIMARY_CONSTRUCTOR
import org.jetbrains.kotlin.KtNodeTypes.PROPERTY
import org.jetbrains.kotlin.KtNodeTypes.SECONDARY_CONSTRUCTOR
import org.jetbrains.kotlin.KtNodeTypes.TYPE_CONSTRAINT
import org.jetbrains.kotlin.KtNodeTypes.TYPE_PARAMETER
import org.jetbrains.kotlin.KtNodeTypes.TYPE_PARAMETER_LIST
import org.jetbrains.kotlin.KtNodeTypes.VALUE_ARGUMENT
import org.jetbrains.kotlin.KtNodeTypes.VALUE_ARGUMENT_LIST
import org.jetbrains.kotlin.KtNodeTypes.VALUE_PARAMETER
import org.jetbrains.kotlin.KtNodeTypes.VALUE_PARAMETER_LIST
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.lexer.KtTokens.ARROW
import org.jetbrains.kotlin.lexer.KtTokens.CATCH_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.COLON
import org.jetbrains.kotlin.lexer.KtTokens.COLONCOLON
import org.jetbrains.kotlin.lexer.KtTokens.COMMA
import org.jetbrains.kotlin.lexer.KtTokens.CONSTRUCTOR_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.DOT
import org.jetbrains.kotlin.lexer.KtTokens.DO_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.ELSE_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.ELVIS
import org.jetbrains.kotlin.lexer.KtTokens.EQ
import org.jetbrains.kotlin.lexer.KtTokens.EXCLEXCL
import org.jetbrains.kotlin.lexer.KtTokens.FINALLY_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.FOR_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.GT
import org.jetbrains.kotlin.lexer.KtTokens.IF_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.INIT_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.LBRACE
import org.jetbrains.kotlin.lexer.KtTokens.LBRACKET
import org.jetbrains.kotlin.lexer.KtTokens.LPAR
import org.jetbrains.kotlin.lexer.KtTokens.LT
import org.jetbrains.kotlin.lexer.KtTokens.QUEST
import org.jetbrains.kotlin.lexer.KtTokens.RANGE
import org.jetbrains.kotlin.lexer.KtTokens.RBRACE
import org.jetbrains.kotlin.lexer.KtTokens.RBRACKET
import org.jetbrains.kotlin.lexer.KtTokens.RPAR
import org.jetbrains.kotlin.lexer.KtTokens.SAFE_ACCESS
import org.jetbrains.kotlin.lexer.KtTokens.SEMICOLON
import org.jetbrains.kotlin.lexer.KtTokens.TRY_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.WHEN_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.WHILE_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtPostfixExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType

/**
 * This rule checks usage of whitespaces for horizontal code separation
 * 1. There should be single space between keyword and (, unless keyword is `constructor`
 * 2. There should be single space between keyword and {
 * 3. There should be single space before any {, unless lambda inside parentheses of argument list
 * 4. Binary operators should be surrounded by whitespaces, excluding :: and .
 * 5. Spaces should be used after `,`, `:`, `;` (except cases when those symbols are in the end of line).
 *    There should be no whitespaces in the end of line.
 * 6. There should be only one space between identifier and it's type, if type is nullable there should be no spaces before `?`
 * 7. There should be no space before `[`
 * 8. There should be no space between a method or constructor name (both at declaration and at call site) and a parenthesis.
 * 9. There should be no space after `(`, `[` and `<` (in templates); no space before `)`, `]`, `>` (in templates)
 * 10. There should be no spaces between prefix/postfix operator (like `!!` or `++`) and it's operand
 */
@Suppress("ForbiddenComment")
class WhiteSpaceRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(WRONG_WHITESPACE, LONG_LINE, WRONG_INDENTATION)
) {
    private val configuration by lazy {
        LineLength.LineLengthConfiguration(
            configRules.getRuleConfig(LONG_LINE)?.configuration ?: emptyMap()
        )
    }
    private lateinit var positionByOffset: (Int) -> Pair<Int, Int>
    @Suppress("ComplexMethod")
    override fun logic(node: ASTNode) {
        when (node.elementType) {
            // keywords
            CONSTRUCTOR_KEYWORD -> handleConstructor(node)
            in keywordsWithSpaceAfter -> handleKeywordWithParOrBrace(node)
            // operators and operator-like symbols
            DOT, ARROW, SAFE_ACCESS, EQ -> handleBinaryOperator(node)
            OPERATION_REFERENCE -> handleOperator(node)
            COLON -> handleColon(node)
            COLONCOLON -> handleColonColon(node)
            COMMA, SEMICOLON -> handleToken(node, 0, 1)
            QUEST -> if (node.treeParent.elementType == NULLABLE_TYPE) {
                handleToken(node, 0, null)
            }
            // braces and other symbols
            LBRACE -> handleLbrace(node)
            RBRACE -> handleRbrace(node)
            LBRACKET -> handleLbracket(node)
            LPAR -> handleLpar(node)
            RPAR, RBRACKET -> handleToken(node, 0, null)
            GT, LT -> handleGtOrLt(node)
            // white space
            WHITE_SPACE -> handleEolWhiteSpace(node)
            else -> {
            }
        }
    }

    private fun handleLbracket(node: ASTNode) =
        if (node.treeParent.elementType == COLLECTION_LITERAL_EXPRESSION) {
            handleToken(node, 1, 0)
        } else {
            handleToken(node, 0, 0)
        }

    private fun handleConstructor(node: ASTNode) {
        if (node.treeNext.numWhiteSpaces()?.let { it > 0 } == true) {
            // there is either whitespace or newline after constructor keyword
            WRONG_WHITESPACE.warnAndFix(configRules, emitWarn, isFixMode, "keyword '${node.text}' should not be separated from " +
                    "'(' with a whitespace", node.startOffset, node) {
                node.treeParent.removeChild(node.treeNext)
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleKeywordWithParOrBrace(node: ASTNode) {
        if (node.treeNext.numWhiteSpaces() != 1) {
            // there is either not single whitespace or newline after keyword
            val nextCodeLeaf = node.nextCodeLeaf()!!
            if (nextCodeLeaf.elementType != LPAR && nextCodeLeaf.elementType != LBRACE && node.treeNext.textContains('\n')) {
                // statement after keyword doesn't have braces and starts at new line, e.g. `if (condition) foo()\n else\n bar()`
                return
            }
            WRONG_WHITESPACE.warnAndFix(configRules, emitWarn, isFixMode, "keyword '${node.text}' should be separated from " +
                    "'${nextCodeLeaf.text}' with a whitespace", nextCodeLeaf.startOffset, nextCodeLeaf) {
                node.leaveSingleWhiteSpace()
            }
        }
    }

    private fun handleRbrace(node: ASTNode) {
        if (node.treeParent.elementType == FUNCTION_LITERAL &&
                !node.treePrev.isWhiteSpace() &&
                node.treePrev.elementType == BLOCK &&
                node.treePrev.text.isNotEmpty()) {
            WRONG_WHITESPACE.warnAndFix(configRules, emitWarn, isFixMode, "there should be a whitespace before }", node.startOffset, node) {
                node.treeParent.addChild(PsiWhiteSpaceImpl(" "), node)
            }
        }
    }

    /**
     * This method covers all other opening braces, not covered in [handleKeywordWithParOrBrace].
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun handleLbrace(node: ASTNode) {
        // `{` can't be the very first symbol in the file, so `!!` should be safe
        val whitespaceOrPrevNode = node.selfOrParentsTreePrev()!!
        val isFromLambdaAsArgument = node
            .parents()
            .take(NUM_PARENTS_FOR_LAMBDA)
            .toList()
            .takeIf { it.size == NUM_PARENTS_FOR_LAMBDA }
            ?.let {
                it[0].elementType == FUNCTION_LITERAL &&
                        it[1].elementType == LAMBDA_EXPRESSION &&
                        it[2].elementType == VALUE_ARGUMENT &&
                        // lambda is not passed as a named argument
                        !it[2].hasChildOfType(EQ)
            }
            ?: false

        val prevNode = whitespaceOrPrevNode.let { if (it.elementType == WHITE_SPACE) it.treePrev else it }
        val numWhiteSpace = whitespaceOrPrevNode.numWhiteSpaces()
        handleWhiteSpaceBeforeLeftBrace(node, isFromLambdaAsArgument, numWhiteSpace, whitespaceOrPrevNode, prevNode)
        handleWhiteSpaceAfterLeftBrace(node)
    }

    private fun handleWhiteSpaceBeforeLeftBrace(node: ASTNode,
                                                isFromLambdaAsArgument: Boolean,
                                                numWhiteSpace: Int?,
                                                whitespaceOrPrevNode: ASTNode,
                                                prevNode: ASTNode
    ) {
        // note: the conditions in the following `if`s cannot be collapsed into simple conjunctions
        if (isFromLambdaAsArgument) {
            @Suppress("PARAMETER_NAME_IN_OUTER_LAMBDA")
            val isFirstArgument = node
                .parent { it.elementType == VALUE_ARGUMENT }
                .let { it?.prevSibling { prevNode -> prevNode.elementType == COMMA } == null }

            // Handling this case: `foo({ it.bar() }, 2, 3)`
            if (numWhiteSpace != 0 && isFirstArgument) {
                WRONG_WHITESPACE.warnAndFix(configRules, emitWarn, isFixMode, "there should be no whitespace before '{' of lambda" +
                        " inside argument list", node.startOffset, node) {
                    whitespaceOrPrevNode.treeParent.removeChild(whitespaceOrPrevNode)
                }
            }
        } else if (prevNode.elementType !in keywordsWithSpaceAfter && numWhiteSpace != 1) {
            val hasOnlyWhiteSpaceBefore = whitespaceOrPrevNode.elementType == WHITE_SPACE && whitespaceOrPrevNode.textContains('\n')
            if (!hasOnlyWhiteSpaceBefore) {
                WRONG_WHITESPACE.warnAndFix(configRules, emitWarn, isFixMode, "there should be a whitespace before '{'", node.startOffset, node) {
                    prevNode.leaveSingleWhiteSpace()
                }
            }
        }
    }

    private fun handleWhiteSpaceAfterLeftBrace(node: ASTNode) {
        if (node.treeParent.elementType == FUNCTION_LITERAL && !node.treeNext.isWhiteSpace() &&
                node.treeNext.elementType == BLOCK && node.treeNext.text.isNotEmpty()) {
            WRONG_WHITESPACE.warnAndFix(configRules, emitWarn, isFixMode, "there should be a whitespace after {", node.startOffset, node) {
                node.treeParent.addChild(PsiWhiteSpaceImpl(" "), node.treeNext)
            }
        }
    }

    private fun handleColonColon(node: ASTNode) {
        if (node.treeParent.elementType == CALLABLE_REFERENCE_EXPRESSION) {
            if (node.treeParent.firstChildNode != node) {
                // callable reference has receiver and shouldn't have any spaces around
                handleBinaryOperator(node)
            } else {
                // callable reference doesn't have receiver, it should stick to reference name and spaces before are determined
                // by other cases of this rule
                handleToken(node, null, 0)
            }
        }
    }

    private fun handleColon(node: ASTNode) {
        when (node.treeParent.elementType) {
            CLASS, SECONDARY_CONSTRUCTOR, TYPE_CONSTRAINT, TYPE_PARAMETER, OBJECT_DECLARATION -> handleBinaryOperator(node)
            VALUE_PARAMETER, PROPERTY, FUN -> handleToken(node, 0, 1)
            ANNOTATION_ENTRY -> handleToken(node, 0, 0)  // e.g. @param:JsonProperty
            // fixme: find examples or delete this line
            else -> log.warn { "Colon with treeParent.elementType=${node.treeParent.elementType}, not handled by WhiteSpaceRule" }
        }
    }

    private fun handleOperator(node: ASTNode) {
        when (node.treeParent.psi) {
            is KtPrefixExpression -> handleToken(node, null, 0)
            is KtPostfixExpression -> handleToken(node, 0, null)
            is KtBinaryExpression -> handleBinaryOperator(node)
            else -> {
            }
        }
    }

    private fun handleBinaryOperator(node: ASTNode) {
        val operatorNode = if (node.elementType == OPERATION_REFERENCE) node.firstChildNode else node
        if (node.elementType == EQ && node.treeParent.elementType == OPERATION_REFERENCE) {
            return
        }
        if (node.elementType == OPERATION_REFERENCE && node.treeParent.elementType.let { it == BINARY_EXPRESSION || it == POSTFIX_EXPRESSION || it == PROPERTY } ||
                node.elementType != OPERATION_REFERENCE) {
            val requiredNumSpaces = if (operatorNode.elementType in operatorsWithNoWhitespace) 0 else 1
            handleToken(node, requiredNumSpaces, requiredNumSpaces)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleToken(
        node: ASTNode,
        requiredSpacesBefore: Int?,
        requiredSpacesAfter: Int?
    ) {
        require(requiredSpacesBefore != null || requiredSpacesAfter != null) {
            "requiredSpacesBefore=$requiredSpacesBefore and requiredSpacesAfter=$requiredSpacesAfter, but at least one should not be null"
        }
        val spacesBefore = node.selfOrParentsTreePrev()!!.numWhiteSpaces()
        // calculate actual spaces after but only if requiredSpacesAfter is not null, otherwise we won't check it
        val spacesAfter = requiredSpacesAfter?.let { _ ->
            // for `!!` and possibly other postfix expressions treeNext and treeParent.treeNext can be null
            // upper levels are already outside of the expression this token belongs to, so we won't check them
            (node.treeNext
                ?: node.treeParent.treeNext)
                ?.numWhiteSpaces()
        }
        val isErrorBefore = requiredSpacesBefore != null && spacesBefore != null && spacesBefore != requiredSpacesBefore
        val isErrorAfter = requiredSpacesAfter != null && spacesAfter != null && spacesAfter != requiredSpacesAfter
        if (isErrorBefore || isErrorAfter) {
            val freeText = "${node.text} should have" +
                    getDescription(requiredSpacesBefore != null, requiredSpacesAfter != null, requiredSpacesBefore, requiredSpacesAfter) +
                    ", but has" +
                    getDescription(isErrorBefore, isErrorAfter, spacesBefore, spacesAfter)
            WRONG_WHITESPACE.warnAndFix(configRules, emitWarn, isFixMode, freeText, node.startOffset, node) {
                node.fixSpaceAround(requiredSpacesBefore, requiredSpacesAfter)
            }
        }
    }

    private fun handleEolWhiteSpace(node: ASTNode) {
        val hasSpaces = node.text.substringBefore('\n').contains(' ')
        // the second condition corresponds to the last line of file
        val isEol = node.textContains('\n') || node.psi.parentsWithSelf.all { it.nextSibling == null }
        if (hasSpaces && isEol) {
            WRONG_WHITESPACE.warnAndFix(configRules, emitWarn, isFixMode, "there should be no spaces in the end of line", node.startOffset, node) {
                (node as LeafElement).rawReplaceWithText(node.text.trimStart(' '))
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleLpar(node: ASTNode) {
        when {
            node.treeParent.treeParent.elementType == SECONDARY_CONSTRUCTOR -> {
                // there is separate handler for 'constructor' keyword to provide custom warning message
                return
            }
            node.nextCodeLeaf()!!.elementType == LBRACE -> {
                // there is separate handler for lambda expression inside parenthesis
                return
            }
            node.treeParent.treeParent.elementType == ANNOTATION_ENTRY ->
                handleToken(node.treeParent, 0, null)
            else -> {
            }
        }
        val isDeclaration = node.treeParent.elementType == VALUE_PARAMETER_LIST && node.treeParent
            .treeParent
            .elementType
            .let {
                it == PRIMARY_CONSTRUCTOR || it == FUN || it == CALL_EXPRESSION
            }
        val isCall = node.treeParent.elementType == VALUE_ARGUMENT_LIST && node.treeParent
            .treeParent
            .elementType
            .let {
                it == CONSTRUCTOR_DELEGATION_CALL || it == CALL_EXPRESSION
            }
        if (isDeclaration || isCall) {
            handleToken(node, 0, 0)
        } else {
            handleToken(node, null, 0)
        }
    }

    private fun handleGtOrLt(node: ASTNode) {
        if (node.treeParent == TYPE_PARAMETER_LIST) {
            handleToken(
                node,
                if (node.elementType == GT) 0 else null,
                if (node.elementType == GT) null else 0
            )
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun ASTNode.isNeedNewLineInOperatorReferences(): Boolean {
        positionByOffset = this.findParentNodeWithSpecificType(KtFileElementType.INSTANCE)!!.calculateLineColByOffset()
        val offset = positionByOffset(this.startOffset).second
        return offset + this.text.length >= configuration.lineLength
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun ASTNode.fixSpaceAround(requiredSpacesBefore: Int?, requiredSpacesAfter: Int?) {
        if (requiredSpacesBefore == 1) {
            selfOrParentsTreePrev()?.let { if (it.elementType == WHITE_SPACE) it.treePrev else it }?.leaveSingleWhiteSpace()
            if (this.isNeedNewLineInOperatorReferences() && this.firstChildNode.elementType == ELVIS) {
                this.treePrev.let { this.treeParent.appendNewlineMergingWhiteSpace(it, it) }
            }
        } else if (requiredSpacesBefore == 0) {
            selfOrParentsTreePrev()?.removeIfWhiteSpace()
        }
        if (requiredSpacesAfter == 1) {
            leaveSingleWhiteSpace()
            if (this.isNeedNewLineInOperatorReferences() && this.firstChildNode.elementType != ELVIS) {
                this.treeNext.let { this.treeParent.appendNewlineMergingWhiteSpace(it, it) }
            }
        } else if (requiredSpacesAfter == 0) {
            // for `!!` and possibly other postfix expressions treeNext can be null
            (treeNext ?: treeParent.treeNext).removeIfWhiteSpace()
        }
    }

    /**
     * Function that returns `treePrev` of this node, or if this.treePrev is null, `treePrev` of first parent node that has it
     */
    private fun ASTNode.selfOrParentsTreePrev() = parent(false) { it.treePrev != null }?.treePrev

    /**
     * This method counts spaces in this node. Null is returned in following cases:
     * * if it is WHITE_SPACE with a newline
     * * if the next node is a comment, because spaces around comments are checked elsewhere.
     *
     * If this node is not a WHITE_SPACE, 0 is returned because there are zero white spaces.
     */
    private fun ASTNode.numWhiteSpaces(): Int? = if (elementType != WHITE_SPACE) {
        0
    } else {
        // this can happen, e.g. in lambdas after an arrow, where block can be not surrounded by braces
        // treeNext may not have children ( {_, _ -> })
        val isBlockStartingWithComment = treeNext.elementType == BLOCK && treeNext.firstChildNode?.isPartOfComment() == true
        if (textContains('\n') || treeNext.isPartOfComment() || isBlockStartingWithComment) null else text.count { it == ' ' }
    }

    private fun ASTNode.leaveSingleWhiteSpace() {
        if (treeNext.elementType == WHITE_SPACE) {
            (treeNext as LeafElement).rawReplaceWithText(" ")
        } else {
            treeParent.addChild(PsiWhiteSpaceImpl(" "), treeNext)
        }
    }

    private fun ASTNode.removeIfWhiteSpace() = takeIf { it.elementType == WHITE_SPACE && !it.textContains('\n') }
        ?.let { it.treeParent.removeChild(it) }

    private fun getDescription(shouldBefore: Boolean,
                               shouldAfter: Boolean,
                               before: Int?,
                               after: Int?
    ): String =
        if (shouldBefore && shouldAfter) {
            " $before space(s) before and $after space(s) after"
        } else if (shouldBefore && !shouldAfter) {
            " $before space(s) before"
        } else if (shouldAfter) {
            " $after space(s) after"
        } else {
            ""
        }

    companion object {
        private val log = KotlinLogging.logger {}
        const val NAME_ID = "horizontal-whitespace"

        // this is the number of parent nodes needed to check if this node is lambda from argument list
        private const val NUM_PARENTS_FOR_LAMBDA = 3
        private val keywordsWithSpaceAfter = TokenSet.create(
            // these keywords are followed by {
            ELSE_KEYWORD, TRY_KEYWORD, DO_KEYWORD, FINALLY_KEYWORD, INIT_KEYWORD,
            // these keywords are followed by (
            FOR_KEYWORD, IF_KEYWORD, WHILE_KEYWORD, CATCH_KEYWORD,
            // these keywords can be followed by either { or (
            WHEN_KEYWORD
        )
        val operatorsWithNoWhitespace = TokenSet.create(DOT, RANGE, COLONCOLON, SAFE_ACCESS, EXCLEXCL)
    }
}
