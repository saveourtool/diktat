package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.rules.files.FileStructureRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class FileStructureRuleTestFix : FixTestBase("test/paragraph3/file_structure", FileStructureRule()) {
    @Test
    @Tag("FILE_INCORRECT_BLOCKS_ORDER")
    fun `should move @file targeted annotations after header KDoc`() {
        fixAndCompare("FileAnnotationExpected.kt", "FileAnnotationTest.kt")
    }

    @Test
    @Tag("FILE_INCORRECT_BLOCKS_ORDER")
    fun `should move copyright comment before @file targeted annotations`() {
        fixAndCompare("CopyrightCommentPositionExpected.kt", "CopyrightCommentPositionTest.kt")
    }

    @Test
    @Tag("FILE_NO_BLANK_LINE_BETWEEN_BLOCKS")
    fun `should insert blank lines between code blocks`() {
        fixAndCompare("BlankLinesBetweenBlocksExpected.kt", "MissingBlankLinesBetweenBlocksTest.kt")
    }

    @Test
    @Tag("FILE_NO_BLANK_LINE_BETWEEN_BLOCKS")
    fun `should remove redundant blank lines`() {
        fixAndCompare("BlankLinesBetweenBlocksExpected.kt", "RedundantBlankLinesBetweenBlocksTest.kt")
    }

    @Test
    @Tag("FILE_UNORDERED_IMPORTS")
    fun `should reorder imports alphabetically with saving of EOL comments`() {
        fixAndCompare("ReorderingImportsExpected.kt", "ReorderingImportsTest.kt")
    }

    @Test
    @Tag("FILE_UNORDERED_IMPORTS")
    @Disabled("not yet implemented")
    fun `should reorder imports according to recommendation 3_1`() = Unit
}
