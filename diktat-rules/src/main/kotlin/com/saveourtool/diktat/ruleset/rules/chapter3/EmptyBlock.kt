package com.saveourtool.diktat.ruleset.rules.chapter3

import com.saveourtool.diktat.common.config.rules.RuleConfiguration
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getRuleConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.EMPTY_BLOCK_STRUCTURE_ERROR
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.*

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.KtNodeTypes.CALL_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.DOT_QUALIFIED_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.FUNCTION_LITERAL
import org.jetbrains.kotlin.KtNodeTypes.LAMBDA_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.VALUE_ARGUMENT_LIST
import org.jetbrains.kotlin.KtNodeTypes.VALUE_PARAMETER
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.lexer.KtTokens.IDENTIFIER
import org.jetbrains.kotlin.lexer.KtTokens.RBRACE
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * Rule that checks if empty code blocks (`{  }`) are used and checks their formatting.
 */
class EmptyBlock(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(EMPTY_BLOCK_STRUCTURE_ERROR)
) {
    override fun logic(node: ASTNode) {
        val configuration = EmptyBlockStyleConfiguration(
            configRules.getRuleConfig(EMPTY_BLOCK_STRUCTURE_ERROR)?.configuration ?: emptyMap()
        )
        searchNode(node, configuration)
    }

    private fun searchNode(node: ASTNode, configuration: EmptyBlockStyleConfiguration) {
        val newNode = node.findLBrace()?.treeParent ?: return
        if (!isAllowedEmptyBlock(newNode) && newNode.isBlockEmpty()) {
            checkEmptyBlock(newNode, configuration)
        }
    }

    private fun isNewLine(node: ASTNode) =
        node.findChildByType(WHITE_SPACE)?.text?.contains("\n") ?: false

    private fun isAllowedEmptyBlock(node: ASTNode) = node.treeParent.isOverridden() ||
            isAnonymousSamClass(node) ||
            isLambdaUsedAsFunction(node) ||
            isKotlinLogging(node)

    @Suppress("UnsafeCallOnNullableType", "TOO_LONG_FUNCTION")
    private fun checkEmptyBlock(node: ASTNode, configuration: EmptyBlockStyleConfiguration) {
        if (!configuration.emptyBlockExist) {
            EMPTY_BLOCK_STRUCTURE_ERROR.warn(configRules, emitWarn, "empty blocks are forbidden unless it is function with override keyword",
                node.startOffset, node)
        } else {
            node.findParentNodeWithSpecificType(KtNodeTypes.LAMBDA_ARGUMENT)?.let {
                // Lambda body is always has a BLOCK -> run { } - (LBRACE, WHITE_SPACE, BLOCK "", RBRACE)
                if (isNewLine(node)) {
                    val freeText = "do not put newlines in empty lambda"
                    EMPTY_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, freeText, node.startOffset, node) {
                        val whiteSpaceNode = node.findChildByType(WHITE_SPACE)
                        whiteSpaceNode?.let {
                            node.replaceChild(whiteSpaceNode, PsiWhiteSpaceImpl(" "))
                        }
                    }
                }
                return
            }
            val space = node.findChildByType(RBRACE)!!.treePrev
            if (configuration.emptyBlockNewline && !space.text.contains("\n")) {
                EMPTY_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "different style for empty block",
                    node.startOffset, node) {
                    if (space.elementType == WHITE_SPACE) {
                        (space.treeNext as LeafPsiElement).rawReplaceWithText("\n")
                    } else {
                        node.addChild(PsiWhiteSpaceImpl("\n"), space.treeNext)
                    }
                }
            } else if (!configuration.emptyBlockNewline && space.text.contains("\n")) {
                EMPTY_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "different style for empty block",
                    node.startOffset, node) {
                    node.removeChild(space)
                }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun isAnonymousSamClass(node: ASTNode): Boolean =
        if (node.elementType == FUNCTION_LITERAL && node.hasParent(CALL_EXPRESSION)) {
            // We are checking identifier because it is not class in AST,
            // SAM conversions are indistinguishable from lambdas.
            // So we just verify that identifier is in PascalCase
            val valueArgument = node.findParentNodeWithSpecificType(CALL_EXPRESSION)!!
            valueArgument.findLeafWithSpecificType(IDENTIFIER)?.text?.isPascalCase() ?: false
        } else {
            false
        }

    @Suppress("UnsafeCallOnNullableType")
    private fun isLambdaUsedAsFunction(node: ASTNode): Boolean {
        val parents = node.parents()
        return when {
            parents.any { it.elementType == CALL_EXPRESSION } -> {
                val callExpression = parents.find { it.elementType == CALL_EXPRESSION }!!
                // excepting cases like list.map { }. In this case call expression will not have value argument list
                // And in this case: Parser.parse({}, some, thing) it will have value argument list
                callExpression.hasChildOfType(VALUE_ARGUMENT_LIST)
            }
            parents.any { it.elementType == LAMBDA_EXPRESSION } -> {
                val lambdaExpression = parents.find { it.elementType == LAMBDA_EXPRESSION }!!
                // cases like A({}). Here Lambda expression is used as a value parameter.
                lambdaExpression.treeParent.elementType == VALUE_PARAMETER
            }
            else -> false
        }
    }

    private fun isKotlinLogging(node: ASTNode): Boolean = node.findParentNodeWithSpecificType(DOT_QUALIFIED_EXPRESSION)
        ?.text
        ?.replace(" ", "")
        .let {
            it == "KotlinLogging.logger{}"
        }

    /**
     * [RuleConfiguration] for empty blocks formatting
     */
    class EmptyBlockStyleConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Whether empty code blocks should be allowed
         */
        val emptyBlockExist = config["allowEmptyBlocks"]?.toBoolean() ?: false

        /**
         * Whether a newline after `{` is required in an empty block
         */
        val emptyBlockNewline = config["styleEmptyBlockWithNewline"]?.toBoolean() ?: true
    }

    companion object {
        const val NAME_ID = "empty-block-structure"
    }
}
