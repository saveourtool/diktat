package com.saveourtool.diktat.ruleset.chapter6

import com.saveourtool.diktat.ruleset.rules.chapter6.UseLastIndex
import com.saveourtool.diktat.util.FixTestBase

import org.junit.jupiter.api.Test

class UseLastIndexFixTest : FixTestBase("test/chapter6/lastIndex_change", ::UseLastIndex) {
    @Test
    fun `fix example with white spaces`() {
        fixAndCompare("UseAnyWhiteSpacesExpected.kt", "UseAnyWhiteSpacesTest.kt")
    }

    @Test
    fun `fix example with incorrect use length`() {
        fixAndCompare("IncorrectUseLengthMinusOneExpected.kt", "IncorrectUseLengthMinusOneTest.kt")
    }
}
