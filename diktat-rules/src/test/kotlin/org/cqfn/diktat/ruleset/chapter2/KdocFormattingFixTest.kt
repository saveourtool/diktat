package org.cqfn.diktat.ruleset.chapter2

import org.cqfn.diktat.ruleset.rules.kdoc.KdocFormatting
import org.cqfn.diktat.util.FixTestBase
import org.junit.Test

class KdocFormattingFixTest: FixTestBase("test/paragraph2/kdoc/", KdocFormatting()) {

    @Test
    fun `there should be no blank line between kdoc and it's declaration code`() {
        fixAndCompare("KdocEmptyLineExpected.kt", "KdocEmptyLineTest.kt")
    }

    @Test
    fun `there should be exactly one white space after tag name`() {
        fixAndCompare("SpacesAfterTagExpected.kt", "SpacesAfterTagTest.kt")
    }

    @Test
    fun `basic tags should be ordered in KDocs`() {
        fixAndCompare("OrderedTagsExpected.kt", "OrderedTagsTest.kt")
    }

    @Test
    fun `basic tags should not have empty lines between`() {
        fixAndCompare("BasicTagsEmptyLinesExpected.kt", "BasicTagsEmptyLinesTest.kt")
    }

    @Test
    fun `special tags should have newline after them`() {
        fixAndCompare("SpecialTagsInKdocExpected.kt", "SpecialTagsInKdocTest.kt")
    }

    @Test
    fun `@deprecated tag should be substituted with annotation`() {
        fixAndCompare("DeprecatedTagExpected.kt", "DeprecatedTagTest.kt")
    }

    @Test
    fun `Empty line should be added before block of standard tags`() {
        fixAndCompare("BasicTagsEmptyLineBeforeExpected.kt", "BasicTagsEmptyLineBeforeTest.kt")
    }

    @Test
    fun `KdocFormatting - all warnings`() {
        fixAndCompare("KdocFormattingFullExpected.kt", "KdocFormattingFullTest.kt")
    }
}
