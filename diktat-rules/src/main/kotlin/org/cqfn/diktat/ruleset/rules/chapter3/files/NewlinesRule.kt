package org.cqfn.diktat.ruleset.rules.chapter3.files

import com.pinterest.ktlint.core.ast.*
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.ListOfList
import org.cqfn.diktat.ruleset.constants.Warnings.COMPLEX_EXPRESSION
import org.cqfn.diktat.ruleset.constants.Warnings.REDUNDANT_SEMICOLON
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_NEWLINES
import org.cqfn.diktat.ruleset.rules.DiktatRule

import com.pinterest.ktlint.core.ast.ElementType.ANDAND
import com.pinterest.ktlint.core.ast.ElementType.ARROW
import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.CALLABLE_REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.COLON
import com.pinterest.ktlint.core.ast.ElementType.COLONCOLON
import com.pinterest.ktlint.core.ast.ElementType.COMMA
import com.pinterest.ktlint.core.ast.ElementType.CONDITION
import com.pinterest.ktlint.core.ast.ElementType.DIV
import com.pinterest.ktlint.core.ast.ElementType.DIVEQ
import com.pinterest.ktlint.core.ast.ElementType.DOT
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.ELVIS
import com.pinterest.ktlint.core.ast.ElementType.ENUM_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.EQ
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.FUNCTION_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.FUNCTION_TYPE
import com.pinterest.ktlint.core.ast.ElementType.FUNCTION_TYPE_RECEIVER
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.IF
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.KDOC
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
import com.pinterest.ktlint.core.ast.ElementType.POSTFIX_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.PRIMARY_CONSTRUCTOR
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.RETURN
import com.pinterest.ktlint.core.ast.ElementType.RETURN_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.RPAR
import com.pinterest.ktlint.core.ast.ElementType.SAFE_ACCESS
import com.pinterest.ktlint.core.ast.ElementType.SAFE_ACCESS_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.SECONDARY_CONSTRUCTOR
import com.pinterest.ktlint.core.ast.ElementType.SEMICOLON
import com.pinterest.ktlint.core.ast.ElementType.SUPER_TYPE_LIST
import com.pinterest.ktlint.core.ast.ElementType.VALUE_ARGUMENT
import com.pinterest.ktlint.core.ast.ElementType.VALUE_ARGUMENT_LIST
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER_LIST
import com.pinterest.ktlint.core.ast.ElementType.WHEN
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.utils.*
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.incremental.ChangesCollector.Companion.getNonPrivateNames
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParameterList
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtSuperTypeList
import org.jetbrains.kotlin.psi.KtValueArgumentList
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.children
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.siblings
import org.slf4j.LoggerFactory

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
 * 9. parameter or argument lists and supertype lists that have more than 2 elements should be separated by newlines
 * 10. Complex expression inside condition replaced with new variable
 */
@Suppress("ForbiddenComment")
class NewlinesRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(COMPLEX_EXPRESSION, REDUNDANT_SEMICOLON, WRONG_NEWLINES)
) {
    private val configuration by lazy {
        NewlinesRuleConfiguration(configRules.getRuleConfig(WRONG_NEWLINES)?.configuration ?: emptyMap())
    }

    override fun logic(node: ASTNode) {
        when (node.elementType) {
            SEMICOLON -> handleSemicolon(node)
            OPERATION_REFERENCE, EQ -> handleOperatorWithLineBreakAfter(node)
            in lineBreakBeforeOperators -> handleOperatorWithLineBreakBefore(node)
            LPAR -> handleOpeningParentheses(node)
            COMMA -> handleComma(node)
            COLON -> handleColon(node)
            BLOCK -> handleLambdaBody(node)
            RETURN -> handleReturnStatement(node)
            SUPER_TYPE_LIST, VALUE_PARAMETER_LIST, VALUE_ARGUMENT_LIST -> handleList(node)
            DOT_QUALIFIED_EXPRESSION, SAFE_ACCESS_EXPRESSION, POSTFIX_EXPRESSION -> handDotQualifiedExpression(node)
            else -> {
            }
        }
    }
    private fun handDotQualifiedExpression(node: ASTNode) {
        val list: MutableList<ASTNode> = mutableListOf()
        searchDot(node, list)
        if (list.size > 3) {
            WRONG_NEWLINES.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
                fixDotQualifiedExpression(list)
            }
        }
    }

    private fun fixDotQualifiedExpression(list: MutableList<ASTNode>) {
        list.forEachIndexed { index, astNode ->
            if (index>0) {
                val dotNode = astNode.getFirstChildWithType(DOT) ?: astNode.getFirstChildWithType(SAFE_ACCESS)
                val nodeBeforeDot = dotNode?.treePrev
                astNode.appendNewlineMergingWhiteSpace(nodeBeforeDot, dotNode)
            }
        }
    }

    private fun searchDot(node: ASTNode, dotList: MutableList<ASTNode>) {
        if (node.elementType == DOT_QUALIFIED_EXPRESSION || node.elementType == SAFE_ACCESS_EXPRESSION || node.elementType == POSTFIX_EXPRESSION) {
            node.getChildren(null)
                .filter {
                    it.elementType == DOT_QUALIFIED_EXPRESSION || it.elementType == SAFE_ACCESS_EXPRESSION || it.elementType == POSTFIX_EXPRESSION
                }
                .forEach {
                    searchDot(it, dotList)
                }
            if (node.elementType != POSTFIX_EXPRESSION) {
                dotList.add(node)
            }
        }
    }

    /**
     * Check that EOL semicolon is used only in enums
     */
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
        if (node.elementType != EQ && node.firstChildNode.elementType !in lineBreakAfterOperators && !node.isInfixCall()) {
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

    @Suppress("ComplexMethod", "TOO_LONG_FUNCTION")
    private fun handleOperatorWithLineBreakBefore(node: ASTNode) {
        if (node.isDotFromPackageOrImport()) {
            return
        }
        val isIncorrect = (if (node.elementType == ELVIS) node.treeParent else node).run {
            if (isInvalidCallsChain(dropLeadingProperties = true)) {
                if (node.isInParentheses()) {
                    checkForComplexExpression(node)
                }
                val isSingleLineIfElse = parent({ it.elementType == IF }, true)?.isSingleLineIfElse() ?: false
                // to follow functional style these operators should be started by newline
                (isFollowedByNewline() || !isBeginByNewline()) && !isSingleLineIfElse &&
                        (!isFirstCall() || !isMultilineLambda(treeParent))
            } else {
                if (isInvalidCallsChain(dropLeadingProperties = false) && node.isInParentheses()) {
                    checkForComplexExpression(node)
                }
                // unless statement is simple and on single line, these operators cannot have newline after
                isFollowedByNewline() && !isSingleDotStatementOnSingleLine()
            }
        }
        if (isIncorrect || node.isElvisCorrect()) {
            val freeText = if (node.isInvalidCallsChain() || node.isElvisCorrect()) {
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

    private fun checkForComplexExpression(node: ASTNode) {
        if (node.getRootNode().getFilePath().isGradleScript()) {
            // this inspection is softened for gradle scripts, see https://github.com/analysis-dev/diktat/issues/1148
            return
        }
        COMPLEX_EXPRESSION.warn(configRules, emitWarn, isFixMode, node.text, node.startOffset, node)
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

    /**
     * Check that newline is not placed before a comma
     */
    private fun handleComma(node: ASTNode) {
        val prevNewLine = node
            .parent({ it.treePrev != null }, strict = false)
            ?.treePrev
            ?.takeIf {
                it.elementType == WHITE_SPACE && it.text.contains("\n")
            }
        prevNewLine?.let {
            WRONG_NEWLINES.warnAndFix(configRules, emitWarn, isFixMode, "newline should be placed only after comma", node.startOffset, node) {
                it.treeParent.removeChild(it)
            }
        }
    }

    /**
     * Check that newline is not placed before or after a colon
     */
    private fun handleColon(node: ASTNode) {
        val prevNewLine = node
            .parent({ it.treePrev != null }, strict = false)
            ?.treePrev
            ?.takeIf {
                it.elementType == WHITE_SPACE && it.text.contains("\n")
            }
        prevNewLine?.let { whiteSpace ->
            WRONG_NEWLINES.warnAndFix(configRules, emitWarn, isFixMode, "newline shouldn't be placed before colon", node.startOffset, node) {
                whiteSpace.treeParent.removeChild(whiteSpace)

                if (node.hasParent(VALUE_PARAMETER_LIST)) {
                    // If inside the list of arguments the rule for a new line before the colon has worked,
                    // then we delete the new line on both sides of the colon
                    whiteSpace.treeParent?.let {
                        val nextNewLine = node
                            .parent({ it.treeNext != null }, strict = false)
                            ?.treeNext
                            ?.takeIf {
                                it.elementType == WHITE_SPACE && it.text.contains("\n")
                            }
                        nextNewLine?.let {
                            it.treeParent.addChild(PsiWhiteSpaceImpl(" "), it)
                            it.treeParent.removeChild(it)
                        }
                    }
                }
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
                    .filter { it.isNewLineNode() }
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

    @Suppress("UnsafeCallOnNullableType", "AVOID_NULL_CHECKS")
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
                    val returnType = (funNode.psi as? KtNamedFunction)?.typeReference?.node
                    val expression = node.findChildByType(RETURN_KEYWORD)!!.nextCodeSibling()!!
                    val blockNode = funNode.findChildByType(BLOCK)
                    funNode.apply {
                        if (returnType != null) {
                            removeRange(returnType.treeNext, null)
                            addChild(PsiWhiteSpaceImpl(" "), null)
                        } else if (blockNode != null) {
                            removeChild(blockNode)
                        }
                        addChild(LeafPsiElement(EQ, "="), null)
                        addChild(PsiWhiteSpaceImpl(" "), null)
                        addChild(expression.clone() as ASTNode, null)
                    }
                }
            }
    }

    /**
     * Checks that members of [VALUE_PARAMETER_LIST] (list of function parameters at declaration site) are separated with newlines.
     * Also checks that entries of [SUPER_TYPE_LIST] are separated by newlines.
     */
    @Suppress("ComplexMethod")
    private fun handleList(node: ASTNode) {
        if (node.elementType == VALUE_PARAMETER_LIST) {
            // do not check list parameter in lambda
            node.findParentNodeWithSpecificType(LAMBDA_ARGUMENT)?.let {
                return
            }
            // do not check other value lists
            if (node.treeParent.elementType.let { it == FUNCTION_TYPE || it == FUNCTION_TYPE_RECEIVER }) {
                return
            }
        }

        if (node.elementType == VALUE_ARGUMENT_LIST && node.siblings(forward = false).any { it.elementType == REFERENCE_EXPRESSION }) {
            // check that it is not function invocation, but only supertype constructor calls
            return
        }

        val (numEntries, entryType) = when (node.elementType) {
            VALUE_PARAMETER_LIST -> (node.psi as KtParameterList).parameters.size to "value parameters"
            SUPER_TYPE_LIST -> (node.psi as KtSuperTypeList).entries.size to "supertype list entries"
            VALUE_ARGUMENT_LIST -> (node.psi as KtValueArgumentList).arguments.size to "value arguments"
            else -> {
                log.warn("Unexpected node element type ${node.elementType}")
                return
            }
        }
        if (numEntries > configuration.maxParametersInOneLine) {
            when (node.elementType) {
                VALUE_PARAMETER_LIST -> handleFirstValue(node, VALUE_PARAMETER, "first parameter should be placed on a separate line " +
                        "or all other parameters should be aligned with it in declaration of <${node.getParentIdentifier()}>")
                VALUE_ARGUMENT_LIST -> handleFirstValue(node, VALUE_ARGUMENT, "first value argument (%s) should be placed on the new line " +
                        "or all other parameters should be aligned with it")
                else -> {
                }
            }

            handleValueParameterList(node, entryType)
        }
    }

    private fun handleFirstValue(node: ASTNode,
                                 filterType: IElementType,
                                 warnText: String
    ) = node
        .children()
        .takeWhile { !it.textContains('\n') }
        .filter { it.elementType == filterType }
        .toList()
        .takeIf { it.size > 1 }
        ?.let { list ->
            val freeText = if (filterType == VALUE_ARGUMENT) {
                warnText.format(list.first().text)
            } else {
                warnText
            }
            WRONG_NEWLINES.warnAndFix(configRules, emitWarn, isFixMode, freeText, node.startOffset, node) {
                list.first().treePrev?.let {
                    node.appendNewlineMergingWhiteSpace(
                        list.first()
                            .treePrev
                            .takeIf { it.elementType == WHITE_SPACE },
                        list.first()
                    )
                }
            }
        }

    @Suppress("AVOID_NULL_CHECKS")
    private fun handleValueParameterList(node: ASTNode, entryType: String) = node
        .children()
        .filter {
            (it.elementType == COMMA && !it.treeNext.isNewLineNode()) ||
                    // Move RPAR to the new line
                    (it.elementType == RPAR && it.treePrev.elementType != COMMA && !it.treePrev.isNewLineNode())
        }
        .toList()
        .takeIf { it.isNotEmpty() }
        ?.let { invalidCommas ->
            val warnText = if (node.getParentIdentifier() != null) {
                "$entryType should be placed on different lines in declaration of <${node.getParentIdentifier()}>"
            } else {
                "$entryType should be placed on different lines"
            }
            WRONG_NEWLINES.warnAndFix(configRules, emitWarn, isFixMode,
                warnText, node.startOffset, node) {
                invalidCommas.forEach { commaOrRpar ->
                    val nextWhiteSpace = commaOrRpar.treeNext?.takeIf { it.elementType == WHITE_SPACE }
                    if (commaOrRpar.elementType == COMMA) {
                        nextWhiteSpace?.treeNext?.let {
                            commaOrRpar.appendNewlineMergingWhiteSpace(nextWhiteSpace, nextWhiteSpace.treeNext)
                        } ?: commaOrRpar.treeNext?.treeParent?.appendNewlineMergingWhiteSpace(nextWhiteSpace, commaOrRpar.treeNext)
                    } else {
                        commaOrRpar.treeParent.appendNewlineMergingWhiteSpace(nextWhiteSpace, commaOrRpar)
                    }
                }
            }
        }

    private fun ASTNode.isNewLineNode(): Boolean = this.run { elementType == WHITE_SPACE && textContains('\n') }

    @Suppress("UnsafeCallOnNullableType")
    private fun ASTNode.getParentIdentifier() = when (treeParent.elementType) {
        PRIMARY_CONSTRUCTOR -> treeParent.treeParent
        SECONDARY_CONSTRUCTOR -> parent(CLASS)!!
        else -> treeParent
    }
        .getIdentifierName()?.text

    private fun ASTNode.getOrderedCallExpressions(psi: PsiElement, result: MutableList<ASTNode>) {
        // if statements here have the only right order - don't change it

        if (psi.children.isNotEmpty() && !psi.isFirstChildElementType(DOT_QUALIFIED_EXPRESSION) &&
                !psi.isFirstChildElementType(SAFE_ACCESS_EXPRESSION)) {
            val firstChild = psi.firstChild
            if (firstChild.isFirstChildElementType(DOT_QUALIFIED_EXPRESSION) ||
                    firstChild.isFirstChildElementType(SAFE_ACCESS_EXPRESSION)) {
                getOrderedCallExpressions(firstChild.firstChild, result)
            }
            result.add(firstChild.node
                .siblings(true)
                .dropWhile { it.elementType in dropChainValues }
                .first()  // node treeNext is ".", "?.", "!!", "::"
            )
        } else if (psi.children.isNotEmpty()) {
            getOrderedCallExpressions(psi.firstChild, result)

            result.add(psi.firstChild
                .node
                .siblings(true)
                .dropWhile { it.elementType in dropChainValues }
                .first()  // node treeNext is ".", "?.", "!!", "::"
            )
        }
    }

    private fun KtBinaryExpression.dotCalls(right: Boolean = true) = (if (right) this.right else this.left)
        ?.node
        ?.takeIf { it.elementType == DOT_QUALIFIED_EXPRESSION }
        ?.findChildByType(DOT)
        ?.getCallChain()

    private fun ASTNode.isElvisCorrect(): Boolean {
        if (this.elementType != ELVIS) {
            return false
        }
        val binaryExpression = (this.treeParent.treeParent.psi as KtBinaryExpression)
        val leftDotCalls = binaryExpression.dotCalls(false)
        val rightDotCalls = binaryExpression.dotCalls()
        return (leftDotCalls?.size ?: 0) + (rightDotCalls?.size ?: 0) > configuration.maxCallsInOneLine && !this.isBeginByNewline()
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

    private fun PsiElement.isFirstChildElementType(elementType: IElementType) =
            this.firstChild.node.elementType == elementType

    /**
     * This method collects chain calls and checks it
     *
     * @return true - if there is error, and false if there is no error
     */
    private fun ASTNode.isInvalidCallsChain(dropLeadingProperties: Boolean = true) = getCallChain(dropLeadingProperties)?.isNotValidCalls(this) ?: false

    private fun ASTNode.getCallChain(dropLeadingProperties: Boolean = true): List<ASTNode>? {
        val parentExpressionList = getParentExpressions()
            .lastOrNull()
            ?.run {
                mutableListOf<ASTNode>().also {
                    getOrderedCallExpressions(psi, it)
                }
            }
        return if (dropLeadingProperties) {
            // fixme: we can't distinguish fully qualified names (like java.lang) from chain of property accesses (like list.size) for now
            parentExpressionList?.dropWhile { !it.treeParent.textContains('(') && !it.treeParent.textContains('{') }
        } else {
            parentExpressionList
        }
    }

    private fun List<ASTNode>.isNotValidCalls(node: ASTNode): Boolean {
        if (this.size == 1) {
            return false
        }
        val callsByNewLine: ListOfList = mutableListOf()
        var callsInOneNewLine: MutableList<ASTNode> = mutableListOf()
        this.forEach { astNode ->
            if (astNode.treePrev.isFollowedByNewline() || astNode.treePrev.isWhiteSpaceWithNewline()) {
                callsByNewLine.add(callsInOneNewLine)
                callsInOneNewLine = mutableListOf()
                callsInOneNewLine.add(astNode)
            } else {
                callsInOneNewLine.add(astNode)
            }
            if (astNode.treePrev.elementType == POSTFIX_EXPRESSION && !astNode.treePrev.isFollowedByNewline() && configuration.maxCallsInOneLine == 1) {
                return true
            }
        }
        callsByNewLine.add(callsInOneNewLine)
        return (callsByNewLine.find { it.contains(node) } ?: return false)
            .indexOf(node) + 1 > configuration.maxCallsInOneLine
    }

    /**
     *  taking all expressions inside complex expression until we reach lambda arguments
     */
    private fun ASTNode.getParentExpressions() =
            parents().takeWhile { it.elementType in chainExpressionTypes && it.elementType != LAMBDA_ARGUMENT }

    private fun isMultilineLambda(node: ASTNode): Boolean =
            (node.findAllDescendantsWithSpecificType(LAMBDA_ARGUMENT)
                .firstOrNull()
                ?.text
                ?.count { it == '\n' } ?: -1) > 0

    /**
     * Getting the first call expression in call chain
     */
    private fun ASTNode.isFirstCall() = getParentExpressions()
        .lastOrNull()
        ?.run {
            val firstCallee = mutableListOf<ASTNode>().also {
                getOrderedCallExpressions(psi, it)
            }.first()
            findAllDescendantsWithSpecificType(firstCallee.elementType, false).first() === this@isFirstCall
        } ?: false

    /**
     * This method should be called on OPERATION_REFERENCE in the middle of BINARY_EXPRESSION
     */
    private fun ASTNode.isInfixCall() = elementType == OPERATION_REFERENCE &&
            firstChildNode.elementType == IDENTIFIER &&
            treeParent.elementType == BINARY_EXPRESSION

    /**
     * This method checks that complex expression should be replace with new variable
     */
    private fun ASTNode.isInParentheses() = parent({ it.elementType == DOT_QUALIFIED_EXPRESSION || it.elementType == SAFE_ACCESS_EXPRESSION })
        ?.treeParent
        ?.elementType
        ?.let { it in parenthesesTypes }
        ?: false

    /**
     * [RuleConfiguration] for newlines placement
     */
    private class NewlinesRuleConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * If the number of parameters on one line is more than this threshold, all parameters should be placed on separate lines.
         */
        val maxParametersInOneLine = config["maxParametersInOneLine"]?.toInt() ?: 2
        val maxCallsInOneLine = config["maxCallsInOneLine"]?.toInt() ?: MAX_CALLS_IN_ONE_LINE
    }

    companion object {
        private val log = LoggerFactory.getLogger(NewlinesRule::class.java)
        const val MAX_CALLS_IN_ONE_LINE = 3
        const val NAME_ID = "acr-newlines"

        // fixme: these token sets can be not full, need to add new once as corresponding cases are discovered.
        // error is raised if these operators are prepended by newline
        private val lineBreakAfterOperators = TokenSet.create(ANDAND, OROR, PLUS, PLUSEQ, MINUS, MINUSEQ, MUL, MULTEQ, DIV, DIVEQ)
        // error is raised if these operators are followed by newline

        private val lineBreakBeforeOperators = TokenSet.create(DOT, SAFE_ACCESS, ELVIS, COLONCOLON)
        private val expressionTypes = TokenSet.create(DOT_QUALIFIED_EXPRESSION, SAFE_ACCESS_EXPRESSION, CALLABLE_REFERENCE_EXPRESSION, BINARY_EXPRESSION)
        private val chainExpressionTypes = TokenSet.create(DOT_QUALIFIED_EXPRESSION, SAFE_ACCESS_EXPRESSION)
        private val dropChainValues = TokenSet.create(EOL_COMMENT, WHITE_SPACE, BLOCK_COMMENT, KDOC)
        private val parenthesesTypes = TokenSet.create(CONDITION, WHEN, VALUE_ARGUMENT)
    }
}

/**
 * Checks whether [this] function is recursive, i.e. calls itself inside from it's body
 *
 * @return true if function is recursive, false otherwise
 */
fun KtNamedFunction.isRecursive() = bodyBlockExpression
    ?.statements?.any { statement ->
    statement.anyDescendantOfType<KtReferenceExpression> {
        it.text == this@isRecursive.name
    }
}
    ?: false
