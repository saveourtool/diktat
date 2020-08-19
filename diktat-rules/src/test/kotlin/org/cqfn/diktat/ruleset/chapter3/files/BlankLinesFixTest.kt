package org.cqfn.diktat.ruleset.chapter3.files

import org.cqfn.diktat.ruleset.rules.files.BlankLinesRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class BlankLinesFixTest : FixTestBase("test/paragraph3/blank_lines", BlankLinesRule()) {
    @Test
    @Tag("TOO_MANY_BLANK_LINES")
    fun `should remove redundant blank lines`() {
        fixAndCompare("RedundantBlankLinesExpected.kt", "RedundantBlankLinesTest.kt")
    }

    @Test
    @Tag("TOO_MANY_BLANK_LINES")
    fun `should remove blank lines in the beginning and at the end of code block`() {
        fixAndCompare("CodeBlockWithBlankLinesExpected.kt", "CodeBlockWithBlankLinesTest.kt")
    }
}
