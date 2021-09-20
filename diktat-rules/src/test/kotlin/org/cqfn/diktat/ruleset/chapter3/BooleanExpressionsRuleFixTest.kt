package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.rules.chapter3.BooleanExpressionsRule
import org.cqfn.diktat.util.FixTestBase

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
}
