package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.isRuleEnabled
import org.cqfn.diktat.ruleset.constants.EmitType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

typealias DiktatConfigRule = org.cqfn.diktat.common.config.rules.Rule

abstract class DiktatRule(id: String, val configRules: List<RulesConfig>, val rules: List<DiktatConfigRule>): Rule(id) {
    var isFixMode: Boolean = false
    lateinit var emitWarn: EmitType

    override fun visit(node: ASTNode, autoCorrect: Boolean, emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (check()) return
        else {
            try {
                logic(node)
            } catch (e: Exception) {
                // TODO: Specify message
            }
        }
    }

    private fun check(): Boolean {
        return rules.none { configRules.isRuleEnabled(it) }
    }
    abstract fun logic(node: ASTNode)
}