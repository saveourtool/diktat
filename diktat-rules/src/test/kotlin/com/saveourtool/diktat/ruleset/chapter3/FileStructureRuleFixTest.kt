package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.DIKTAT_COMMON
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter3.files.FileStructureRule
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

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
    @Tag(WarningNames.FILE_INCORRECT_BLOCKS_ORDER)
    fun `should move header kdoc before package directive`() {
        fixAndCompare("HeaderKdocAfterPackageExpected.kt", "HeaderKdocAfterPackageTest.kt")
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
                    mapOf("domainName" to "com.saveourtool.diktat")
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
    @Tag(WarningNames.FILE_INCORRECT_BLOCKS_ORDER)
    fun `invalid move in kts files`() {
        fixAndCompare("ScriptPackageDirectiveExpected.kts", "ScriptPackageDirectiveTest.kts")
    }

    @Test
    @Tag(WarningNames.FILE_NO_BLANK_LINE_BETWEEN_BLOCKS)
    fun `several empty lines after package`(@TempDir tempDir: Path) {
        val folder = tempDir.resolve("src/main/kotlin/com/saveourtool/diktat").also {
            it.createDirectories()
        }
        val testFile = folder.resolve("test.kt").also {
            it.writeText("""
                package com.saveourtool.diktat
                /**
                 * @param bar
                 * @return something
                 */
                fun foo1(bar: Bar): Baz {
                    // placeholder
                }
            """.trimIndent())
        }
        val expectedFile = folder.resolve("expected.kt").also {
            it.writeText("""
                package com.saveourtool.diktat
                /**
                 * @param bar
                 * @return something
                 */
                fun foo1(bar: Bar): Baz {
                    // placeholder
                }
            """.trimIndent())
        }
        fixAndCompare(
            expectedFile,
            testFile,
            overrideRulesConfigList = listOf(
                RulesConfig(
                    name = WarningNames.FILE_NO_BLANK_LINE_BETWEEN_BLOCKS,
                    enabled = true,
                ),
                RulesConfig(
                    name = WarningNames.WRONG_NEWLINES_AROUND_KDOC,
                    enabled = true,
                )
            ),
        )
    }
}
