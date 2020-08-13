package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.LONG_LINE
import org.cqfn.diktat.ruleset.rules.LineLength
import org.cqfn.diktat.util.FixTestBase
import org.junit.Test

class LineLengthFixTest : FixTestBase("test/paragraph3/long_line", LineLength()) {

    private val rulesConfigListLineLength: List<RulesConfig> = listOf(
            RulesConfig(LONG_LINE.name, true,
                    mapOf("lineLength" to "50"))
    )

    @Test
    fun `should fix long comment`() {
        fixAndCompare("LongLineCommentExpected.kt", "LongLineCommentTest.kt", rulesConfigListLineLength)
    }

    @Test
    fun `should fix long binary expression`() {
        fixAndCompare("LongLineExpressionExpected.kt", "LongLineExpressionTest.kt", rulesConfigListLineLength)
    }

    @Test
    fun `should fix long function`() {
        fixAndCompare("LongLineFunExpected.kt", "LongLineFunTest.kt", rulesConfigListLineLength)
    }

    @Test
    fun `should fix long right value`() {
        fixAndCompare("LongLineRValueExpected.kt", "LongLineRValueTest.kt", rulesConfigListLineLength)
    }
}
