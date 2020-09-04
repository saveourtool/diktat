package org.cqfn.diktat.ruleset.chapter2

import generated.WarningNames
import generated.WarningNames.COMMENT_NEW_LINE_ABOVE
import generated.WarningNames.FIRST_COMMENT_NO_SPACES
import generated.WarningNames.IF_ELSE_COMMENTS
import generated.WarningNames.SPACE_BETWEEN_COMMENT_AND_CODE
import generated.WarningNames.WHITESPACE_IN_COMMENT
import org.cqfn.diktat.ruleset.rules.kdoc.KdocCodeBlocksFormatting
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test

class KdocCodeBlocksFormattingFixTest: FixTestBase("test/paragraph2/kdoc/", KdocCodeBlocksFormatting()) {

    @Test
    @Tags(Tag(WHITESPACE_IN_COMMENT), Tag(COMMENT_NEW_LINE_ABOVE), Tag(SPACE_BETWEEN_COMMENT_AND_CODE), Tag(IF_ELSE_COMMENTS), Tag(FIRST_COMMENT_NO_SPACES))
    fun `there should be no blank line between kdoc and it's declaration code`() {
        fixAndCompare("KdocCodeBlocksFormattingExpected.kt", "KdocCodeBlocksFormattingTest.kt")
    }

    @Test
    @Tags(Tag(COMMENT_NEW_LINE_ABOVE), Tag(FIRST_COMMENT_NO_SPACES))
    fun `test example from code style`() {
        fixAndCompare("KdocCodeBlockFormattingExampleExpected.kt", "KdocCodeBlockFormattingExampleTest.kt")
    }
}
