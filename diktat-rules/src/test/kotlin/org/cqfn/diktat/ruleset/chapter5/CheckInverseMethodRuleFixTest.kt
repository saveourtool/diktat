package org.cqfn.diktat.ruleset.chapter5

import org.cqfn.diktat.ruleset.rules.chapter5.CheckInverseMethodRule
import org.cqfn.diktat.util.FixTestBase

import org.cqfn.diktat.ruleset.constants.WarningsNames.INVERSE_FUNCTION_PREFERRED
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CheckInverseMethodRuleFixTest : FixTestBase("test/paragraph5/method_call_names", ::CheckInverseMethodRule) {
    @Test
    @Tag(INVERSE_FUNCTION_PREFERRED)
    fun `should fix method calls`() {
        fixAndCompare("ReplaceMethodCallNamesExpected.kt", "ReplaceMethodCallNamesTest.kt")
    }
}
