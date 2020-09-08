package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.OVERRIDE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.RBRACE
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.EMPTY_BLOCK_STRUCTURE_ERROR
import org.cqfn.diktat.ruleset.utils.findLBrace
import org.cqfn.diktat.ruleset.utils.isBlockEmpty
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl

class EmptyBlock(private val configRules: List<RulesConfig>) : Rule("empty-block-structure") {

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        val configuration = EmptyBlockStyleConfiguration(
                configRules.getRuleConfig(EMPTY_BLOCK_STRUCTURE_ERROR)?.configuration ?: mapOf()
        )
        searchNode(node, configuration)
    }

    private fun searchNode(node: ASTNode, configuration: EmptyBlockStyleConfiguration) {
        val newNode = node.findLBrace()?.treeParent ?: return
        checkEmptyBlock(newNode, configuration)
    }

    private fun checkEmptyBlock(node: ASTNode, configuration: EmptyBlockStyleConfiguration) {
        if (node.treeParent.findChildByType(MODIFIER_LIST)?.findChildByType(OVERRIDE_KEYWORD) != null) return
        if (node.isBlockEmpty()) {
            if (!configuration.emptyBlockExist) {
                EMPTY_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "empty blocks are forbidden unless it is function with override keyword",
                        node.startOffset, node) {}
            } else {
                val space = node.findChildByType(RBRACE)!!.treePrev
                if (configuration.emptyBlockNewline && !space.text.contains("\n")) {
                    EMPTY_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "different style for empty block",
                            node.startOffset, node) {
                        if (space.elementType == WHITE_SPACE)
                            (space.treeNext as LeafPsiElement).replaceWithText("\n")
                        else
                            node.addChild(PsiWhiteSpaceImpl("\n"), space.treeNext)
                    }
                } else if (!configuration.emptyBlockNewline && space.text.contains("\n")) {
                    EMPTY_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "different style for empty block",
                            node.startOffset, node) {
                        node.removeChild(space)
                    }
                }
            }
        }
    }

    class EmptyBlockStyleConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val emptyBlockExist = config["allowEmptyBlocks"]?.toBoolean() ?: false
        val emptyBlockNewline = config["styleEmptyBlockWithNewline"]?.toBoolean() ?: true
    }
}
