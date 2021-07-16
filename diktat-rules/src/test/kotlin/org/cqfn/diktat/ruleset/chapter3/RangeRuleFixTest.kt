package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.rules.chapter3.RangeRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Test

class RangeRuleFixTest : FixTestBase("test/paragraph3/range", ::RangeRule) {
    @Test
    fun `should fix simple`() {
        fixAndCompare("RangeToUntilExpected.kt", "RangeToUntilTest.kt")
    }
}
