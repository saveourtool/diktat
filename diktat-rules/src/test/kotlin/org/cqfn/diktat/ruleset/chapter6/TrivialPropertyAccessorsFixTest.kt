package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.ruleset.rules.chapter6.TrivialPropertyAccessors
import org.cqfn.diktat.util.FixTestBase

import org.cqfn.diktat.ruleset.constants.WarningsNames.TRIVIAL_ACCESSORS_ARE_NOT_RECOMMENDED
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TrivialPropertyAccessorsFixTest : FixTestBase("test/chapter6/properties", ::TrivialPropertyAccessors) {
    @Test
    @Tag(TRIVIAL_ACCESSORS_ARE_NOT_RECOMMENDED)
    fun `fix trivial setters and getters`() {
        fixAndCompare("TrivialPropertyAccessorsExpected.kt", "TrivialPropertyAccessorsTest.kt")
    }
}
