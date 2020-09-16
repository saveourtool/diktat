package org.cqfn.diktat.ruleset.chapter3

import generated.WarningNames
import org.cqfn.diktat.ruleset.rules.SmartCastRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class SmartCastRuleFixTest : FixTestBase("test/paragraph4/smart_cast", ::SmartCastRule) {
    @Test
    fun `should fix enum order`() {
        fixAndCompare("SmartCastExpected.kt", "SmartCastTest.kt")
    }
}