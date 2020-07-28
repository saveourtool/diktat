package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.THEN
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.BODY
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.DO_WHILE
import com.pinterest.ktlint.core.ast.ElementType.ELSE
import com.pinterest.ktlint.core.ast.ElementType.FOR
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.OBJECT_DECLARATION
import com.pinterest.ktlint.core.ast.ElementType.OVERRIDE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.RBRACE
import com.pinterest.ktlint.core.ast.ElementType.WHEN
import com.pinterest.ktlint.core.ast.ElementType.WHILE
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.BRACES_BLOCK_STRUCTURE_ERROR
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.cqfn.diktat.ruleset.utils.isBlockEmpty
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl

class EmptyBlock : Rule("empty-block-structure") {

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

        val configuration = EmptyBlockStyleConfiguration(
                configRules.getRuleConfig(BRACES_BLOCK_STRUCTURE_ERROR)?.configuration ?: mapOf()
        )
        searchNode(node, configuration)
    }

    private fun searchNode(node: ASTNode, configuration: EmptyBlockStyleConfiguration) {
        val newNode = when (node.elementType) {
            THEN, ELSE -> node.findChildByType(BLOCK)?.findChildByType(LBRACE)!!.treeNext.treeParent
            WHEN -> node.findChildByType(LBRACE)!!.treeNext.treeParent
            FOR, WHILE, DO_WHILE -> node.findChildByType(BODY)?.findChildByType(BLOCK)?.findChildByType(LBRACE)!!.treeNext.treeParent
            CLASS, OBJECT_DECLARATION -> node.findChildByType(CLASS_BODY)!!.findChildByType(LBRACE)!!.treeNext.treeParent
            else -> if (node.hasChildOfType(BLOCK)) node.findChildByType(BLOCK)?.findChildByType(LBRACE)!!.treeNext.treeParent else null
        } ?: return
        checkEmptyBlock(newNode, configuration)
    }

    private fun checkEmptyBlock(node: ASTNode, configuration: EmptyBlockStyleConfiguration) {
        if (node.treeParent.findChildByType(MODIFIER_LIST)?.findChildByType(OVERRIDE_KEYWORD) != null) return
        if (node.isBlockEmpty(node)) {
            BRACES_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "there can't be empty blocks in multi blocks",
                    node.startOffset) {}
            val space = node.findChildByType(RBRACE)!!.treePrev
            if (configuration.emptyBlockNewline && !space.text.contains("\n")) {
                BRACES_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "different style for empty block",
                        node.startOffset) {
                    if (space.elementType == WHITE_SPACE)
                        (space.treeNext as LeafPsiElement).replaceWithText("\n")
                    else
                        node.addChild(PsiWhiteSpaceImpl("\n"), space.treeNext)
                }
            } else if (!configuration.emptyBlockNewline && space.text.contains("\n")) {
                BRACES_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "different style for empty block",
                        node.startOffset) {
                    node.removeChild(space)
                }
            }
        }
    }

    class EmptyBlockStyleConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val emptyBlockNewline = config["styleEmptyBlockWithNewline"]?.toBoolean() ?: true
    }
}
