package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.BODY
import com.pinterest.ktlint.core.ast.ElementType.CATCH
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.CLASS_INITIALIZER
import com.pinterest.ktlint.core.ast.ElementType.DO_WHILE
import com.pinterest.ktlint.core.ast.ElementType.ELSE
import com.pinterest.ktlint.core.ast.ElementType.FINALLY
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
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.BRACES_BLOCK_STRUCTURE_ERROR
import org.cqfn.diktat.ruleset.utils.*
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtTryExpression

class BlockStructureBraces : Rule("block-structure") {

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
        checkCloseBrace(node, configuration)
    }


    private fun checkClass(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        if (node.hasChildOfType(CLASS_BODY)) {
            checkOpenBraceOnSameLine(node, CLASS_BODY, configuration)
            checkCloseBrace(node.findChildByType(CLASS_BODY)!!, configuration)
        }
    }

    private fun checkTry(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        val tryBlock = node.psi as KtTryExpression
        val catchBlocks = tryBlock.catchClauses.map { it.node }
        val finallyBlock = tryBlock.finallyBlock?.node
        checkOpenBraceOnSameLine(tryBlock.node, BLOCK, configuration)
        val allMiddleSpaceNode = node.findAllNodesWithSpecificType(FINALLY).map { it.treePrev } +
                node.findAllNodesWithSpecificType(CATCH).map { it.treePrev }
        checkMidBrace(allMiddleSpaceNode)
        catchBlocks.forEach {
            checkOpenBraceOnSameLine(it, BLOCK, configuration)
            checkCloseBrace(it.findChildByType(BLOCK)!!, configuration)
        }
        if (finallyBlock != null) {
            checkOpenBraceOnSameLine(finallyBlock, BLOCK, configuration)
            checkCloseBrace(finallyBlock.findChildByType(BLOCK)!!, configuration)
        }
    }

    private fun checkLoop(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        node.findChildByType(BODY)?.takeIf { body -> body.hasChildOfType(BLOCK) }?.let {
            checkOpenBraceOnSameLine(node, BODY, configuration)
            checkCloseBrace(it.findChildByType(BLOCK)!!, configuration)
            if (node.elementType == DO_WHILE){
                val allMiddleNode = listOf(node.findChildByType(BODY)!!.treeNext)
                checkMidBrace(allMiddleNode)
            }
        }
    }

    private fun checkWhen(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        checkOpenBraceOnSameLine(node, LBRACE, configuration)
        checkCloseBrace(node, configuration)
    }

    private fun checkFun(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        if (node.hasChildOfType(BLOCK)) {
            checkOpenBraceOnSameLine(node, BLOCK, configuration)
            checkCloseBrace(node.findChildByType(BLOCK)!!, configuration)
        }
    }

    private fun checkIf(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        val ifPsi = node.psi as KtIfExpression
        val thenNode = ifPsi.then?.node
        val hasElseBranch = ifPsi.elseKeyword != null
        val elseNode = ifPsi.`else`?.node
        if (thenNode != null && thenNode.hasChildOfType(LBRACE)) {
            checkOpenBraceOnSameLine(node, THEN, configuration)
            checkCloseBrace(thenNode, configuration)
            if (hasElseBranch) {
                val allMiddleNode = listOf(node.findChildByType(THEN)!!.treeNext)
                checkMidBrace(allMiddleNode)
            }
        }
        if (hasElseBranch && elseNode!!.elementType != IF && elseNode.hasChildOfType(LBRACE)) {
            checkOpenBraceOnSameLine(node, ELSE, configuration)
            checkCloseBrace(elseNode, configuration)
        }
    }

    private fun checkOpenBraceOnSameLine(node: ASTNode, beforeType: IElementType, configuration: BlockStructureBracesConfiguration) {
        if (!configuration.openBrace) return
        val braceSpace = node.findChildByType(beforeType)?.treePrev
        if (braceSpace == null || checkBraceNode(braceSpace, true))
            BRACES_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "incorrect newline before opening brace",
                    (braceSpace ?: node).startOffset) {
                if (braceSpace == null || braceSpace.elementType != WHITE_SPACE) {
                    node.addChild(PsiWhiteSpaceImpl(" "), node.findChildByType(beforeType))
                } else {
                    (braceSpace as LeafPsiElement).replaceWithText(" ")
                }
            }
        checkOpenBraceEndLine(node, beforeType)
    }

    private fun checkOpenBraceEndLine(node: ASTNode, beforeType: IElementType) {
        val newNode = when (node.elementType) {
            IF -> {
                when (beforeType) {
                    THEN -> node.findChildByType(THEN)?.findChildByType(BLOCK)?.findChildByType(LBRACE)?.treeNext
                    else -> node.findChildByType(ELSE)?.findChildByType(BLOCK)?.findChildByType(LBRACE)?.treeNext
                }
            }
            WHEN -> node.findChildByType(LBRACE)?.treeNext
            FOR, WHILE, DO_WHILE -> node.findChildByType(BODY)?.findChildByType(BLOCK)?.findChildByType(LBRACE)?.treeNext
            CLASS, OBJECT_DECLARATION -> node.findChildByType(CLASS_BODY)!!.findChildByType(LBRACE)?.treeNext
            else -> node.findChildByType(BLOCK)?.findChildByType(LBRACE)?.treeNext
        }
        if (newNode == null || checkBraceNode(newNode)) {
            BRACES_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "incorrect same line after opening brace",
                    (newNode ?: node).startOffset) {
                if (newNode == null || newNode.elementType != WHITE_SPACE) {
                    node.addChild(PsiWhiteSpaceImpl("\n"), newNode ?: node.findChildByType(beforeType))
                } else {
                    (newNode as LeafPsiElement).replaceWithText("\n")
                }
            }
        }
    }

    private fun checkMidBrace(allMiddleSpace: List<ASTNode>) {
        allMiddleSpace.forEach {
            if (checkBraceNode(it, true)) {
                BRACES_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "incorrect new line after closing brace",
                        it.startOffset) {
                    if (it.elementType != WHITE_SPACE) {
                        it.addChild(PsiWhiteSpaceImpl(" "), it.treePrev)
                    } else {
                        (it as LeafPsiElement).replaceWithText(" ")
                    }
                }
            }
        }
    }

    private fun checkCloseBrace(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        if (!configuration.closeBrace) return
        val space = node.findChildByType(RBRACE)!!.treePrev
        if (checkBraceNode(space))
            BRACES_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "no newline before closing brace",
                    (space.treeNext ?: node.findChildByType(RBRACE))!!.startOffset) {
                if (space.elementType != WHITE_SPACE) {
                    node.addChild(PsiWhiteSpaceImpl("\n"), node.findChildByType(RBRACE))
                } else {
                    (space as LeafPsiElement).replaceWithText("\n")
                }
            }
    }

    private fun checkBraceNode(node: ASTNode, shouldContainNewline: Boolean = false) =
            (node.elementType != WHITE_SPACE || shouldContainNewline == node.text.contains("\n"))

    class BlockStructureBracesConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val openBrace = config["openBraceNewline"]?.toBoolean() ?: true
        val closeBrace = config["closeBraceNewline"]?.toBoolean() ?: true
    }
}

