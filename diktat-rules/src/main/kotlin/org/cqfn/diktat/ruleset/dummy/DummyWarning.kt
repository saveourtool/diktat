package org.cqfn.diktat.ruleset.dummy

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Dummy warning used for testing and debug purposes.
 * Can be used in manual testing.
 */
class DummyWarning(configRules: List<RulesConfig>) : DiktatRule(
    "dummy-rule",
    configRules,
    listOf(
        Warnings.FILE_NAME_INCORRECT,
        Warnings.FILE_NAME_MATCH_CLASS
    )
) {
    @Suppress("UNUSED")
    private lateinit var filePath: String

    @Suppress("EmptyFunctionBlock")
    override fun logic(node: ASTNode) {}
}
