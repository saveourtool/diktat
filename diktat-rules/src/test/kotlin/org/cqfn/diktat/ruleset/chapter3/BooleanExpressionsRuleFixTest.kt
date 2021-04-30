package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.util.FixTestBase
import org.cqfn.diktat.ruleset.rules.chapter3.BooleanExpressionsRule
import org.junit.jupiter.api.Test

class BooleanExpressionsRuleFixTest : FixTestBase("test/paragraph3/boolean_expressions", ::BooleanExpressionsRule) {
    @Test
//    @Tag TODO
    fun fixBooleanExpressions() {
        fixAndCompare("BooleanExpressionsExpected.kt", "BooleanExpressionsTest.kt")
    }
}