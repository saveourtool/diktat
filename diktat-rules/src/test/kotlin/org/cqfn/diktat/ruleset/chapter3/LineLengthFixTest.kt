package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.LONG_LINE
import org.cqfn.diktat.ruleset.rules.chapter3.LineLength
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Test

class LineLengthFixTest : FixTestBase("test/paragraph3/long_line", ::LineLength) {
    private val rulesConfigListDefaultLineLength: List<RulesConfig> = listOf(
        RulesConfig(LONG_LINE.name, true,
            mapOf("lineLength" to "120"))
    )
    private val rulesConfigListLineLength: List<RulesConfig> = listOf(
        RulesConfig(LONG_LINE.name, true,
            mapOf("lineLength" to "50"))
    )
    private val rulesConfigListShortLineLength: List<RulesConfig> = listOf(
        RulesConfig(LONG_LINE.name, true,
            mapOf("lineLength" to "20"))
    )

    @Test
    fun `should fix long comment`() {
        fixAndCompare("LongLineCommentExpected.kt", "LongLineCommentTest.kt", rulesConfigListLineLength)
    }

    @Test
    fun `should not fix long comment which located on the line length limit`() {
        fixAndCompare("LongLineCommentExpected2.kt", "LongLineCommentTest2.kt", rulesConfigListDefaultLineLength)
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

    @Test
    fun `should fix short long right value`() {
        fixAndCompare("LongShortRValueExpected.kt", "LongShortRValueTest.kt", rulesConfigListShortLineLength)
    }

    @Test
    fun `shouldn't fix`() {
        fixAndCompare("LongExpressionNoFixExpected.kt", "LongExpressionNoFixTest.kt", rulesConfigListShortLineLength)
    }

    @Test
    fun `should fix annotation`() {
        fixAndCompare("LongLineAnnotationExpected.kt", "LongLineAnnotationTest.kt", rulesConfigListLineLength)
    }
}
