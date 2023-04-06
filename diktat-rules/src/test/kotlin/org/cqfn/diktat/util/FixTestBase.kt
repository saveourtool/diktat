package org.cqfn.diktat.util

import org.cqfn.diktat.api.DiktatCallback
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ktlint.format
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.test.framework.processing.FileComparisonResult
import org.cqfn.diktat.test.framework.processing.ResourceReader
import org.cqfn.diktat.test.framework.processing.TestComparatorUnit
import org.cqfn.diktat.util.DiktatRuleSetProviderTest.Companion.diktatRuleSetForTest
import mu.KotlinLogging
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
    ruleSupplier: (rulesConfigList: List<RulesConfig>) -> DiktatRule,
    defaultRulesConfigList: List<RulesConfig>? = null,
    cb: DiktatCallback = defaultCallback,
) {
    /**
     * testComparatorUnit
     */
    private val testComparatorUnitSupplier = { overrideRulesConfigList: List<RulesConfig>? ->
        TestComparatorUnit(
            resourceFilePath = resourceFilePath,
            function = { testFile ->
                format(
                    ruleSetSupplier = { diktatRuleSetForTest(ruleSupplier, overrideRulesConfigList ?: defaultRulesConfigList) },
                    file = testFile,
                    cb = cb,
                )
            },
        )
    }

    /**
     * @param expectedPath path to file with expected result, relative to [resourceFilePath]
     * @param testPath path to file with code that will be transformed by formatter, relative to [resourceFilePath]
     * @param overrideRulesConfigList optional override to [defaultRulesConfigList]
     * @param resourceReader [ResourceReader] to read resource content.
     * @see fixAndCompareContent
     */
    protected fun fixAndCompare(
        expectedPath: String,
        testPath: String,
        overrideRulesConfigList: List<RulesConfig>? = null,
        resourceReader: ResourceReader = ResourceReader.default,
    ) {
        val testComparatorUnit = testComparatorUnitSupplier(overrideRulesConfigList)
        val result = testComparatorUnit
            .compareFilesFromResources(expectedPath, testPath, resourceReader)
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
            .compareFilesFromFileSystem(expected, actual)
    }

    companion object {
        private val log = KotlinLogging.logger { }

        private val defaultCallback = DiktatCallback { error, _ ->
            log.warn { "Received linting error: $error" }
        }
    }
}
