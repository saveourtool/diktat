package org.cqfn.diktat.util

import org.cqfn.diktat.common.config.rules.RulesConfig
import com.pinterest.ktlint.core.Rule
import org.intellij.lang.annotations.Language
import java.nio.file.Path

/**
 * @property resourceFilePath path to files which will be compared in tests
 */
open class FixTestBase(
    resourceFilePath: String,
    ruleSupplier: (rulesConfigList: List<RulesConfig>) -> Rule,
    private val defaultRulesConfigList: List<RulesConfig> = emptyList(),
) : FixTestBaseCommon(
    resourceFilePath = resourceFilePath,
) {
    private val ruleSetProviderRef = { rulesConfigList: List<RulesConfig>? -> DiktatRuleSetProvider4Test(ruleSupplier, rulesConfigList ?: defaultRulesConfigList) }

    /**
     * @param expectedPath path to file with expected result, relative to [resourceFilePath]
     * @param testPath path to file with code that will be transformed by formatter, relative to [resourceFilePath]
     * @param overrideRulesConfigList optional override to [defaultRulesConfigList]
     * @param trimLastEmptyLine whether the last (empty) line should be
     *   discarded when reading the content of [testPath].
     * @see fixAndCompareContent
     */
    protected fun fixAndCompare(
        expectedPath: String,
        testPath: String,
        overrideRulesConfigList: List<RulesConfig> = emptyList(),
        trimLastEmptyLine: Boolean = false,
    ) = super.fixAndCompare(expectedPath, testPath, ruleSetProviderRef(overrideRulesConfigList), trimLastEmptyLine)

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
    ) = super.fixAndCompareContent(actualContent, expectedContent, tempDir, ruleSetProviderRef(overrideRulesConfigList))
}
