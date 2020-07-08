package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.Rule
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.junit.Assert
import org.cqfn.diktat.test.framework.processing.TestComparatorUnit

open class FixTestBase(private val resourceFilePath: String,
                       protected val rule: Rule,
                       rulesConfigList: List<RulesConfig>? = emptyList()
) {
    private val testComparatorUnit = TestComparatorUnit(resourceFilePath) { text, fileName ->
        rule.format(text, fileName, rulesConfigList)
    }

    protected fun fixAndCompare(expectedPath: String, testPath: String) {
        Assert.assertTrue(
                testComparatorUnit
                        .compareFilesFromResources(expectedPath, testPath)
        )
    }

    protected fun fixAndCompare(expectedPath: String, testPath: String, overrideRulesConfigList: List<RulesConfig>) {
        val testComparatorUnit = TestComparatorUnit(resourceFilePath) { text, fileName ->
            rule.format(text, fileName, overrideRulesConfigList)
        }
        Assert.assertTrue(
                testComparatorUnit
                        .compareFilesFromResources(expectedPath, testPath)
        )
    }
}
