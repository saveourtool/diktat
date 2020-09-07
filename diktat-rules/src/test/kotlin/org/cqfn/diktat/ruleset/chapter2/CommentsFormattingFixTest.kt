package org.cqfn.diktat.ruleset.chapter2

import generated.WarningNames
import generated.WarningNames.COMMENT_NEW_LINES
import generated.WarningNames.COMMENT_WHITE_SPACE
import generated.WarningNames.FIRST_COMMENT_NO_SPACES
import generated.WarningNames.IF_ELSE_COMMENTS
import org.cqfn.diktat.ruleset.rules.kdoc.CommentsFormatting
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test

class CommentsFormattingFixTest: FixTestBase("test/paragraph2/kdoc/", CommentsFormatting()) {

    @Test
    @Tag(COMMENT_NEW_LINES)
    fun `there should be no blank line between kdoc and it's declaration code`() {
        fixAndCompare("KdocEmptyLineExpected.kt", "KdocEmptyLineTest.kt")
    }

    @Test
    @Tags(Tag(COMMENT_NEW_LINES), Tag(COMMENT_WHITE_SPACE), Tag(IF_ELSE_COMMENTS), Tag(FIRST_COMMENT_NO_SPACES))
    fun `check lines and spaces in comments`() {
        fixAndCompare("KdocCodeBlocksFormattingExpected.kt", "KdocCodeBlocksFormattingTest.kt")
    }

    @Test
    @Tags(Tag(COMMENT_NEW_LINES), Tag(FIRST_COMMENT_NO_SPACES))
    fun `test example from code style`() {
        fixAndCompare("KdocCodeBlockFormattingExampleExpected.kt", "KdocCodeBlockFormattingExampleTest.kt")
    }
}
