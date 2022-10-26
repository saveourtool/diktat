package org.cqfn.diktat.util

import org.cqfn.diktat.test.framework.processing.FileComparisonResult
import org.cqfn.diktat.test.framework.processing.TestComparatorUnit
import com.pinterest.ktlint.core.RuleSetProvider
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions
import java.nio.file.Path
import java.nio.file.Paths
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
        val expectedPathResource = requireNotNull(javaClass.classLoader.getResource("$resourceFilePath/$expectedPath")) {
            "Not able to find a file for running test: $expectedPath"
        }
        val testPathResource = requireNotNull(javaClass.classLoader.getResource("$resourceFilePath/$testPath")) {
            "Not able to find a file for running test: $testPath"
        }

        val testComparatorUnit = TestComparatorUnit(resourceFilePath) { text, fileName ->
            format(
                ruleSetProviderRef = { ruleSetProvider },
                text = text,
                fileName = fileName,
                cb = cb
            )
        }
        Assertions.assertTrue(
            testComparatorUnit
                .compareFilesFromFileSystem(Paths.get(expectedPathResource.toURI()), Paths.get(testPathResource.toURI()), trimLastEmptyLine)
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

        val testComparatorUnit = TestComparatorUnit(resourceFilePath) { text, fileName ->
            format(
                ruleSetProviderRef = { ruleSetProvider },
                text = text,
                fileName = fileName,
                cb = cb
            )
        }
        return testComparatorUnit
            .compareFilesFromFileSystem(expected, actual, false)
    }
}
