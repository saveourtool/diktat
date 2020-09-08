package org.cqfn.diktat.util

import com.pinterest.ktlint.core.Rule
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.test.framework.processing.TestComparatorUnit
import org.junit.jupiter.api.Assertions

open class FixTestBase(private val resourceFilePath: String,
                       private val ruleSupplier: (rulesConfigList: List<RulesConfig>) -> Rule,
                       rulesConfigList: List<RulesConfig> = emptyList()
) {
    private val testComparatorUnit = TestComparatorUnit(resourceFilePath) { text, fileName ->
        format(ruleSupplier, text, fileName, rulesConfigList)
    }

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
            format(ruleSupplier, text, fileName, overrideRulesConfigList)
        }
        Assertions.assertTrue(
                testComparatorUnit
                        .compareFilesFromResources(expectedPath, testPath)
        )
    }
}
