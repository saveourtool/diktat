package org.cqfn.diktat.ruleset.chapter2

import generated.WarningNames
import org.cqfn.diktat.ruleset.rules.kdoc.KdocFormatting
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test

class KdocFormattingFixTest: FixTestBase("test/paragraph2/kdoc/", ::KdocFormatting) {

    @Test
    @Tag(WarningNames.BLANK_LINE_AFTER_KDOC)
    fun `there should be no blank line between kdoc and it's declaration code`() {
        fixAndCompare("KdocEmptyLineExpected.kt", "KdocEmptyLineTest.kt")
    }

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
}
