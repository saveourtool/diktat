package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.rules.files.FileStructureRule
import org.cqfn.diktat.ruleset.utils.FixTestBase
import org.junit.Ignore
import org.junit.Test

class FileStructureRuleTestFix : FixTestBase("test/paragraph3/file_structure", FileStructureRule()) {
    @Test
    fun `should move @file targeted annotations before package directive`() {
        fixAndCompare("FileAnnotationExpected.kt", "FileAnnotationTest.kt")
    }

    @Test
    fun `should insert blank lines between code blocks`() {
        fixAndCompare("BlankLinesBetweenBlocksExpected.kt", "MissingBlankLinesBetweenBlocksTest.kt")
    }

    @Test
    fun `should remove redundant blank lines`() {
        fixAndCompare("BlankLinesBetweenBlocksExpected.kt", "RedundantBlankLinesBetweenBlocksTest.kt")
    }

    @Test
    fun `should reorder imports alphabetically with saving of EOL comments`() {
        fixAndCompare("ReorderingImportsExpected.kt", "ReorderingImportsTest.kt")
    }

    @Test
    @Ignore("not yet implemented")
    fun `should reorder imports according to recommendation 3_1`() = Unit
}
