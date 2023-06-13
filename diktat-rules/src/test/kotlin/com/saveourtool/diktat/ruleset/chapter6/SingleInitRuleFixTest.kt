package com.saveourtool.diktat.ruleset.chapter6

import com.saveourtool.diktat.ruleset.rules.chapter6.classes.SingleInitRule
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class SingleInitRuleFixTest : FixTestBase("test/chapter6/init_blocks", ::SingleInitRule) {
    @Test
    @Tag(WarningNames.MULTIPLE_INIT_BLOCKS)
    fun `should merge init blocks`() {
        fixAndCompare("InitBlocksExpected.kt", "InitBlocksTest.kt")
    }

    @Test
    @Tag(WarningNames.MULTIPLE_INIT_BLOCKS)
    fun `should move property assignments from init blocks to declarations`() {
        fixAndCompare("InitBlockWithAssignmentsExpected.kt", "InitBlockWithAssignmentsTest.kt")
    }

    @Test
    @Tag(WarningNames.MULTIPLE_INIT_BLOCKS)
    fun `should merge init blocks and move property assignments from init blocks to declarations`() {
        fixAndCompare("InitBlocksWithAssignmentsExpected.kt", "InitBlocksWithAssignmentsTest.kt")
    }
}
