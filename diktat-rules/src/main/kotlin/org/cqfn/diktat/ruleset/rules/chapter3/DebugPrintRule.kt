package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Rule checks that print or println are not in used (assumption: it's a debug logging)
 */
class DebugPrintRule(configRules: List<RulesConfig>, prevId: String?) : DiktatRule(NAME_ID, configRules, listOf(), prevId) {

    companion object {
        const val NAME_ID = "aan-empty-block-structure"
    }

    override fun logic(node: ASTNode) {
        TODO("Not yet implemented")
    }
}