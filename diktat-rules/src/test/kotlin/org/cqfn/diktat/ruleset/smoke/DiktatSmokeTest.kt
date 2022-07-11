package org.cqfn.diktat.ruleset.smoke

import org.cqfn.diktat.common.config.rules.DIKTAT_COMMON
import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NO_EMPTY_TAGS
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_CLASS_ELEMENTS
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_ON_FUNCTION
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_TOP_LEVEL
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import org.cqfn.diktat.ruleset.rules.chapter2.comments.CommentsRule
import org.cqfn.diktat.ruleset.rules.chapter2.kdoc.KdocComments
import org.cqfn.diktat.ruleset.rules.chapter2.kdoc.KdocFormatting
import org.cqfn.diktat.ruleset.rules.chapter2.kdoc.KdocMethods
import org.cqfn.diktat.util.assertEquals

import com.pinterest.ktlint.core.LintError
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

import java.io.File

/**
 * Test for [DiktatRuleSetProvider] in autocorrect mode as a whole. All rules are applied to a file.
 * Note: ktlint uses initial text from a file to calculate line and column from offset. Because of that line/col of unfixed errors
 * may change after some changes to text or other rules.
 */
class DiktatSmokeTest : DiktatSmokeTestBase() {
    override fun fixAndCompareBase(
        config: String,
        test: String,
        expected: String
    ) {
        fixAndCompareSmokeTest(test, expected)
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test with multiplatform project layout`() {
        fixAndCompareSmokeTest("../../jsMain/kotlin/org/cqfn/diktat/scripts/ScriptExpected.kt",
            "../../jsMain/kotlin/org/cqfn/diktat/scripts/ScriptTest.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test with kts files`() {
        overrideRulesConfig(
            emptyList(),
            mapOf(
                WRONG_INDENTATION.name to mapOf(
                    "newlineAtEnd" to "false",
                    "extendedIndentOfParameters" to "false",
                )
            )
        )  // so that trailing newline isn't checked, because it's incorrectly read in tests and we are comparing file with itself
        // file name is `gradle_` so that IDE doesn't suggest to import gradle project
        val tmpTestFile = javaClass.classLoader
            .getResource("$resourceFilePath/../../../build.gradle_.kts")!!
            .toURI()
            .let {
                val tmpTestFile = File(it).parentFile.resolve("build.gradle.kts")
                File(it).copyTo(tmpTestFile)
                tmpTestFile
            }
        val tmpFilePath = "../../../build.gradle.kts"
        fixAndCompare(tmpFilePath, tmpFilePath)
        Assertions.assertTrue(unfixedLintErrors.isEmpty())
        tmpTestFile.delete()
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test with gradle script plugin`() {
        fixAndCompareSmokeTest("kotlin-library-expected.gradle.kts", "kotlin-library.gradle.kts")
        Assertions.assertEquals(
            LintError(2, 1, "$DIKTAT_RULE_SET_ID:${CommentsRule.NAME_ID}", "[COMMENTED_OUT_CODE] you should not comment out code, " +
                "use VCS to save it in history and delete this block: import org.jetbrains.kotlin.gradle.dsl.jvm", false),
            unfixedLintErrors.single()
        )
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `disable charters`() {
        overrideRulesConfig(
            emptyList(),
            mapOf(
                DIKTAT_COMMON to mapOf(
                    "domainName" to "org.cqfn.diktat",
                    "disabledChapters" to "Naming,3,4,5,Classes"
                )
            )
        )
        fixAndCompareSmokeTest("Example1-2Expected.kt", "Example1Test.kt")
        unfixedLintErrors.assertEquals(
            LintError(1, 1, "$DIKTAT_RULE_SET_ID:${KdocFormatting.NAME_ID}", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false),
            LintError(3, 1, "$DIKTAT_RULE_SET_ID:${KdocComments.NAME_ID}", "${MISSING_KDOC_TOP_LEVEL.warnText()} example", false),
            LintError(3, 16, "$DIKTAT_RULE_SET_ID:${KdocComments.NAME_ID}", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} isValid", false),
            LintError(6, 5, "$DIKTAT_RULE_SET_ID:${KdocComments.NAME_ID}", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} foo", false),
            LintError(6, 5, "$DIKTAT_RULE_SET_ID:${KdocMethods.NAME_ID}", "${MISSING_KDOC_ON_FUNCTION.warnText()} foo", false),
            LintError(8, 5, "$DIKTAT_RULE_SET_ID:${KdocComments.NAME_ID}", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} foo", false),
            LintError(10, 4, "$DIKTAT_RULE_SET_ID:${KdocFormatting.NAME_ID}", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false),
            LintError(13, 9, "$DIKTAT_RULE_SET_ID:${KdocFormatting.NAME_ID}", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false),
            LintError(18, 40, "$DIKTAT_RULE_SET_ID:${KdocFormatting.NAME_ID}", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false)
        )
    }
}
