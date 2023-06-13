package com.saveourtool.diktat.ruleset.chapter3.spaces

import com.saveourtool.diktat.ruleset.rules.chapter3.files.WhiteSpaceRule
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class WhiteSpaceRuleFixTest : FixTestBase("test/paragraph3/spaces", ::WhiteSpaceRule) {
    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `should keep single whitespace between keyword and opening parentheses`() {
        fixAndCompare("WhiteSpaceBeforeLParExpected.kt", "WhiteSpaceBeforeLParTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `should keep single whitespace between keyword and opening brace`() {
        fixAndCompare("LBraceAfterKeywordExpected.kt", "LBraceAfterKeywordTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `should remove spaces between ( and { when lambda is used as an argument`() {
        fixAndCompare("LambdaAsArgumentExpected.kt", "LambdaAsArgumentTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `should keep single whitespace before any other opening brace`() {
        fixAndCompare("LbraceExpected.kt", "LbraceTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `should surround binary operators with spaces`() {
        fixAndCompare("BinaryOpExpected.kt", "BinaryOpTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `should trim spaces in the end of line`() {
        fixAndCompare("EolSpacesExpected.kt", "EolSpacesTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `should fix space in annotation`() {
        fixAndCompare("AnnotationExpected.kt", "AnnotationTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `should add spaces on both sides of equals`() {
        fixAndCompare("EqualsExpected.kt", "EqualsTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `should add spaces on both sides of braces in lambda`() {
        fixAndCompare("BracesLambdaSpacesExpected.kt", "BracesLambdaSpacesTest.kt")
    }
}
