package org.cqfn.diktat.util

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.utils.FormatCallback
import org.cqfn.diktat.ruleset.utils.defaultCallback
import org.cqfn.diktat.ruleset.utils.format
import org.cqfn.diktat.test.framework.processing.FileComparisonResult
import org.cqfn.diktat.test.framework.processing.TestComparatorUnit
import com.pinterest.ktlint.core.Rule
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions
import java.nio.file.Path
import kotlin.io.path.bufferedWriter
import kotlin.io.path.div

/**
 * Base class for FixTest
 */
open class FixTestBase(
    resourceFilePath: String,
    ruleSupplier: (rulesConfigList: List<RulesConfig>) -> Rule,
    defaultRulesConfigList: List<RulesConfig>? = null,
    cb: FormatCallback = defaultCallback,
) {
    /**
     * testComparatorUnit
     */
    private val testComparatorUnitSupplier = { overrideRulesConfigList: List<RulesConfig>? ->
        TestComparatorUnit(
            resourceFilePath = resourceFilePath,
            function = { expectedText, testFilePath ->
                format(
                    ruleSetProviderRef = { DiktatRuleSetProvider4Test(ruleSupplier, overrideRulesConfigList ?: defaultRulesConfigList) },
                    text = expectedText,
                    fileName = testFilePath,
                    cb = cb,
                )
            },
        )
    }

    /**
     * @param expectedPath path to file with expected result, relative to [resourceFilePath]
     * @param testPath path to file with code that will be transformed by formatter, relative to [resourceFilePath]
     * @param overrideRulesConfigList optional override to [defaultRulesConfigList]
     * @param trimLastEmptyLine whether the last (empty) line should be
     *   discarded when reading the content of [testPath].
     * @param replacements a map of replacements which will be applied to [expectedPath] and [testPath] before comparing.
     * @see fixAndCompareContent
     */
    protected fun fixAndCompare(
        expectedPath: String,
        testPath: String,
        overrideRulesConfigList: List<RulesConfig>? = null,
        trimLastEmptyLine: Boolean = false,
        replacements: Map<String, String> = emptyMap(),
    ) {
        val testComparatorUnit = testComparatorUnitSupplier(overrideRulesConfigList)
        val result = testComparatorUnit
            .compareFilesFromResources(expectedPath, testPath, trimLastEmptyLine, replacements)
        Assertions.assertTrue(
            result.isSuccessful
        ) {
            "Detected delta: ${result.delta}"
        }
    }

    /**
     * Unlike [fixAndCompare], this method doesn't perform any assertions.
     *
     * @param actualContent the original file content (may well be modified as
     *   fixes are applied).
     * @param expectedContent the content the file is expected to have after the
     *   fixes are applied.
     * @param tempDir the temporary directory (usually injected by _JUnit_).
     * @param overrideRulesConfigList an optional override for [rulesConfigList]
     *   (the class-wide configuration).
     * @return the result of file content comparison.
     * @see fixAndCompare
     */
    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    protected fun fixAndCompareContent(
        @Language("kotlin") actualContent: String,
        @Language("kotlin") expectedContent: String = actualContent,
        tempDir: Path,
        overrideRulesConfigList: List<RulesConfig>? = null
    ): FileComparisonResult {
        val actual = tempDir / "actual.kt"
        actual.bufferedWriter().use { out ->
            out.write(actualContent)
        }

        val expected = tempDir / "expected.kt"
        expected.bufferedWriter().use { out ->
            out.write(expectedContent)
        }

        val testComparatorUnit = testComparatorUnitSupplier(overrideRulesConfigList)
        return testComparatorUnit
            .compareFilesFromFileSystem(expected, actual, false)
    }
}
