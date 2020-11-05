package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.util.FixTestBase
import org.cqfn.diktat.ruleset.rules.TrivialPropertyAccessors
import org.junit.jupiter.api.Test

class TrivialPropertyAccessorsFixTest : FixTestBase("test/chapter6/properties", ::TrivialPropertyAccessors) {
    @Test
    fun `fix trivial setters and getters`() {
        fixAndCompare("TrivialPropertyAccessorsExpected.kt", "TrivialPropertyAccessorsTest.kt")
    }
}