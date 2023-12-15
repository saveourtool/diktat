package com.saveourtool.diktat.ruleset.chapter3.files

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter3.files.NewlinesRule
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class NewlinesRuleFixTest : FixTestBase("test/paragraph3/newlines", ::NewlinesRule) {
    private val rulesConfigListShort: List<RulesConfig> = listOf(
        RulesConfig(Warnings.WRONG_NEWLINES.name, true,
            mapOf("maxCallsInOneLine" to "1"))
    )

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
    fun `should fix wrong newlines before colon`() {
        fixAndCompare("ColonExpected.kt", "ColonTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `One line parameters list sheet must contain no more than 2 parameters`() {
        fixAndCompare("SizeParameterListExpected.kt", "SizeParameterListTest.kt")
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
    fun `should fix one line function`() {
        fixAndCompare("OneLineFunctionExpected.kt", "OneLineFunctionTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `list argument in lambda`() {
        fixAndCompare("ListArgumentLambdaExpected.kt", "ListArgumentLambdaTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `long dot qualified expression`() {
        fixAndCompare("LongDotQualifiedExpressionExpected.kt", "LongDotQualifiedExpressionTest.kt")
    }
}
