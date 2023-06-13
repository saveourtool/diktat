package com.saveourtool.diktat.ruleset.rules.chapter3

import com.saveourtool.diktat.common.config.rules.RuleConfiguration
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getRuleConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.BRACES_BLOCK_STRUCTURE_ERROR
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.*
import com.saveourtool.diktat.ruleset.utils.isWhiteSpaceWithNewline

import org.jetbrains.kotlin.KtNodeTypes.BLOCK
import org.jetbrains.kotlin.KtNodeTypes.BODY
import org.jetbrains.kotlin.KtNodeTypes.CATCH
import org.jetbrains.kotlin.KtNodeTypes.CLASS
import org.jetbrains.kotlin.KtNodeTypes.CLASS_BODY
import org.jetbrains.kotlin.KtNodeTypes.CLASS_INITIALIZER
import org.jetbrains.kotlin.KtNodeTypes.DO_WHILE
import org.jetbrains.kotlin.KtNodeTypes.ELSE
import org.jetbrains.kotlin.KtNodeTypes.FINALLY
import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.FUNCTION_LITERAL
import org.jetbrains.kotlin.KtNodeTypes.IF
import org.jetbrains.kotlin.KtNodeTypes.LAMBDA_ARGUMENT
import org.jetbrains.kotlin.KtNodeTypes.OBJECT_DECLARATION
import org.jetbrains.kotlin.KtNodeTypes.SECONDARY_CONSTRUCTOR
import org.jetbrains.kotlin.KtNodeTypes.THEN
import org.jetbrains.kotlin.KtNodeTypes.TRY
import org.jetbrains.kotlin.KtNodeTypes.WHEN
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.lexer.KtTokens.CATCH_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.ELSE_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.FINALLY_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.LBRACE
import org.jetbrains.kotlin.lexer.KtTokens.RBRACE
import org.jetbrains.kotlin.lexer.KtTokens.WHILE_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtTryExpression

/**
 * This rule checks that *non-empty* code blocks with braces follow the K&R style (1TBS or OTBS style):
 * - The opening brace is on the same same line with the first line of the code block
 * - The closing brace is on it's new line
 * - The closing brace can be followed by a new line. Only exceptions are: `else`, `finally`, `while` (from do-while statement) or `catch` keywords.
 *   These keywords should not be split from the closing brace by a newline.
 * Exceptions:
 * - opening brace of lambda
 * - braces around `else`/`catch`/`finally`/`while` (in `do-while` loop)
 */
class BlockStructureBraces(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(BRACES_BLOCK_STRUCTURE_ERROR),
) {
    override fun logic(node: ASTNode) {
        val configuration = BlockStructureBracesConfiguration(
            configRules.getRuleConfig(BRACES_BLOCK_STRUCTURE_ERROR)?.configuration ?: emptyMap()
        )

        when (node.elementType) {
            FUNCTION_LITERAL -> checkLambda(node, configuration)
            CLASS, OBJECT_DECLARATION -> checkClass(node, configuration)
            FUN, CLASS_INITIALIZER, SECONDARY_CONSTRUCTOR -> checkFun(node, configuration)
            IF -> checkIf(node, configuration)
            WHEN -> checkWhen(node, configuration)
            in loopType -> checkLoop(node, configuration)
            TRY -> checkTry(node, configuration)
            else -> return
        }
    }

    private fun checkLambda(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        val isSingleLineLambda = node.text.lines().size == 1
        if (!isSingleLineLambda) {
            checkCloseBrace(node, configuration)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkClass(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        if (node.hasChildOfType(CLASS_BODY) && !node.findChildByType(CLASS_BODY).isBlockEmpty()) {
            checkOpenBraceOnSameLine(node, CLASS_BODY, configuration)
            checkCloseBrace(node.findChildByType(CLASS_BODY)!!, configuration)
        }
    }

    @Suppress("UnsafeCallOnNullableType")  // `catch` and `finally` clauses should always have body in `{}`, therefore !!
    private fun checkTry(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        val tryBlock = node.psi as KtTryExpression
        val catchBlocks = tryBlock.catchClauses.map { it.node }
        val finallyBlock = tryBlock.finallyBlock?.node
        checkOpenBraceOnSameLine(tryBlock.node, BLOCK, configuration)
        val allMiddleSpaceNodes = node.findAllDescendantsWithSpecificType(CATCH).map { it.treePrev }
        checkMidBrace(allMiddleSpaceNodes, node, CATCH_KEYWORD)
        catchBlocks.forEach {
            checkOpenBraceOnSameLine(it, BLOCK, configuration)
            checkCloseBrace(it.findChildByType(BLOCK)!!, configuration)
        }
        finallyBlock?.let { block ->
            checkOpenBraceOnSameLine(block, BLOCK, configuration)
            checkCloseBrace(block.findChildByType(BLOCK)!!, configuration)
            val newAllMiddleSpaceNodes = node.findAllDescendantsWithSpecificType(FINALLY).map { it.treePrev }
            checkMidBrace(newAllMiddleSpaceNodes, node, FINALLY_KEYWORD)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkLoop(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        node.findChildByType(BODY)?.let {
            if (!it.findChildByType(BLOCK).isBlockEmpty()) {
                checkOpenBraceOnSameLine(node, BODY, configuration)
                // check that there is a `BLOCK` child is done inside `!isBlockEmpty`
                checkCloseBrace(it.findChildByType(BLOCK)!!, configuration)
                if (node.elementType == DO_WHILE) {
                    val allMiddleNode = listOf(node.findChildByType(BODY)!!.treeNext)
                    checkMidBrace(allMiddleNode, node, WHILE_KEYWORD)
                }
            }
        }
    }

    private fun checkWhen(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        /// WHEN expression doesn't contain BLOCK element and LBRECE isn't the first child, so we should to find it.
        val childrenAfterLbrace = node
            .getChildren(null)
            .toList()
            .run { subList(indexOfFirst { it.elementType == LBRACE }, size) }
        if (!emptyBlockList.containsAll(childrenAfterLbrace.distinct().map { it.elementType })) {
            checkOpenBraceOnSameLine(node, LBRACE, configuration)
            checkCloseBrace(node, configuration)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkFun(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        if (!node.findChildByType(BLOCK).isBlockEmpty()) {
            checkOpenBraceOnSameLine(node, BLOCK, configuration)
            checkCloseBrace(node.findChildByType(BLOCK)!!, configuration)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkIf(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        val ifPsi = node.psi as KtIfExpression
        val thenNode = ifPsi.then?.node
        val hasElseBranch = ifPsi.elseKeyword != null
        val elseNode = ifPsi.`else`?.node
        if (thenNode != null && thenNode.hasChildOfType(LBRACE)) {
            checkOpenBraceOnSameLine(node, THEN, configuration)
            checkCloseBrace(thenNode, configuration)
            if (hasElseBranch) {
                // thenNode might have been altered by this point
                val allMiddleNode = listOf(node.findChildByType(THEN)!!.treeNext)
                checkMidBrace(allMiddleNode, node, ELSE_KEYWORD)
            }
        }
        if (hasElseBranch && elseNode != null && elseNode.elementType != IF && elseNode.hasChildOfType(LBRACE)) {
            checkOpenBraceOnSameLine(node, ELSE, configuration)
            checkCloseBrace(elseNode, configuration)
        }
    }

    private fun checkOpenBraceOnSameLine(
        node: ASTNode,
        beforeType: IElementType,
        configuration: BlockStructureBracesConfiguration
    ) {
        if (!configuration.openBrace) {
            return
        }
        val nodeBefore = node.findChildByType(beforeType)
        val braceSpace = nodeBefore?.treePrev
        if (braceSpace == null || checkBraceNode(braceSpace, true)) {
            BRACES_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "incorrect newline before opening brace",
                (braceSpace ?: node).startOffset, node) {
                if (braceSpace == null || braceSpace.elementType != WHITE_SPACE) {
                    node.addChild(PsiWhiteSpaceImpl(" "), nodeBefore)
                } else {
                    if (braceSpace.treePrev.elementType in commentType) {
                        val commentBefore = braceSpace.treePrev
                        if (commentBefore.treePrev.elementType == WHITE_SPACE) {
                            commentBefore.treeParent.removeChild(commentBefore.treePrev)
                        }
                        commentBefore.treeParent.removeChild(commentBefore)
                        node.treeParent.addChild(commentBefore.clone() as ASTNode, node)
                        node.treeParent.addChild(PsiWhiteSpaceImpl("\n"), node)
                    }
                    braceSpace.treeParent.replaceWhiteSpaceText(braceSpace, " ")
                }
            }
        }
        checkOpenBraceEndLine(node, beforeType)
    }

    private fun checkOpenBraceEndLine(node: ASTNode, beforeType: IElementType) {
        val newNode = (if (beforeType == THEN || beforeType == ELSE) node.findChildByType(beforeType) else node)
            ?.findLBrace()
            ?.treeNext
            ?: return
        if (checkBraceNode(newNode)) {
            BRACES_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "incorrect same line after opening brace",
                newNode.startOffset, newNode) {
                if (newNode.elementType != WHITE_SPACE) {
                    newNode.treeParent.addChild(PsiWhiteSpaceImpl("\n"), newNode)
                } else {
                    (newNode as LeafPsiElement).rawReplaceWithText("\n")
                }
            }
        }
    }

    private fun checkMidBrace(
        allMiddleSpace: List<ASTNode>,
        node: ASTNode,
        keyword: IElementType
    ) {
        allMiddleSpace.forEach { space ->
            if (checkBraceNode(space, true)) {
                BRACES_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "incorrect new line after closing brace",
                    space.startOffset, space) {
                    if (space.elementType != WHITE_SPACE) {
                        node.addChild(PsiWhiteSpaceImpl(" "), node.findChildByType(keyword))
                    } else {
                        (space as LeafPsiElement).rawReplaceWithText(" ")
                    }
                }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkCloseBrace(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        if (!configuration.closeBrace) {
            return
        }
        val space = node.findChildByType(RBRACE)!!.treePrev
        node.findParentNodeWithSpecificType(LAMBDA_ARGUMENT)?.let {
            if (space.text.isEmpty()) {
                return
            }
        }
        if (checkBraceNode(space)) {
            BRACES_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "no newline before closing brace",
                (space.treeNext ?: node.findChildByType(RBRACE))!!.startOffset, node) {
                if (space.elementType != WHITE_SPACE) {
                    node.addChild(PsiWhiteSpaceImpl("\n"), node.findChildByType(RBRACE))
                } else {
                    (space as LeafPsiElement).rawReplaceWithText("\n")
                }
            }
        }
    }

    private fun checkBraceNode(node: ASTNode, shouldContainNewline: Boolean = false) =
        shouldContainNewline == node.isWhiteSpaceWithNewline()

    /**
     * Configuration for style of braces in block
     */
    class BlockStructureBracesConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Whether the opening brace should be placed on a new line
         */
        val openBrace = config["openBraceNewline"]?.toBoolean() ?: true

        /**
         * Whether a closing brace should be placed on a new line
         */
        val closeBrace = config["closeBraceNewline"]?.toBoolean() ?: true
    }

    companion object {
        const val NAME_ID = "block-structure"
    }
}
