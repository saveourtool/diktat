package com.saveourtool.diktat.ruleset.rules.chapter3

import com.saveourtool.diktat.common.config.rules.RuleConfiguration
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getRuleConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.CONVENTIONAL_RANGE
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.KotlinParser
import com.saveourtool.diktat.ruleset.utils.getIdentifierName
import com.saveourtool.diktat.ruleset.utils.hasChildOfType
import com.saveourtool.diktat.ruleset.utils.isWhiteSpace
import com.saveourtool.diktat.ruleset.utils.parent
import com.saveourtool.diktat.ruleset.utils.takeByChainOfTypes

import org.jetbrains.kotlin.KtNodeTypes.BINARY_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.DOT_QUALIFIED_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.REFERENCE_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.VALUE_ARGUMENT
import org.jetbrains.kotlin.KtNodeTypes.VALUE_ARGUMENT_LIST
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.lexer.KtTokens.IDENTIFIER
import org.jetbrains.kotlin.lexer.KtTokens.INTEGER_LITERAL
import org.jetbrains.kotlin.lexer.KtTokens.MINUS
import org.jetbrains.kotlin.lexer.KtTokens.RANGE
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

/**
 * This rule warn and fix cases when it's possible to replace range operator `..` with infix function `until`
 * or replace `rangeTo` function with range operator `..`
 */
class RangeConventionalRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(CONVENTIONAL_RANGE)
) {
    private val configuration by lazy {
        RangeConventionalConfiguration(
            this.configRules.getRuleConfig(CONVENTIONAL_RANGE)?.configuration ?: emptyMap(),
        )
    }
    override fun logic(node: ASTNode) {
        if (node.elementType == DOT_QUALIFIED_EXPRESSION && !configuration.isRangeToIgnore) {
            handleQualifiedExpression(node)
        }
        if (node.elementType == RANGE) {
            handleRange(node)
        }
    }

    @Suppress("TOO_MANY_LINES_IN_LAMBDA", "PARAMETER_NAME_IN_OUTER_LAMBDA")
    private fun handleQualifiedExpression(node: ASTNode) {
        (node.psi as KtDotQualifiedExpression).selectorExpression?.node?.let {
            if (it.findChildByType(REFERENCE_EXPRESSION)?.getIdentifierName()?.text == "rangeTo") {
                val arguments = it.findChildByType(VALUE_ARGUMENT_LIST)?.getChildren(TokenSet.create(VALUE_ARGUMENT))
                if (arguments?.size == 1) {
                    CONVENTIONAL_RANGE.warnAndFix(configRules, emitWarn, isFixMode, "replace `rangeTo` with `..`: ${node.text}", node.startOffset, node) {
                        val receiverExpression = (node.psi as KtDotQualifiedExpression).receiverExpression.text
                        val correctNode = KotlinParser().createNode("$receiverExpression..${arguments[0].text}")
                        node.treeParent.addChild(correctNode, node)
                        node.treeParent.removeChild(node)
                    }
                }
            }
        }
    }

    @Suppress("TOO_MANY_LINES_IN_LAMBDA")
    private fun handleRange(node: ASTNode) {
        val binaryInExpression = node.parent(BINARY_EXPRESSION)?.psi as KtBinaryExpression?
        binaryInExpression
            ?.right
            ?.node
            // Unwrap parentheses and get `BINARY_EXPRESSION` on the RHS of `..`
            ?.takeByChainOfTypes(BINARY_EXPRESSION)
            ?.run { psi as? KtBinaryExpression }
            ?.takeIf { it.operationReference.node.hasChildOfType(MINUS) }
            ?.let { upperBoundExpression ->
                val isMinusOne = (upperBoundExpression.right as? KtConstantExpression)?.firstChild?.let {
                    it.node.elementType == INTEGER_LITERAL && it.text == "1"
                } ?: false
                if (!isMinusOne) {
                    return@let
                }
                // At this point we are sure that `upperBoundExpression` is `[left] - 1` and should be replaced.
                val errorNode = binaryInExpression.node
                CONVENTIONAL_RANGE.warnAndFix(configRules, emitWarn, isFixMode, "replace `..` with `until`: ${errorNode.text}", errorNode.startOffset, errorNode) {
                    // Replace `..` with `until`
                    replaceUntil(node)
                    // fix right side of binary expression to correct form (remove ` - 1 `) : (b-1) -> (b)
                    val astNode = upperBoundExpression.node
                    val parent = astNode.treeParent
                    parent.addChild(astNode.firstChildNode, astNode)
                    parent.removeChild(astNode)
                }
            }
    }

    private fun replaceUntil(node: ASTNode) {
        val untilNode = LeafPsiElement(IDENTIFIER, "until")
        val parent = node.treeParent
        if (parent.treePrev?.isWhiteSpace() != true) {
            parent.treeParent.addChild(PsiWhiteSpaceImpl(" "), parent)
        }
        if (parent.treeNext?.isWhiteSpace() != true) {
            parent.treeParent.addChild(PsiWhiteSpaceImpl(" "), parent.treeNext)
        }
        parent.addChild(untilNode, node)
        parent.removeChild(node)
    }

    /**
     *
     * [RuleConfiguration] for rangeTo function
     */
    class RangeConventionalConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * If true, don't suggest to replace `rangeTo` function with operator `..`
         */
        val isRangeToIgnore = config["isRangeToIgnore"]?.toBoolean() ?: false
    }

    companion object {
        const val NAME_ID = "range"
    }
}
