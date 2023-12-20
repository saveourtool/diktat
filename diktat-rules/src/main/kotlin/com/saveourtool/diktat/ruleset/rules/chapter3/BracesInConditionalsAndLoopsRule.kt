package com.saveourtool.diktat.ruleset.rules.chapter3

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.NO_BRACES_IN_CONDITIONALS_AND_LOOPS
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.findChildrenMatching
import com.saveourtool.diktat.ruleset.utils.isPartOfComment
import com.saveourtool.diktat.ruleset.utils.isSingleLineIfElse
import com.saveourtool.diktat.ruleset.utils.loopType
import com.saveourtool.diktat.ruleset.utils.prevSibling

import org.jetbrains.kotlin.KtNodeTypes.BLOCK
import org.jetbrains.kotlin.KtNodeTypes.BLOCK_CODE_FRAGMENT
import org.jetbrains.kotlin.KtNodeTypes.BODY
import org.jetbrains.kotlin.KtNodeTypes.CALL_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.ELSE
import org.jetbrains.kotlin.KtNodeTypes.IF
import org.jetbrains.kotlin.KtNodeTypes.LAMBDA_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.REFERENCE_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.SAFE_ACCESS_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.THEN
import org.jetbrains.kotlin.KtNodeTypes.WHEN
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.lexer.KtTokens.DO_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.ELSE_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.IF_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.LBRACE
import org.jetbrains.kotlin.lexer.KtTokens.RBRACE
import org.jetbrains.kotlin.lexer.KtTokens.WHILE_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtLoopExpression
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.psi.psiUtil.astReplace
import org.jetbrains.kotlin.psi.psiUtil.children

/**
 * Rule that checks that all conditionals and loops have braces.
 */
class BracesInConditionalsAndLoopsRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
) {
    override fun logic(node: ASTNode) {
        when (node.elementType) {
            IF -> checkIfNode(node)
            WHEN -> checkWhenBranches(node)
            in loopType -> checkLoop(node)
            else -> return
        }
    }

    /**
     * Check braces in if-else statements. Check for both IF and ELSE needs to be done in one method to discover single-line if-else statements correctly.
     */
    @Suppress(
        "ForbiddenComment",
        "UnsafeCallOnNullableType",
        "ComplexMethod",
        "TOO_LONG_FUNCTION"
    )
    private fun checkIfNode(node: ASTNode) {
        val ifPsi = node.psi as KtIfExpression
        val thenNode = ifPsi.then?.node
        val elseKeyword = ifPsi.elseKeyword
        val elseNode = ifPsi.`else`?.node
        val indent = node.findIndentBeforeNode()

        if (node.isSingleLineIfElse()) {
            return
        }

        if (thenNode?.elementType != BLOCK) {
            NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnAndFix(configRules, emitWarn, isFixMode, "IF",
                (thenNode?.prevSibling { it.elementType == IF_KEYWORD } ?: node).startOffset, node) {
                thenNode?.run {
                    (psi as KtElement).replaceWithBlock(indent)
                    if (elseNode != null && elseKeyword != null) {
                        node.replaceChild(elseKeyword.prevSibling.node, PsiWhiteSpaceImpl(" "))
                    }
                }
                    ?: run {
                        node.insertEmptyBlockInsideThenNode(indent)
                    }
            }
        }

        if (elseKeyword != null && elseNode?.elementType != IF && elseNode?.elementType != BLOCK) {
            // Looking for scope functions, for which we won't trigger
            val callAndSafeAccessExpressionChildren = elseNode?.findChildrenMatching {
                it.elementType == CALL_EXPRESSION || it.elementType == SAFE_ACCESS_EXPRESSION
            }

            val scopeFunctionChildren = callAndSafeAccessExpressionChildren?.flatMap {
                it.children()
            }?.filter {
                it.elementType == REFERENCE_EXPRESSION
            }

            val isNodeHaveScopeFunctionChildren = scopeFunctionChildren?.any {
                it.text in scopeFunctions
            }
            if (isNodeHaveScopeFunctionChildren == true) {
                return
            }

            NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnAndFix(configRules, emitWarn, isFixMode, "ELSE",
                (elseNode?.treeParent?.prevSibling { it.elementType == ELSE_KEYWORD } ?: node).startOffset, node) {
                elseNode?.run {
                    (psi as KtElement).replaceWithBlock(indent)
                }
                    ?: run {
                        // `else` can have empty body e.g. when there is a semicolon after: `else ;`
                        node.insertEmptyBlockInsideElseNode(indent)
                    }
            }
        }
    }

    private fun ASTNode.insertEmptyBlockInsideThenNode(indent: Int) {
        val ifPsi = psi as KtIfExpression
        val elseKeyword = ifPsi.elseKeyword
        val emptyThenNode = findChildByType(THEN)

        emptyThenNode?.findChildByType(BLOCK_CODE_FRAGMENT) ?: run {
            val whiteSpacesAfterCondition = ifPsi.rightParenthesis?.node?.treeNext

            whiteSpacesAfterCondition?.let {
                replaceChild(it, PsiWhiteSpaceImpl(" "))
            }
            emptyThenNode?.insertEmptyBlock(indent)
            elseKeyword?.let {
                addChild(PsiWhiteSpaceImpl(" "), elseKeyword.node)
            }
        }
    }

    private fun ASTNode.insertEmptyBlockInsideElseNode(indent: Int) {
        val ifPsi = psi as KtIfExpression
        val elseKeyword = ifPsi.elseKeyword
        val emptyElseNode = findChildByType(ELSE)

        emptyElseNode?.findChildByType(BLOCK_CODE_FRAGMENT) ?: run {
            val whiteSpacesAfterElseKeyword = elseKeyword?.node?.treeNext

            whiteSpacesAfterElseKeyword?.let {
                replaceChild(it, PsiWhiteSpaceImpl(" "))
            }
            emptyElseNode?.insertEmptyBlock(indent)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkLoop(node: ASTNode) {
        val loopBody = (node.psi as KtLoopExpression).body
        val loopBodyNode = loopBody?.node

        if (loopBodyNode == null || loopBodyNode.elementType != BLOCK) {
            NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnAndFix(configRules, emitWarn, isFixMode,
                node.elementType.toString(), node.startOffset, node) {
                // fixme proper way to calculate indent? or get step size (instead of hardcoded 4)
                val indent = node.findIndentBeforeNode()

                loopBody?.run {
                    replaceWithBlock(indent)
                }
                    ?: run {
                        // this corresponds to do-while with empty body
                        node.insertEmptyBlockInsideDoWhileNode(indent)
                    }
            }
        }
    }

    private fun ASTNode.insertEmptyBlockInsideDoWhileNode(indent: Int) {
        findChildByType(BODY) ?: run {
            val doKeyword = findChildByType(DO_KEYWORD)
            val whileKeyword = findChildByType(WHILE_KEYWORD)
            val whiteSpacesAfterDoKeyword = doKeyword?.treeNext

            addChild(CompositeElement(BODY), whileKeyword)
            val emptyWhenNode = findChildByType(BODY)

            whiteSpacesAfterDoKeyword?.let {
                replaceChild(it, PsiWhiteSpaceImpl(" "))
            }
            emptyWhenNode?.insertEmptyBlock(indent)
            addChild(PsiWhiteSpaceImpl(" "), whileKeyword)
        }
    }

    private fun ASTNode.findIndentBeforeNode(): Int {
        val isElseIfStatement = treeParent.elementType == ELSE
        val primaryIfNode = if (isElseIfStatement) treeParent.treeParent else this

        val indentNode = if (primaryIfNode.treeParent?.treeParent?.treeParent?.elementType == LAMBDA_EXPRESSION) {
            primaryIfNode.treeParent.prevSibling { it.elementType == WHITE_SPACE }
        } else {
            primaryIfNode.prevSibling { it.elementType == WHITE_SPACE }
        }

        return indentNode
            ?.text
            ?.lines()
            ?.last()
            ?.count { it == ' ' } ?: 0
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkWhenBranches(node: ASTNode) {
        (node.psi as KtWhenExpression)
            .entries
            .asSequence()
            .filter { it.expression != null && it.expression!!.node.elementType == BLOCK }
            .map { it.expression as KtBlockExpression }
            .filter { block ->
                block.statements.size == 1 &&
                        block.findChildrenMatching { it.isPartOfComment() }.isEmpty()
            }
            .forEach { block ->
                NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnAndFix(configRules, emitWarn, isFixMode,
                    "WHEN", block.node.startOffset, block.node) {
                    block.astReplace(block.firstStatement!!.node.psi)
                }
            }
    }

    private fun KtElement.replaceWithBlock(indent: Int) {
        this.astReplace(KtBlockExpression(
            "{\n${" ".repeat(indent + INDENT_STEP)}$text\n${" ".repeat(indent)}}"
        ))
    }

    private fun ASTNode.insertEmptyBlock(indent: Int) {
        val emptyBlock = CompositeElement(BLOCK_CODE_FRAGMENT)
        addChild(emptyBlock, null)
        emptyBlock.addChild(LeafPsiElement(LBRACE, "{"))
        emptyBlock.addChild(PsiWhiteSpaceImpl("\n${" ".repeat(indent)}"))
        emptyBlock.addChild(LeafPsiElement(RBRACE, "}"))
    }

    companion object {
        private const val INDENT_STEP = 4
        const val NAME_ID = "races-rule"
        private val scopeFunctions = listOf("let", "run", "with", "apply", "also")
    }
}
