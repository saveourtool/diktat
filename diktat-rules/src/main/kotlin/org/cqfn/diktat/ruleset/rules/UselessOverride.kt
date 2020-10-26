package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.EQ
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.OVERRIDE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.RBRACE
import com.pinterest.ktlint.core.ast.ElementType.SUPER_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.USELESS_OVERRIDE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.psi.psiUtil.siblings

/**
 * rule 6.1.5
 * Explicit supertype qualification should not be used if there is not clash between called methods
 */
class UselessOverride(private val configRules: List<RulesConfig>) : Rule("useless-override") {

    companion object {
        private val USELESS_CHILDREN_OVERRIDE_FUNCTION = listOf(WHITE_SPACE, LBRACE,
                RBRACE, DOT_QUALIFIED_EXPRESSION, KDOC, EOL_COMMENT, BLOCK_COMMENT)
    }

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        if(node.elementType == FUN)
            checkFun(node)
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkFun(node: ASTNode) {
        if (node.getChildren(TokenSet.create(MODIFIER_LIST)).any { it.elementType == OVERRIDE_KEYWORD })
            return
        val functionBody = (node.findChildByType(BLOCK)?.getChildren(null)?.toList() ?: node.findChildByType(EQ)!!.siblings(true).toList())
        if (USELESS_CHILDREN_OVERRIDE_FUNCTION.containsAll(functionBody.map { it.elementType })) {
            if (functionBody.find { it.elementType == DOT_QUALIFIED_EXPRESSION }?.findChildByType(SUPER_EXPRESSION) != null) {
                USELESS_OVERRIDE.warnAndFix(configRules, emitWarn, isFixMode, node.findChildByType(IDENTIFIER)!!.text, node.startOffset, node) {
                    val parentNode = node.treeParent
                    if (node.treeNext != null && node.treeNext.elementType == WHITE_SPACE)
                        parentNode.removeChild(node.treeNext)
                    parentNode.removeChild(node)
                }
            }
        }
    }
}
