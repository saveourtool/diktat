package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.constants.Warnings.SINGLE_CONSTRUCTOR_SHOULD_BE_PRIMARY
import org.cqfn.diktat.ruleset.rules.chapter6.classes.SingleConstructorRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.constants.WarningsNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class SingleConstructorRuleWarnTest : LintTestBase(::SingleConstructorRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${SingleConstructorRule.NAME_ID}"

    @Test
    @Tag(WarningNames.SINGLE_CONSTRUCTOR_SHOULD_BE_PRIMARY)
    fun `should suggest to convert single constructor to primary - positive example`() {
        lintMethod(
            """
                |class Test(var a: Int) { }
                |
                |class Test private constructor(var a: Int) { }
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.SINGLE_CONSTRUCTOR_SHOULD_BE_PRIMARY)
    fun `should suggest to convert single constructor to primary`() {
        lintMethod(
            """
                |class Test {
                |    var a: Int
                |
                |    constructor(a: Int) {
                |        this.a = a
                |    }
                |}
            """.trimMargin(),
            LintError(1, 1, ruleId, "${SINGLE_CONSTRUCTOR_SHOULD_BE_PRIMARY.warnText()} in class <Test>", true)
        )
    }
}
