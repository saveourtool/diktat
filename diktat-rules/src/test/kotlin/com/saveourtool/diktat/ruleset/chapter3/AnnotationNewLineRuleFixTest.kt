package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.ruleset.rules.chapter3.AnnotationNewLineRule
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class AnnotationNewLineRuleFixTest : FixTestBase("test/paragraph3/annotations", ::AnnotationNewLineRule) {
    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `should fix func and class annotations`() {
        fixAndCompare("AnnotationSingleLineExpected.kt", "AnnotationSingleLineTest.kt")
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `should fix constructor annotations`() {
        fixAndCompare("AnnotationConstructorSingleLineExpected.kt", "AnnotationConstructorSingleLineTest.kt")
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `shouldn't fix correct annotation with comment`() {
        fixAndCompare("AnnotationCommentExpected.kt", "AnnotationCommentTest.kt")
    }
}
