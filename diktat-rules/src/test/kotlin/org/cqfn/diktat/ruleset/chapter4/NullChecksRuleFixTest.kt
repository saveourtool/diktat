package org.cqfn.diktat.ruleset.chapter4

import org.cqfn.diktat.ruleset.rules.chapter4.NullChecksRule
import org.cqfn.diktat.util.FixTestBase

import org.cqfn.diktat.ruleset.constants.WarningsNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class NullChecksRuleFixTest : FixTestBase("test/paragraph4/null_checks", ::NullChecksRule) {
    @Test
    @Tag(WarningNames.AVOID_NULL_CHECKS)
    fun `should careful fix if conditions with break`() {
        fixAndCompare("IfConditionBreakCheckExpected.kt", "IfConditionBreakCheckTest.kt")
    }

    @Test
    @Tag(WarningNames.AVOID_NULL_CHECKS)
    fun `should fix if conditions`() {
        fixAndCompare("IfConditionNullCheckExpected.kt", "IfConditionNullCheckTest.kt")
    }

    @Test
    @Tag(WarningNames.AVOID_NULL_CHECKS)
    fun `should fix require function`() {
        fixAndCompare("RequireFunctionExpected.kt", "RequireFunctionTest.kt")
    }

    @Test
    @Tag(WarningNames.AVOID_NULL_CHECKS)
    fun `should fix if conditions when assigned`() {
        fixAndCompare("IfConditionAssignCheckExpected.kt", "IfConditionAssignCheckTest.kt")
    }
}
