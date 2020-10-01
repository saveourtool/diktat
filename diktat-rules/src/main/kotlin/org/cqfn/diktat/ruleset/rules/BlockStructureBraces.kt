package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.BODY
import com.pinterest.ktlint.core.ast.ElementType.CATCH
import com.pinterest.ktlint.core.ast.ElementType.CATCH_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.CLASS_INITIALIZER
import com.pinterest.ktlint.core.ast.ElementType.DO_WHILE
import com.pinterest.ktlint.core.ast.ElementType.ELSE
import com.pinterest.ktlint.core.ast.ElementType.ELSE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.FINALLY
import com.pinterest.ktlint.core.ast.ElementType.FINALLY_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.FOR
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.FUNCTION_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.IF
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.OBJECT_DECLARATION
import com.pinterest.ktlint.core.ast.ElementType.RBRACE
import com.pinterest.ktlint.core.ast.ElementType.SECONDARY_CONSTRUCTOR
import com.pinterest.ktlint.core.ast.ElementType.THEN
import com.pinterest.ktlint.core.ast.ElementType.TRY
import com.pinterest.ktlint.core.ast.ElementType.WHEN
import com.pinterest.ktlint.core.ast.ElementType.WHILE
import com.pinterest.ktlint.core.ast.ElementType.WHILE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.isWhiteSpaceWithNewline
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.BRACES_BLOCK_STRUCTURE_ERROR
import org.cqfn.diktat.ruleset.utils.emptyBlockList
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.findLBrace
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.cqfn.diktat.ruleset.utils.isBlockEmpty
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtTryExpression
import java.lang.Exception

class BlockStructureBraces(private val configRules: List<RulesConfig>) : Rule("block-structure") {

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        val configuration = BlockStructureBracesConfiguration(
                configRules.getRuleConfig(BRACES_BLOCK_STRUCTURE_ERROR)?.configuration ?: mapOf()
        )

        when (node.elementType) {
            FUNCTION_LITERAL -> checkLambda(node, configuration)
            CLASS, OBJECT_DECLARATION -> checkClass(node, configuration)
            FUN, CLASS_INITIALIZER, SECONDARY_CONSTRUCTOR -> checkFun(node, configuration)
            IF -> checkIf(node, configuration)
            WHEN -> checkWhen(node, configuration)
            FOR, WHILE, DO_WHILE -> checkLoop(node, configuration)
            TRY -> checkTry(node, configuration)
        }
    }

    private fun checkLambda(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        val isSingleLineLambda = node.text.lines().size == 1
        if (!isSingleLineLambda)
            checkCloseBrace(node, configuration)
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
        var allMiddleSpaceNodes = node.findAllNodesWithSpecificType(CATCH).map { it.treePrev }
        checkMidBrace(allMiddleSpaceNodes, node, CATCH_KEYWORD)
        catchBlocks.forEach {
            checkOpenBraceOnSameLine(it, BLOCK, configuration)
            checkCloseBrace(it.findChildByType(BLOCK)!!, configuration)
        }
        if (finallyBlock != null) {
            checkOpenBraceOnSameLine(finallyBlock, BLOCK, configuration)
            checkCloseBrace(finallyBlock.findChildByType(BLOCK)!!, configuration)
            allMiddleSpaceNodes = node.findAllNodesWithSpecificType(FINALLY).map { it.treePrev }
            checkMidBrace(allMiddleSpaceNodes, node, FINALLY_KEYWORD)
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
        val childrenAfterLBrace =  node.getChildren(null).toList().run { subList(indexOfFirst { it.elementType == LBRACE }, size) }
        if (!emptyBlockList.containsAll(childrenAfterLBrace.distinct().map { it.elementType })) {
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

    private fun checkOpenBraceOnSameLine(node: ASTNode, beforeType: IElementType, configuration: BlockStructureBracesConfiguration) {
        if (!configuration.openBrace) return
        val nodeBefore = node.findChildByType(beforeType)
        val braceSpace = nodeBefore?.treePrev
        if (braceSpace == null || checkBraceNode(braceSpace, true)) {
            BRACES_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "incorrect newline before opening brace",
                    (braceSpace ?: node).startOffset, node) {
                if (braceSpace == null || braceSpace.elementType != WHITE_SPACE) {
                    node.addChild(PsiWhiteSpaceImpl(" "), nodeBefore)
                } else {
                    (braceSpace as LeafPsiElement).replaceWithText(" ")
                }
            }
        }
        checkOpenBraceEndLine(node, beforeType)
    }

    private fun checkOpenBraceEndLine(node: ASTNode, beforeType: IElementType) {
        val newNode = (if (beforeType == THEN || beforeType == ELSE)
            node.findChildByType(beforeType) else node)?.findLBrace()?.treeNext ?: return
        if (checkBraceNode(newNode)) {
            BRACES_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "incorrect same line after opening brace",
                    newNode.startOffset, newNode) {
                if (newNode.elementType != WHITE_SPACE) {
                    node.addChild(PsiWhiteSpaceImpl("\n"), newNode)
                } else {
                    (newNode as LeafPsiElement).replaceWithText("\n")
                }
            }
        }
    }

    private fun checkMidBrace(allMiddleSpace: List<ASTNode>, node: ASTNode, keyword: IElementType) {
        allMiddleSpace.forEach {
            if (checkBraceNode(it, true)) {
                BRACES_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "incorrect new line after closing brace",
                        it.startOffset, it) {
                    if (it.elementType != WHITE_SPACE) {
                        node.addChild(PsiWhiteSpaceImpl(" "), node.findChildByType(keyword))
                    } else {
                        (it as LeafPsiElement).replaceWithText(" ")
                    }
                }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkCloseBrace(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        if (!configuration.closeBrace) return
        val space = node.findChildByType(RBRACE)!!.treePrev
        if (checkBraceNode(space))
            BRACES_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "no newline before closing brace",
                    (space.treeNext ?: node.findChildByType(RBRACE))!!.startOffset, node) {
                if (space.elementType != WHITE_SPACE) {
                    node.addChild(PsiWhiteSpaceImpl("\n"), node.findChildByType(RBRACE))
                } else {
                    (space as LeafPsiElement).replaceWithText("\n")
                }
            }
    }

    private fun checkBraceNode(node: ASTNode, shouldContainNewline: Boolean = false) =
            shouldContainNewline == node.isWhiteSpaceWithNewline()

    class BlockStructureBracesConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val openBrace = config["openBraceNewline"]?.toBoolean() ?: true
        val closeBrace = config["closeBraceNewline"]?.toBoolean() ?: true
    }
}
