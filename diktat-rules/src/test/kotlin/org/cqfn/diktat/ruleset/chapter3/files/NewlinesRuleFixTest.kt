package org.cqfn.diktat.ruleset.chapter3.files

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.chapter3.files.NewlinesRule
import org.cqfn.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class NewlinesRuleFixTest : FixTestBase("test/paragraph3/newlines", ::NewlinesRule) {
    private val rulesConfigListShort: List<RulesConfig> = listOf(
        RulesConfig(Warnings.WRONG_NEWLINES.name, true,
            mapOf("maxCallsInOneLine" to "1"))
    )

    @Test
    @Tag(WarningNames.REDUNDANT_SEMICOLON)
    fun `should remove redundant semicolons`() {
        fixAndCompare("SemicolonsExpected.kt", "SemicolonsTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should fix newlines near operators`() {
        fixAndCompare("OperatorsExpected.kt", "OperatorsTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should fix newlines to follow functional style`() {
        fixAndCompare("FunctionalStyleExpected.kt", "FunctionalStyleTest.kt", rulesConfigListShort)
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should fix empty space between identifier and opening parentheses`() {
        fixAndCompare("LParExpected.kt", "LParTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should fix wrong newlines around comma`() {
        fixAndCompare("CommaExpected.kt", "CommaTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should fix wrong newlines in lambdas`() {
        fixAndCompare("LambdaExpected.kt", "LambdaTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should replace functions with only return with expression body`() {
        fixAndCompare("ExpressionBodyExpected.kt", "ExpressionBodyTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should insert newlines in a long parameter or supertype list`() {
        fixAndCompare("ParameterListExpected.kt", "ParameterListTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should fix one line function with and without semicolon`() {
        fixAndCompare("OneLineFunctionExpected.kt", "OneLineFunctionTest.kt")
    }
}
