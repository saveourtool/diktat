package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.rules.Statement
import org.cqfn.diktat.util.FixTestBase
import org.junit.Test

class StatementFixTest : FixTestBase("test/paragraph3/statement", Statement()) {

    @Test
    fun `should make one statement per line`() {
        fixAndCompare("StatementExpected.kt", "StatementTest.kt")
    }
}
