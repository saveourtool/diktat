package org.cqfn.diktat.ruleset.chapter6

import generated.WarningNames
import org.cqfn.diktat.util.FixTestBase
import org.cqfn.diktat.ruleset.rules.classes.SingleInitRule
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class SingleInitRuleFixTest: FixTestBase("test/chapter6/init_blocks", ::SingleInitRule) {
    @Test
    @Tag(WarningNames.MULTIPLE_INIT_BLOCKS)
    fun `should merge init blocks`() {
        fixAndCompare("InitBlocksExpected.kt", "InitBlocksTest.kt")
    }
}
