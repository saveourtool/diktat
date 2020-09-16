package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BINARY_WITH_TYPE
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.CONDITION
import com.pinterest.ktlint.core.ast.ElementType.ELSE
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.IF
import com.pinterest.ktlint.core.ast.ElementType.IS_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.IS_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.NOT_IS
import com.pinterest.ktlint.core.ast.ElementType.THEN
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.constants.Warnings.SMART_CAST_NEEDED
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.findParentNodeWithSpecificType
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.hasParent
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

class SmartCastRule(private val configRules: List<RulesConfig>) : Rule("smart-cast-rule") {

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == IF) {
            handleIfBlock(node)
        }
    }

    private fun handleIfBlock(node: ASTNode) {
        /*
            There are 2 conditions.
            1) if has 'is' in condition, then we check all 'as' keywords in 'then block'
            2) if has '!is' -> NOT_IS in condition, then we check all 'as' keywords in else's 'then block'
         */
        val isBlocks = mutableListOf<IsExpressions>()
        val notIsBlocks = mutableListOf<IsExpressions>()

        val conditionBlock = node.findAllNodesWithSpecificType(CONDITION).first()

        conditionBlock.findAllNodesWithSpecificType(IS_EXPRESSION).map { it.text }.forEach {
            if (it.contains("!is")) {
                val split = it.split("!is").map { part -> part.trim() }
                notIsBlocks.add(IsExpressions(split[0], split[1]))
            } else {
                val split = it.split("is").map { part -> part.trim() }
                isBlocks.add(IsExpressions(split[0], split[1]))
            }
        }

        val elseBlock = node.findAllNodesWithSpecificType(ELSE).firstOrNull { it.firstChildNode.elementType != IF }

        if (isBlocks.isNotEmpty()) {
            val then = node.findChildByType(THEN)!!
            val thenBlock = then.findChildByType(BLOCK)

            if (thenBlock != null) {
                // Find all as expressions that are inside this current block
                val asList = thenBlock.findAllNodesWithSpecificType(BINARY_WITH_TYPE).filter { it.text.contains(" as ")
                        && it.findParentNodeWithSpecificType(BLOCK) == thenBlock }
                val asExpr = mutableListOf<AsExpressions>()

                asList.forEach {
                    val split = it.text.split("as").map { part -> part.trim() }
                    asExpr.add(AsExpressions(split[0], split[1], it))
                }

                val exprToChange = asExpr.filter {
                    isBlocks.any { isExpr ->
                        isExpr.identifier == it.identifier
                                && isExpr.type == it.type
                    }
                }

                if (exprToChange.isNotEmpty()) {
                    exprToChange.forEach {
                        SMART_CAST_NEEDED.warn(configRules, emitWarn, isFixMode, it.identifier, thenBlock.startOffset)
                    }
                }
            }
        }
    }

    private fun handleWhenCondition(node: ASTNode) {
        /*
            Check if there is WHEN_CONDITION_IS_PATTERN. If so delete 'as' in it's block
            or call expression if it doesn't have block
         */
    }

    class AsExpressions(val identifier: String, val type: String, val node:ASTNode)

    class IsExpressions(val identifier: String, val type: String)
}