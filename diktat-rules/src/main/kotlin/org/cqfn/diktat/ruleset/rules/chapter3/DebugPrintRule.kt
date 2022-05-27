package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

class DebugPrintRule(configRules: List<RulesConfig>) : DiktatRule(NAME_ID, configRules, listOf()) {

    override fun logic(node: ASTNode) {
        TODO("Not yet implemented")
    }

    private companion object {
        const val NAME_ID = "debug-print"
    }
}