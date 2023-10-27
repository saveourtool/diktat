package com.saveourtool.diktat.ruleset.chapter2

import com.saveourtool.diktat.ruleset.rules.chapter2.kdoc.KdocFormatting
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test

class KdocFormattingFixTest : FixTestBase("test/paragraph2/kdoc/", ::KdocFormatting) {
    @Test
    @Tag(WarningNames.KDOC_WRONG_SPACES_AFTER_TAG)
    fun `there should be exactly one white space after tag name`() {
        fixAndCompare("SpacesAfterTagExpected.kt", "SpacesAfterTagTest.kt")
    }

    @Test
    @Tag(WarningNames.KDOC_WRONG_TAGS_ORDER)
    fun `basic tags should be ordered in KDocs`() {
        fixAndCompare("OrderedTagsExpected.kt", "OrderedTagsTest.kt")
    }

    @Test
    @Tag(WarningNames.KDOC_WRONG_TAGS_ORDER)
    fun `extra new line with tags ordering should not cause assert`() {
        fixAndCompare("OrderedTagsAssertionExpected.kt", "OrderedTagsAssertionTest.kt")
    }

    @Test
    @Tag(WarningNames.KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS)
    fun `basic tags should not have empty lines between`() {
        fixAndCompare("BasicTagsEmptyLinesExpected.kt", "BasicTagsEmptyLinesTest.kt")
    }

    @Test
    @Tag(WarningNames.KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS)
    fun `special tags should have newline after them`() {
        fixAndCompare("SpecialTagsInKdocExpected.kt", "SpecialTagsInKdocTest.kt")
    }

    @Test
    @Tag(WarningNames.KDOC_NO_DEPRECATED_TAG)
    fun `@deprecated tag should be substituted with annotation`() {
        fixAndCompare("DeprecatedTagExpected.kt", "DeprecatedTagTest.kt")
    }

    @Test
    @Tag(WarningNames.KDOC_NEWLINES_BEFORE_BASIC_TAGS)
    fun `Empty line should be added before block of standard tags`() {
        fixAndCompare("BasicTagsEmptyLineBeforeExpected.kt", "BasicTagsEmptyLineBeforeTest.kt")
    }

    @Test
    @Tags(Tag(WarningNames.KDOC_NO_DEPRECATED_TAG), Tag(WarningNames.KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS))
    fun `KdocFormatting - all warnings`() {
        fixAndCompare("KdocFormattingFullExpected.kt", "KdocFormattingFullTest.kt")
    }

    @Test
    @Tags(Tag(WarningNames.KDOC_NO_DEPRECATED_TAG), Tag(WarningNames.KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS))
    fun `KdocFormatting - sort order`() {
        fixAndCompare("KdocFormattingOrderExpected.kt", "KdocFormattingOrderTest.kt")
    }
}
