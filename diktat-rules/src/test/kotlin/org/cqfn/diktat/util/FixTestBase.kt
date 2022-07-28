package org.cqfn.diktat.util

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.test.framework.processing.FileComparisonResult
import org.cqfn.diktat.test.framework.processing.TestComparatorUnit
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSetProvider
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions
import java.nio.file.Path
import kotlin.io.path.bufferedWriter
import kotlin.io.path.div

const val SAVE_VERSION: String = "0.3.2"

/**
 * @property resourceFilePath path to files which will be compared in tests
 */
open class FixTestBase(
    protected val resourceFilePath: String,
    private val ruleSetProviderRef: (rulesConfigList: List<RulesConfig>?) -> RuleSetProvider,
    private val cb: LintErrorCallback = defaultCallback,
    private val rulesConfigList: List<RulesConfig>? = null,
) {
    /**
     * testComparatorUnit
     */
    protected val testComparatorUnit = TestComparatorUnit(resourceFilePath) { text, fileName ->
        format(ruleSetProviderRef, text, fileName, rulesConfigList, cb = cb)
    }

    constructor(resourceFilePath: String,
                ruleSupplier: (rulesConfigList: List<RulesConfig>) -> Rule,
                rulesConfigList: List<RulesConfig>? = null,
                cb: LintErrorCallback = defaultCallback
    ) : this(
        resourceFilePath,
        { overrideRulesConfigList -> DiktatRuleSetProvider4Test(ruleSupplier, overrideRulesConfigList) },
        cb,
        rulesConfigList
    )

    /**
     * @param expectedPath path to file with expected result, relative to [resourceFilePath]
     * @param testPath path to file with code that will be transformed by formatter, relative to [resourceFilePath]
     */
    protected fun fixAndCompare(expectedPath: String, testPath: String) {
        Assertions.assertTrue(
            testComparatorUnit
                .compareFilesFromResources(expectedPath, testPath)
        )
    }

    private fun getSaveForCurrentOs() = when {
        System.getProperty("os.name").startsWith("Linux", ignoreCase = true) -> "save-$SAVE_VERSION-linuxX64.kexe"
        System.getProperty("os.name").startsWith("Mac", ignoreCase = true) -> "save-$SAVE_VERSION-macosX64.kexe"
        System.getProperty("os.name").startsWith("Windows", ignoreCase = true) -> "save-$SAVE_VERSION-mingwX64.exe"
        else -> ""
    }

    /**
     * @param testPath path to file with code that will be transformed by formatter, relative to [resourceFilePath]
     * @return ProcessBuilder
     */
    protected fun createProcessBuilderWithCmd(testPath: String): ProcessBuilder {
        val filesDir = "src/test/resources/test/smoke"
        val savePath = "$filesDir/${getSaveForCurrentOs()}"

        val systemName = System.getProperty("os.name")
        val result = when {
            systemName.startsWith("Linux", ignoreCase = true) || systemName.startsWith("Mac", ignoreCase = true) ->
                ProcessBuilder("sh", "-c", "chmod 777 $savePath ; ./$savePath $filesDir/src/main/kotlin $testPath --log all")
            else -> ProcessBuilder(savePath, "$filesDir/src/main/kotlin", testPath)
        }
        return result
    }

    /**
     * @param expectedPath path to file with expected result, relative to [resourceFilePath]
     * @param testPath path to file with code that will be transformed by formatter, relative to [resourceFilePath]
     * @param overrideRulesConfigList optional override to [rulesConfigList]
     * @see fixAndCompareContent
     */
    protected fun fixAndCompare(expectedPath: String,
                                testPath: String,
                                overrideRulesConfigList: List<RulesConfig>
    ) {
        val testComparatorUnit = TestComparatorUnit(resourceFilePath) { text, fileName ->
            format(ruleSetProviderRef, text, fileName, overrideRulesConfigList)
        }
        Assertions.assertTrue(
            testComparatorUnit
                .compareFilesFromResources(expectedPath, testPath)
        )
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

        val testComparatorUnit = TestComparatorUnit(tempDir.toString()) { text, fileName ->
            format(ruleSetProviderRef, text, fileName, overrideRulesConfigList ?: rulesConfigList, cb)
        }

        return testComparatorUnit.compareFilesFromFileSystem(expected, actual)
    }
}
