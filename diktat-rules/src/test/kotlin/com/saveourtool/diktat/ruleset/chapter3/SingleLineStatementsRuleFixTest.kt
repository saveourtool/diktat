package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.ruleset.rules.chapter3.SingleLineStatementsRule
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class SingleLineStatementsRuleFixTest : FixTestBase("test/paragraph3/statement", ::SingleLineStatementsRule) {
    @Test
    @Tag(WarningNames.MORE_THAN_ONE_STATEMENT_PER_LINE)
    fun `should make one statement per line`() {
        fixAndCompare("StatementExpected.kt", "StatementTest.kt")
    }
}
