package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.BODY
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.CLASS_INITIALIZER
import com.pinterest.ktlint.core.ast.ElementType.DO_WHILE
import com.pinterest.ktlint.core.ast.ElementType.ELSE
import com.pinterest.ktlint.core.ast.ElementType.FOR
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.FUNCTION_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.IF
import com.pinterest.ktlint.core.ast.ElementType.LAMBDA_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.OBJECT_DECLARATION
import com.pinterest.ktlint.core.ast.ElementType.SECONDARY_CONSTRUCTOR
import com.pinterest.ktlint.core.ast.ElementType.THEN
import com.pinterest.ktlint.core.ast.ElementType.TRY
import com.pinterest.ktlint.core.ast.ElementType.WHEN
import com.pinterest.ktlint.core.ast.ElementType.WHILE
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.prevSibling
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.BRACES_BLOCK_STRUCTURE_ERROR
import org.cqfn.diktat.ruleset.utils.findChildAfter
import org.cqfn.diktat.ruleset.utils.findChildBefore
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtTryExpression
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty

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
            LAMBDA_EXPRESSION -> checkLambda(node, configuration)
            CLASS, OBJECT_DECLARATION -> checkClass(node, configuration)
            FUN, CLASS_INITIALIZER, SECONDARY_CONSTRUCTOR -> checkFun(node, configuration)
            IF -> checkIf(node, configuration)
            WHEN -> checkWhen(node, configuration)
            FOR, WHILE, DO_WHILE -> checkLoop(node, configuration)
            TRY -> checkTry(node, configuration)
        }
    }

    private fun checkLambda(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        if (node.hasChildOfType(FUNCTION_LITERAL)){
            checkCloseBrace(node.findChildByType(FUNCTION_LITERAL), configuration)
        }
    }


    private fun checkClass(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        if (node.hasChildOfType(CLASS_BODY)) {
            checkOpenBrace(node, CLASS_BODY, configuration)
            checkCloseBrace(node.findChildByType(CLASS_BODY), configuration)
        }
    }

    private fun checkTry(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        val tryBlock = node.psi as KtTryExpression
        val catchBlocks = tryBlock.catchClauses.map { it.node }
        val finallyBlock = tryBlock.finallyBlock?.node
        checkOpenBrace(tryBlock.node, BLOCK, configuration)
        catchBlocks.ifNotEmpty {
            catchBlocks.forEach {
                checkOpenBrace(it, BLOCK, configuration)
                checkCloseBrace(it.findChildByType(BLOCK)!!, configuration)
            }
        }
        if (finallyBlock != null) {
            checkOpenBrace(finallyBlock, BLOCK, configuration)
            checkCloseBrace(finallyBlock.findChildByType(BLOCK)!!, configuration)
        }
    }

    private fun checkLoop(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        if (node.hasChildOfType(BODY) && node.findChildByType(BODY)!!.hasChildOfType(BLOCK)) {
            checkOpenBrace(node, BODY, configuration)
            checkCloseBrace(node.findChildByType(BODY)?.findChildByType(BLOCK), configuration)
        }
    }

    private fun checkWhen(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        checkOpenBrace(node, LBRACE, configuration)
        checkCloseBrace(node, configuration)
    }

    private fun checkFun(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        if (node.hasChildOfType(BLOCK)) {
            checkOpenBrace(node, BLOCK, configuration)
            checkCloseBrace(node.findChildByType(BLOCK)!!, configuration)
        }
    }

    private fun checkIf(node: ASTNode, configuration: BlockStructureBracesConfiguration) {
        val ifPsi = node.psi as KtIfExpression
        val thenNode = ifPsi.then?.node
        val hasElseBranch = ifPsi.elseKeyword != null
        val elseNode = ifPsi.`else`?.node
        if (thenNode != null && thenNode.hasChildOfType(LBRACE)) {
            checkOpenBrace(node, THEN, configuration)
            checkCloseBrace(thenNode, configuration)
        }
        if (hasElseBranch && elseNode!!.elementType != IF && elseNode.hasChildOfType(LBRACE)) {
            checkOpenBrace(node, ELSE, configuration)
            checkCloseBrace(elseNode, configuration)
        }
    }

    private fun checkOpenBrace(node: ASTNode, beforeType: IElementType, configuration: BlockStructureBracesConfiguration) {
        if (!configuration.openBrace) return
        val braceSpace = node.findChildBefore(beforeType, WHITE_SPACE)
        if (braceSpace != null) {
            if (braceSpace.text.contains("\n".toRegex())) {
                BRACES_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "incorrect newline before opening brace",
                        (braceSpace.prevSibling {it.elementType == beforeType} ?:braceSpace).startOffset){
                }
                return
            }
        }
        val nodeText = when (node.elementType) {
            IF -> {
                when (beforeType) {
                    THEN -> node.findChildByType(THEN)?.findChildByType(BLOCK)?.findChildAfter(LBRACE, WHITE_SPACE)
                    else -> node.findChildByType(ELSE)?.findChildByType(BLOCK)?.findChildAfter(LBRACE, WHITE_SPACE)
                }
            }
            WHEN -> node.findChildAfter(LBRACE, WHITE_SPACE)
            FOR, WHILE, DO_WHILE -> node.findChildByType(BODY)?.findChildByType(BLOCK)?.findChildAfter(LBRACE, WHITE_SPACE)
            CLASS, OBJECT_DECLARATION -> node.findChildByType(CLASS_BODY)!!.findChildAfter(LBRACE, WHITE_SPACE)
            else -> node.findChildByType(BLOCK)?.findChildAfter(LBRACE, WHITE_SPACE)
        }
        if (nodeText != null && !nodeText.text.contains("\n".toRegex())) {
            BRACES_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "incorrect line after opening brace",
                    nodeText.startOffset) {
            }
        }
    }

    private fun checkCloseBrace(node: ASTNode?, configuration: BlockStructureBracesConfiguration) {
        if (!configuration.closeBrace) return
        val space = node?.getChildren(null)?.findLast { it.elementType == WHITE_SPACE }
        if (space != null && !space.text.contains("\n".toRegex())) {
            BRACES_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "incorrect line after closing brace",
                    (space).startOffset) {
            }
        }
    }

    class BlockStructureBracesConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val openBrace = config["open-brace-newline"]?.toBoolean() ?: true
        val closeBrace = config["close-brace-newline"]?.toBoolean() ?: true
    }

}

