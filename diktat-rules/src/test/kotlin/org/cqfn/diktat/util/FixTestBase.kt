package org.cqfn.diktat.util

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.test.framework.processing.TestComparatorUnit
import org.junit.jupiter.api.Assertions

open class FixTestBase(private val resourceFilePath: String, private val ruleSet: RuleSet) {
    constructor(resourceFilePath: String, rule: Rule, rulesConfigList: List<RulesConfig>? = emptyList())
            : this(resourceFilePath, DiktatRuleSetProvider4Test(rule, rulesConfigList).get())

    private val testComparatorUnit = TestComparatorUnit(resourceFilePath) { text, fileName ->
        ruleSet.format(text, fileName)
    }

    protected fun fixAndCompare(expectedPath: String, testPath: String) {
        Assertions.assertTrue(
                testComparatorUnit
                        .compareFilesFromResources(expectedPath, testPath)
        )
    }

    protected fun fixAndCompare(expectedPath: String, testPath: String, overrideRulesConfigList: List<RulesConfig>) {
        val testComparatorUnit = TestComparatorUnit(resourceFilePath) { text, fileName ->
            // fixme: use all rules after https://github.com/cqfn/diKTat/pull/208 is merged
            DiktatRuleSetProvider4Test(ruleSet.rules[0], overrideRulesConfigList)
                    .get()
                    .format(text, fileName)
        }
        Assertions.assertTrue(
                testComparatorUnit
                        .compareFilesFromResources(expectedPath, testPath)
        )
    }
}
