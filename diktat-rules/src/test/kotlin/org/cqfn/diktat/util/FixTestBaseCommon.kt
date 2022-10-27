package org.cqfn.diktat.util

import org.cqfn.diktat.test.framework.processing.FileComparisonResult
import org.cqfn.diktat.test.framework.processing.TestComparatorUnit
import org.cqfn.diktat.test.framework.util.LintErrorCallback
import org.cqfn.diktat.test.framework.util.defaultCallback
import com.pinterest.ktlint.core.RuleSetProvider
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions
import java.nio.file.Path
import kotlin.io.path.bufferedWriter
import kotlin.io.path.div

/**
 * @property resourceFilePath path to files which will be compared in tests
 */
open class FixTestBaseCommon(
    protected val resourceFilePath: String,
    private val cb: LintErrorCallback = defaultCallback,
) {
    /**
     * @param expectedPath path to file with expected result, relative to [resourceFilePath]
     * @param testPath path to file with code that will be transformed by formatter, relative to [resourceFilePath]
     * @param ruleSetProvider provider for rules which should be applied
     * @param trimLastEmptyLine whether the last (empty) line should be
     *   discarded when reading the content of [testPath].
     * @see fixAndCompareContent
     */
    protected fun fixAndCompare(
        expectedPath: String,
        testPath: String,
        ruleSetProvider: RuleSetProvider,
        trimLastEmptyLine: Boolean = false,
    ) {
        val testComparatorUnit = TestComparatorUnit(resourceFilePath, { ruleSetProvider }, cb)
        Assertions.assertTrue(
            testComparatorUnit
                .compareFilesFromResources(expectedPath, testPath, trimLastEmptyLine)
                .isSuccessful
        )
    }

    /**
     * @param actualContent the original file content (may well be modified as
     *   fixes are applied).
     * @param expectedContent the content the file is expected to have after the
     *   fixes are applied.
     * @param tempDir the temporary directory (usually injected by _JUnit_).
     * @param ruleSetProvider provider for rules which should be applied
     * @return the result of file content comparison.
     * @see fixAndCompare
     */
    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    protected fun fixAndCompareContent(
        @Language("kotlin") actualContent: String,
        @Language("kotlin") expectedContent: String = actualContent,
        tempDir: Path,
        ruleSetProvider: RuleSetProvider,
    ): FileComparisonResult {
        val actual = tempDir / "actual.kt"
        actual.bufferedWriter().use { out ->
            out.write(actualContent)
        }

        val expected = tempDir / "expected.kt"
        expected.bufferedWriter().use { out ->
            out.write(expectedContent)
        }

        val testComparatorUnit = TestComparatorUnit(resourceFilePath, { ruleSetProvider }, cb)
        return testComparatorUnit
            .compareFilesFromFileSystem(expected, actual, false)
    }
}
