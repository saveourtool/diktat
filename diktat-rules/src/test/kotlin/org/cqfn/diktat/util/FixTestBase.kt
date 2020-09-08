package org.cqfn.diktat.util

import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSetProvider
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.test.framework.processing.TestComparatorUnit
import org.junit.jupiter.api.Assertions

open class FixTestBase(protected val resourceFilePath: String,
                       private val ruleSetProviderRef: (rulesConfigList: List<RulesConfig>?) -> RuleSetProvider,
                       private val cb: (LintError, Boolean) -> Unit = defaultCallback,
                       private val rulesConfigList: List<RulesConfig>? = null) {
    private val testComparatorUnit = TestComparatorUnit(resourceFilePath) { text, fileName ->
        format(ruleSetProviderRef, text, fileName, rulesConfigList, cb = cb)
    }

    constructor(resourceFilePath: String,
                ruleSupplier: (rulesConfigList: List<RulesConfig>) -> Rule,
                rulesConfigList: List<RulesConfig>? = null,
                cb: (LintError, Boolean) -> Unit = defaultCallback) : this(resourceFilePath,
            { overrideRulesConfigList -> DiktatRuleSetProvider4Test(ruleSupplier, overrideRulesConfigList) }, cb, rulesConfigList)

    protected fun fixAndCompare(expectedPath: String, testPath: String) {
        Assertions.assertTrue(
                testComparatorUnit
                        .compareFilesFromResources(expectedPath, testPath)
        )
    }

    protected fun fixAndCompare(expectedPath: String,
                                testPath: String,
                                overrideRulesConfigList: List<RulesConfig>) {
        val testComparatorUnit = TestComparatorUnit(resourceFilePath) { text, fileName ->
            format(ruleSetProviderRef, text, fileName, overrideRulesConfigList)
        }
        Assertions.assertTrue(
                testComparatorUnit
                        .compareFilesFromResources(expectedPath, testPath)
        )
    }
}
