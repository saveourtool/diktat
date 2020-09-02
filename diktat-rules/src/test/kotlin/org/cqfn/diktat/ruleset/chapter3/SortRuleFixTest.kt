package org.cqfn.diktat.ruleset.chapter3

import generated.WarningNames
import org.cqfn.diktat.ruleset.rules.SortRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class SortRuleFixTest : FixTestBase("test/paragraph3/sort_error", SortRule()) {

    @Test
    @Tag(WarningNames.SORT_RULE)
    fun `should fix enum order`() {
        fixAndCompare("EnumSortExpected.kt", "EnumSortTest.kt")
    }

    @Test
    @Tag(WarningNames.SORT_RULE)
    fun `should fix constants order`() {
        fixAndCompare("ConstantsExpected.kt", "ConstantsTest.kt")
    }
}
