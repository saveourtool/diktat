package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.chapter3.CollapseIfStatementsRule
import org.cqfn.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CollapseIfStatementsRuleFixTest : FixTestBase(
    "test/paragraph3/collapse_if",
    ::CollapseIfStatementsRule,
    listOf(
        RulesConfig(Warnings.COLLAPSE_IF_STATEMENTS.name, true, emptyMap())
    )
) {
    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `collapse if`() {
        fixAndCompare("CollapseIfStatementsExpected.kt", "CollapseIfStatementsTest.kt")
    }
}
