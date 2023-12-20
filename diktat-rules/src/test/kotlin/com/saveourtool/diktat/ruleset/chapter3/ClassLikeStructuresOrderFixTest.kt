package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.ruleset.rules.chapter3.ClassLikeStructuresOrderRule
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test

class ClassLikeStructuresOrderFixTest : FixTestBase("test/paragraph3/file_structure", ::ClassLikeStructuresOrderRule) {
    @Test
    @Tags(Tag(WarningNames.BLANK_LINE_BETWEEN_PROPERTIES), Tag(WarningNames.WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES))
    fun `should fix order and newlines between properties`() {
        fixAndCompare("DeclarationsInClassOrderExpected.kt", "DeclarationsInClassOrderTest.kt")
    }

    @Test
    @Tags(Tag(WarningNames.BLANK_LINE_BETWEEN_PROPERTIES), Tag(WarningNames.WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES))
    fun `should fix order and newlines with comment`() {
        fixAndCompare("OrderWithCommentExpected.kt", "OrderWithCommentTest.kt")
    }

    @Test
    @Tags(Tag(WarningNames.WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES))
    fun `regression - should not remove enum members`() {
        fixAndCompare("OrderWithEnumsExpected.kt", "OrderWithEnumsTest.kt")
    }

    @Test
    @Tags(Tag(WarningNames.WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES))
    fun `regression - should not move loggers that depend on other variables from scope`() {
        fixAndCompare("LoggerOrderExpected.kt", "LoggerOrderTest.kt")
    }

    @Test
    @Tag(WarningNames.BLANK_LINE_BETWEEN_PROPERTIES)
    fun `should add new line before the comment`() {
        fixAndCompare("CompanionObjectWithCommentExpected.kt", "CompanionObjectWithCommentTest.kt")
    }
}
