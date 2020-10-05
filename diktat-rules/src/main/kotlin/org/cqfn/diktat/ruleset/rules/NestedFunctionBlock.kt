package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.BODY
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.ELSE
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.FUNCTION_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.IF
import com.pinterest.ktlint.core.ast.ElementType.OBJECT_DECLARATION
import com.pinterest.ktlint.core.ast.ElementType.THEN
import com.pinterest.ktlint.core.ast.ElementType.WHEN
import com.pinterest.ktlint.core.ast.ElementType.WHEN_ENTRY
import com.pinterest.ktlint.core.ast.parent
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.NESTED_BLOCK
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.psi.KtClass

/**
 * Rule 5.1.2 Nested blokcs
 */
class NestedFunctionBlock(private val configRules: List<RulesConfig>) : Rule("nested-block") {

    companion object {
        private const val MAX_NESTED_BLOCK_COUNT = 4L
    }

    val configuration: NestedBlockConfiguration by lazy {
        NestedBlockConfiguration(configRules.getRuleConfig(NESTED_BLOCK)?.configuration ?: mapOf())
    }

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        when (node.elementType) {
            FUN, FUNCTION_LITERAL -> handleFunForNestedBlocks(node, configuration.maxNestedBlockQuantity)
            CLASS, OBJECT_DECLARATION -> handleLocalCLassForNestedBlocks(node, configuration.maxNestedBlockQuantity)
        }
    }

    private fun handleLocalCLassForNestedBlocks(node: ASTNode, maxNestedBlockCount: Long) {
        if (node.elementType == CLASS && !(node.psi as KtClass).isLocal) return
        node.getChildren(TokenSet.create(CLASS_BODY)).forEach {
            dfsBlock(it, node, 1, maxNestedBlockCount)
        }
    }

    private fun handleFunForNestedBlocks(node: ASTNode, maxNestedBlockCount: Long) {
        val classParentNode = node.parent({ it.elementType == CLASS })
        if (classParentNode != null && (classParentNode.psi as KtClass).isLocal) return
        node.getChildren(TokenSet.create(BLOCK)).forEach {
            dfsBlock(it, node, 1, maxNestedBlockCount)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun dfsBlock(node: ASTNode, initialNode: ASTNode, blockCount: Int, maxNestedBlockCount: Long) {
        if (blockCount > maxNestedBlockCount) {
            NESTED_BLOCK.warn(configRules, emitWarn, isFixMode, initialNode.findChildByType(IDENTIFIER)?.text
                    ?: initialNode.text,
                    initialNode.startOffset, initialNode)
            return
        } else {
            findBlocks(node).forEach {
                dfsBlock(it, initialNode, blockCount + 1, maxNestedBlockCount)
            }
        }
    }

    private fun findBlocks(node: ASTNode): List<ASTNode> {
        val result = mutableListOf<ASTNode>()
        node.getChildren(null).forEach { childNode ->
            when (childNode.elementType) {
                IF -> Pair(childNode.findChildByType(THEN)?.findChildByType(BLOCK),
                        (childNode.findChildByType(ELSE)?.findChildByType(BLOCK)
                                ?: if(childNode.findChildByType(ELSE)?.findChildByType(IF) != null)
                                    childNode.findChildByType(ELSE) else null))
                WHEN -> Pair(childNode, null)
                WHEN_ENTRY -> Pair(childNode.findChildByType(BLOCK), null)
                FUN -> Pair(childNode.findChildByType(BLOCK), null)
                else -> Pair(childNode.findChildByType(BODY)?.findChildByType(BLOCK), null)
            }.let { pair ->
                pair.first?.let { result.add(it) }
                pair.second?.let { result.add(it) }
            }
        }
        return result
    }

    class NestedBlockConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val maxNestedBlockQuantity = config["maxNestedBlockQuantity"]?.toLong() ?: MAX_NESTED_BLOCK_COUNT
    }
}
