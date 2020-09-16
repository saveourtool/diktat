package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.FILE
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

class SmartCastRule(private val configRules: List<RulesConfig>) : Rule("smart-cast-rule") {

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node == FILE) {

            val x: String? = null

            if (x !is String) {
                (x as Int)
            } else {
                x.length
            }
        }
    }

    private fun handleIfBlock(node: ASTNode) {
        /*
            There are 2 conditions.
            1) if has 'is' in condition, then we check all 'as' keywords in 'then block'
            2) if has '!is' -> NOT_IS in condition, then we check all 'as' keywords in else's 'then block'
         */
    }

    private fun handleWhenCondition(node: ASTNode) {
        /*
            Check if there is WHEN_CONDITION_IS_PATTERN. If so delete 'as' in it's block
            or call expression if it doesn't have block
         */
    }
}