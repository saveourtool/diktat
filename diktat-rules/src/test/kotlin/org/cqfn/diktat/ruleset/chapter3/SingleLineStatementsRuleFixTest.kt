package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.rules.chapter3.SingleLineStatementsRule
import org.cqfn.diktat.util.FixTestBase

import org.cqfn.diktat.ruleset.constants.WarningsNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class SingleLineStatementsRuleFixTest : FixTestBase("test/paragraph3/statement", ::SingleLineStatementsRule) {
    @Test
    @Tag(WarningNames.MORE_THAN_ONE_STATEMENT_PER_LINE)
    fun `should make one statement per line`() {
        fixAndCompare("StatementExpected.kt", "StatementTest.kt")
    }
}
