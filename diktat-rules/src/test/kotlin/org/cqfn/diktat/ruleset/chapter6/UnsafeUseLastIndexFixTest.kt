package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.ruleset.rules.chapter6.UnsafeUseLastIndex
import org.cqfn.diktat.util.FixTestBase

import org.junit.jupiter.api.Test

class UnsafeUseLastIndexFixTest : FixTestBase("test/chapter6/lastIndex_change", ::UnsafeUseLastIndex) {
    @Test
    fun `fix example with white spaces`() {
        fixAndCompare("UseAnyWhiteSpacesExpected.kt", "UseAnyWhiteSpacesTest.kt")
    }

    @Test
    fun `fix example with incorrect use length`() {
        fixAndCompare("IncorrectUseLengthMinusOneExpected.kt", "IncorrectUseLengthMinusOneTest.kt")
    }

    @Test
    fun `fix example with right use length`() {
        fixAndCompare("CorrectUseLengthExpected.kt", "CorrectUseLengthTest.kt")
    }
}
