package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.rules.chapter3.BracesInConditionalsAndLoopsRule
import org.cqfn.diktat.util.FixTestBase

import org.cqfn.diktat.ruleset.constants.WarningsNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class BracesRuleFixTest : FixTestBase("test/paragraph3/braces", ::BracesInConditionalsAndLoopsRule) {
    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `should add braces to if-else statements - 1`() {
        fixAndCompare("IfElseBraces1Expected.kt", "IfElseBraces1Test.kt")
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `should add braces to loops with single statement`() {
        fixAndCompare("LoopsBracesExpected.kt", "LoopsBracesTest.kt")
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `should add braces to do-while loops with empty body`() {
        fixAndCompare("DoWhileBracesExpected.kt", "DoWhileBracesTest.kt")
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `should remove braces from single-line when branches`() {
        fixAndCompare("WhenBranchesExpected.kt", "WhenBranchesTest.kt")
    }
}
