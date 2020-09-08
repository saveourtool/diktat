package org.cqfn.diktat.util

import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.test.framework.processing.TestComparatorUnit
import org.junit.jupiter.api.Assertions

open class FixTestBase(private val resourceFilePath: String,
                       private val ruleSupplier: (rulesConfigList: List<RulesConfig>) -> Rule,
                       rulesConfigList: List<RulesConfig> = emptyList(),
                       private val cb: (LintError, Boolean) -> Unit = defaultCallback
) {
    constructor(resourceFilePath: String, rule: Rule, rulesConfigList: List<RulesConfig>? = emptyList())
            : this(resourceFilePath, DiktatRuleSetProvider4Test(rule, rulesConfigList).get())

    private val testComparatorUnit = TestComparatorUnit(resourceFilePath) { text, fileName ->
        format(ruleSupplier, text, fileName, rulesConfigList, cb = cb)
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
////             fixme: use all rules after https://github.com/cqfn/diKTat/pull/208 is merged
//            DiktatRuleSetProvider4Test(ruleSet.rules[0], overrideRulesConfigList)
//                    .get()
//                    .format(text, fileName)
        }
        Assertions.assertTrue(
                testComparatorUnit
                        .compareFilesFromResources(expectedPath, testPath)
        )
    }
}
