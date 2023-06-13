package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.ruleset.rules.chapter3.BooleanExpressionsRule
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class BooleanExpressionsRuleFixTest : FixTestBase("test/paragraph3/boolean_expressions", ::BooleanExpressionsRule) {
    @Test
    @Tag(WarningNames.COMPLEX_BOOLEAN_EXPRESSION)
    fun fixBooleanExpressions() {
        fixAndCompare("BooleanExpressionsExpected.kt", "BooleanExpressionsTest.kt")
    }

    @Test
    @Tag(WarningNames.COMPLEX_BOOLEAN_EXPRESSION)
    fun `check distributive law fixing`() {
        fixAndCompare("DistributiveLawExpected.kt", "DistributiveLawTest.kt")
    }

    @Test
    @Tag(WarningNames.COMPLEX_BOOLEAN_EXPRESSION)
    fun `check same expressions`() {
        fixAndCompare("SameExpressionsInConditionExpected.kt", "SameExpressionsInConditionTest.kt")
    }

    @Test
    @Tag(WarningNames.COMPLEX_BOOLEAN_EXPRESSION)
    fun `check substitution works properly`() {
        fixAndCompare("SubstitutionIssueExpected.kt", "SubstitutionIssueTest.kt")
    }

    @Test
    @Tag(WarningNames.COMPLEX_BOOLEAN_EXPRESSION)
    fun `check ordering is persisted`() {
        fixAndCompare("OrderIssueExpected.kt", "OrderIssueTest.kt")
    }

    @Test
    @Tag(WarningNames.COMPLEX_BOOLEAN_EXPRESSION)
    fun `check handling of negative expression`() {
        fixAndCompare("NegativeExpressionExpected.kt", "NegativeExpressionTest.kt")
    }

    @Test
    @Tag(WarningNames.COMPLEX_BOOLEAN_EXPRESSION)
    fun `check expression simplification`() {
        fixAndCompare("ExpressionSimplificationExpected.kt", "ExpressionSimplificationTest.kt")
    }
}
