package org.cqfn.diktat.ruleset.chapter3.spaces

import org.cqfn.diktat.ruleset.rules.WhiteSpaceRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Test

class WhiteSpaceRuleFixTest : FixTestBase("test/paragraph3/spaces", WhiteSpaceRule()) {
    @Test
    fun `should keep single whitespace between keyword and opening parentheses`() {
        fixAndCompare("WhiteSpaceBeforeLParExpected.kt", "WhiteSpaceBeforeLParTest.kt")
    }

    @Test
    fun `should keep single whitespace between keyword and opening brace`() {
        fixAndCompare("LBraceAfterKeywordExpected.kt", "LBraceAfterKeywordTest.kt")
    }

    @Test
    fun `should remove spaces between ( and { when lambda is used as an argument`() {
        fixAndCompare("LambdaAsArgumentExpected.kt", "LambdaAsArgumentTest.kt")
    }

    @Test
    fun `should keep single whitespace before any other opening brace`() {
        fixAndCompare("LbraceExpected.kt", "LbraceTest.kt")
    }

    @Test
    fun `should surround binary operators with spaces`() {
        fixAndCompare("BinaryOpExpected.kt", "BinaryOpTest.kt")
    }

    @Test
    fun `should trim spaces in the end of line`() {
        fixAndCompare("EolSpacesExpected.kt", "EolSpacesTest.kt")
    }
}
