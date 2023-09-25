package com.saveourtool.diktat.ruleset.rules.chapter5

import com.saveourtool.diktat.common.config.rules.RuleConfiguration
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getRuleConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.NESTED_BLOCK
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.findAllDescendantsWithSpecificType
import com.saveourtool.diktat.ruleset.utils.hasChildOfType

import org.jetbrains.kotlin.KtNodeTypes.CLASS
import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.FUNCTION_LITERAL
import org.jetbrains.kotlin.KtNodeTypes.OBJECT_DECLARATION
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens.IDENTIFIER
import org.jetbrains.kotlin.lexer.KtTokens.LBRACE
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * Rule 5.1.2 Nested blokcs
 */
class NestedFunctionBlock(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(NESTED_BLOCK)
) {
    private val configuration: NestedBlockConfiguration by lazy {
        NestedBlockConfiguration(configRules.getRuleConfig(NESTED_BLOCK)?.configuration ?: emptyMap())
    }

    override fun logic(node: ASTNode) {
        if (node.elementType in nullificationType) {
            countNestedBlocks(node, configuration.maxNestedBlockQuantity)
        }
    }

    private fun countNestedBlocks(node: ASTNode, maxNestedBlockCount: Long) {
        node.findAllDescendantsWithSpecificType(LBRACE)
            .reversed()
            .forEach { lbraceNode ->
                val blockParent = lbraceNode
                    .parents()
                    .takeWhile { it != node }
                    .takeIf { parentList -> parentList.map { it.elementType }.none { it in nullificationType } }
                    ?.count { it.hasChildOfType(LBRACE) }
                    ?: return
                if (blockParent > maxNestedBlockCount) {
                    NESTED_BLOCK.warn(configRules, emitWarn, node.findChildByType(IDENTIFIER)?.text ?: node.text,
                        node.startOffset, node)
                    return
                }
            }
    }

    /**
     * [RuleConfiguration] for analyzing nested code blocks
     */
    class NestedBlockConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Maximum number of allowed levels of nested blocks
         */
        val maxNestedBlockQuantity = config["maxNestedBlockQuantity"]?.toLong() ?: MAX_NESTED_BLOCK_COUNT
    }

    companion object {
        private const val MAX_NESTED_BLOCK_COUNT = 4L
        const val NAME_ID = "nested-block"

        /**
         * Nodes of these types reset counter of nested blocks
         */
        private val nullificationType = listOf(CLASS, FUN, OBJECT_DECLARATION, FUNCTION_LITERAL)
    }
}
