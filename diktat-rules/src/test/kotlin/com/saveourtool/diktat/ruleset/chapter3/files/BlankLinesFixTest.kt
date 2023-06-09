package com.saveourtool.diktat.ruleset.chapter3.files

import com.saveourtool.diktat.ruleset.rules.chapter3.files.BlankLinesRule
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class BlankLinesFixTest : FixTestBase("test/paragraph3/blank_lines", ::BlankLinesRule) {
    @Test
    @Tag(WarningNames.TOO_MANY_BLANK_LINES)
    fun `should remove redundant blank lines`() {
        fixAndCompare("RedundantBlankLinesExpected.kt", "RedundantBlankLinesTest.kt")
    }

    @Test
    @Tag(WarningNames.TOO_MANY_BLANK_LINES)
    fun `should remove blank lines in the beginning and at the end of code block`() {
        fixAndCompare("CodeBlockWithBlankLinesExpected.kt", "CodeBlockWithBlankLinesTest.kt")
    }

    @Test
    @Tag(WarningNames.TOO_MANY_BLANK_LINES)
    fun `should remove empty line before the closing quote`() {
        fixAndCompare("RedundantBlankLinesAtTheEndOfBlockExpected.kt", "RedundantBlankLinesAtTheEndOfBlockTest.kt")
    }
}
