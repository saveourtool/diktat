package org.cqfn.diktat.ruleset.chapter3.files

import org.cqfn.diktat.ruleset.rules.chapter3.files.TopLevelOrderRule
import org.cqfn.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TopLevelOrderRuleFixTest : FixTestBase("test/paragraph3/top_level", ::TopLevelOrderRule) {
    @Test
    @Tag(WarningNames.TOP_LEVEL_ORDER)
    fun `should fix top level order`() {
        fixAndCompare("TopLevelSortExpected.kt", "TopLevelSortTest.kt")
    }

    @Test
    @Tag(WarningNames.TOP_LEVEL_ORDER)
    fun `should fix top level order with comment`() {
        fixAndCompare("TopLevelWithCommentExpected.kt", "TopLevelWithCommentTest.kt")
    }
}
