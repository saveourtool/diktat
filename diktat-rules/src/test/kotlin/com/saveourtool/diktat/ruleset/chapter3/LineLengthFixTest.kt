package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.LONG_LINE
import com.saveourtool.diktat.ruleset.rules.chapter3.LineLength
import com.saveourtool.diktat.util.FixTestBase
import org.junit.jupiter.api.Test

class LineLengthFixTest : FixTestBase("test/paragraph3/long_line", ::LineLength) {
    private val rulesConfigListLongLineLength: List<RulesConfig> = listOf(
        RulesConfig(LONG_LINE.name, true,
            mapOf("lineLength" to "175"))
    )
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
    private val rulesConfigListErrorLineLength1: List<RulesConfig> = listOf(
        RulesConfig(LONG_LINE.name, true,
            mapOf("lineLength" to "151"))
    )

    @Test
    fun `should fix long comment`() {
        fixAndCompare("LongLineCommentExpected.kt", "LongLineCommentTest.kt", rulesConfigListLineLength)
    }

    @Test
    fun `should fix long inline comments`() {
        fixAndCompare("LongInlineCommentsExpected.kt", "LongInlineCommentsTest.kt", rulesConfigListLineLength)
    }

    @Test
    fun `should fix long comment 2`() {
        fixAndCompare("LongLineCommentExpected2.kt", "LongLineCommentTest2.kt", rulesConfigListDefaultLineLength)
    }

    @Test
    fun `should fix long string template while some fix is already done`() {
        fixAndCompare("LongStringTemplateExpected.kt", "LongStringTemplateTest.kt", rulesConfigListDefaultLineLength)
    }

    @Test
    fun `should fix long binary expression`() {
        fixAndCompare("LongLineExpressionExpected.kt", "LongLineExpressionTest.kt", rulesConfigListLineLength)
    }

    @Test
    fun `should fix complex long binary expressions`() {
        fixAndCompare("LongBinaryExpressionExpected.kt", "LongBinaryExpressionTest.kt", rulesConfigListLineLength)
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

    @Test
    fun `fix condition in small function with long length`() {
        fixAndCompare("LongConditionInSmallFunctionExpected.kt", "LongConditionInSmallFunctionTest.kt", rulesConfigListLongLineLength)
    }

    @Test
    fun `fix expression in condition`() {
        fixAndCompare("LongExpressionInConditionExpected.kt", "LongExpressionInConditionTest.kt", rulesConfigListLineLength)
    }

    @Test
    fun `fix long Dot Qualified Expression`() {
        fixAndCompare("LongDotQualifiedExpressionExpected.kt", "LongDotQualifiedExpressionTest.kt", rulesConfigListLineLength)
    }

    @Test
    fun `fix long value arguments list`() {
        fixAndCompare("LongValueArgumentsListExpected.kt", "LongValueArgumentsListTest.kt", rulesConfigListLineLength)
    }

    @Test
    fun `fix bin expression first symbol last word`() {
        fixAndCompare("LongBinaryExpressionLastWordExpected.kt", "LongBinaryExpressionLastWordTest.kt", rulesConfigListErrorLineLength1)
    }

    @Test
    fun `fix bin 1expression first symbol last word`() {
        fixAndCompare("LongComplexExpressionExpected.kt", "LongComplexExpressionTest.kt", rulesConfigListErrorLineLength1)
    }
}
