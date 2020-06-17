package com.huawei.rri.fixbot.ruleset.huawei.utils

import com.pinterest.ktlint.core.Rule
import config.rules.RulesConfig
import org.junit.Assert
import test_framework.processing.TestComparatorUnit

abstract class FixTestBase(resourceFilePath: String,
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
}
