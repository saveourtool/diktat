package org.cqfn.diktat.ruleset.chapter3.files

import org.cqfn.diktat.ruleset.rules.files.NewlinesRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.Ignore
import org.junit.Test

class NewlinesRuleFixTest : FixTestBase("test/paragraph3/newlines", NewlinesRule()) {
    @Test
    fun `should remove redundant semicolons`() {
        fixAndCompare("SemicolonsExpected.kt", "SemicolonsTest.kt")
    }

    @Test
    fun `should fix newlines near operators`() {
        fixAndCompare("OperatorsExpected.kt", "OperatorsTest.kt")
    }

    @Test
    fun `should fix newlines to follow functional style`() {
        fixAndCompare("FunctionalStyleExpected.kt", "FunctionalStyleTest.kt")
    }

    @Test
    fun `should fix empty space between identifier and opening parentheses`() {
        fixAndCompare("LParExpected.kt", "LParTest.kt")
    }

    @Test
    fun `should fix wrong newlines around comma`() {
        fixAndCompare("CommaExpected.kt", "CommaTest.kt")
    }
}
