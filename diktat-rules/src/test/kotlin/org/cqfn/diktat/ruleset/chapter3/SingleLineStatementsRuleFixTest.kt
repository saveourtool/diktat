package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.rules.SingleLineStatementsRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.Test

class SingleLineStatementsRuleFixTest : FixTestBase("test/paragraph3/statement", SingleLineStatementsRule()) {

    @Test
    fun `should make one statement per line`() {
        fixAndCompare("StatementExpected.kt", "StatementTest.kt")
    }
}
