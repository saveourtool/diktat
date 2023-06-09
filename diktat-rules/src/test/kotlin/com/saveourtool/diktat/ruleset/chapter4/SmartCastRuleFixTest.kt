package com.saveourtool.diktat.ruleset.chapter4

import com.saveourtool.diktat.ruleset.rules.chapter4.SmartCastRule
import com.saveourtool.diktat.util.FixTestBase

import org.junit.jupiter.api.Test

class SmartCastRuleFixTest : FixTestBase("test/paragraph4/smart_cast", ::SmartCastRule) {
    @Test
    fun `should fix enum order`() {
        fixAndCompare("SmartCastExpected.kt", "SmartCastTest.kt")
    }
}
