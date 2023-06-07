package com.saveourtool.diktat.ruleset.chapter5

import com.saveourtool.diktat.ruleset.rules.chapter5.CheckInverseMethodRule
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames.INVERSE_FUNCTION_PREFERRED
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CheckInverseMethodRuleFixTest : FixTestBase("test/paragraph5/method_call_names", ::CheckInverseMethodRule) {
    @Test
    @Tag(INVERSE_FUNCTION_PREFERRED)
    fun `should fix method calls`() {
        fixAndCompare("ReplaceMethodCallNamesExpected.kt", "ReplaceMethodCallNamesTest.kt")
    }
}
