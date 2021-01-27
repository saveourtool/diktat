package org.cqfn.diktat.ruleset.chapter4

import org.cqfn.diktat.ruleset.rules.chapter4.SmartCastRule
import org.cqfn.diktat.util.FixTestBase

import org.junit.jupiter.api.Test

class SmartCastRuleFixTest : FixTestBase("test/paragraph4/smart_cast", ::SmartCastRule) {
    @Test
    fun `should fix enum order`() {
        fixAndCompare("SmartCastExpected.kt", "SmartCastTest.kt")
    }
}
