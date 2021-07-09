package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.common.config.rules.DIKTAT_COMMON
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.chapter3.files.FileStructureRule
import org.cqfn.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class FileStructureRuleFixTest : FixTestBase("test/paragraph3/file_structure", ::FileStructureRule) {
    @Test
    @Tag(WarningNames.FILE_INCORRECT_BLOCKS_ORDER)
    fun `should move @file targeted annotations after header KDoc`() {
        fixAndCompare("FileAnnotationExpected.kt", "FileAnnotationTest.kt")
    }

    @Test
    @Tag(WarningNames.FILE_INCORRECT_BLOCKS_ORDER)
    fun `should move copyright comment before @file targeted annotations`() {
        fixAndCompare("CopyrightCommentPositionExpected.kt", "CopyrightCommentPositionTest.kt")
    }

    @Test
    @Tag(WarningNames.FILE_NO_BLANK_LINE_BETWEEN_BLOCKS)
    fun `should insert blank lines between code blocks`() {
        fixAndCompare("BlankLinesBetweenBlocksExpected.kt", "MissingBlankLinesBetweenBlocksTest.kt")
    }

    @Test
    @Tag(WarningNames.FILE_NO_BLANK_LINE_BETWEEN_BLOCKS)
    fun `should remove redundant blank lines`() {
        fixAndCompare("BlankLinesBetweenBlocksExpected.kt", "RedundantBlankLinesBetweenBlocksTest.kt")
    }

    @Test
    @Tag(WarningNames.FILE_UNORDERED_IMPORTS)
    fun `should reorder imports alphabetically with saving of EOL comments`() {
        fixAndCompare(
            "ReorderingImportsExpected.kt", "ReorderingImportsTest.kt",
            overrideRulesConfigList = listOf(
                RulesConfig(
                    Warnings.FILE_UNORDERED_IMPORTS.name, true,
                    mapOf("useRecommendedImportsOrder" to "false")
                )
            )
        )
    }

    @Test
    @Tag(WarningNames.FILE_UNORDERED_IMPORTS)
    fun `should reorder imports according to recommendation 3_1`() {
        fixAndCompare(
            "ReorderingImportsRecommendedExpected.kt", "ReorderingImportsRecommendedTest.kt",
            overrideRulesConfigList = listOf(
                RulesConfig(
                    DIKTAT_COMMON, true,
                    mapOf("domainName" to "org.cqfn.diktat")
                ),
                RulesConfig(
                    Warnings.FILE_UNORDERED_IMPORTS.name, true,
                    mapOf("useRecommendedImportsOrder" to "true")
                )
            )
        )
    }

    @Test
    @Tag(WarningNames.FILE_UNORDERED_IMPORTS)
    fun `should still work with default package and some imports`() {
        fixAndCompare("DefaultPackageWithImportsExpected.kt", "DefaultPackageWithImportsTest.kt")
    }

    @Test
    @Tag(WarningNames.FILE_UNORDERED_IMPORTS)
    fun `should still work with default package and no imports`() {
        fixAndCompare("NoImportNoPackageExpected.kt", "NoImportNoPackageTest.kt")
    }

    @Test
    @Tag(WarningNames.FILE_UNORDERED_IMPORTS)
    fun `should move other comments before package node`() {
        fixAndCompare("OtherCommentsExpected.kt", "OtherCommentsTest.kt")
    }

    @Test
    @Tag(WarningNames.TOP_LEVEL_ORDER)
    fun `should fix top level order with header kdoc`() {
        fixAndCompare("MoveHeaderKdocExpected.kt", "MoveHeaderKdocTest.kt")
    }
}
