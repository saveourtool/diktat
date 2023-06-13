package com.saveourtool.diktat.ruleset.chapter3.files

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings.TOP_LEVEL_ORDER
import com.saveourtool.diktat.ruleset.rules.chapter3.files.TopLevelOrderRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TopLevelOrderRuleWarnTest : LintTestBase(::TopLevelOrderRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${TopLevelOrderRule.NAME_ID}"

    @Test
    @Tag(WarningNames.TOP_LEVEL_ORDER)
    fun `correct order`() {
        lintMethod(
            """
                |const val CONSTANT = 42
                |val topLevelProperty = "String constant"
                |lateinit var q: String
                |fun String.foo() {}
                |fun foo() {}
                |private fun gio() {}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.TOP_LEVEL_ORDER)
    fun `wrong order`() {
        lintMethod(
            """
                |class A {}
                |lateinit var q: String
                |interface B {}
                |fun foo() {}
                |fun String.foo() {}
                |private val et = 0
                |public const val g = 9.8
                |object B {}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${TOP_LEVEL_ORDER.warnText()} class A {}", true),
            DiktatError(2, 1, ruleId, "${TOP_LEVEL_ORDER.warnText()} lateinit var q: String", true),
            DiktatError(3, 1, ruleId, "${TOP_LEVEL_ORDER.warnText()} interface B {}", true),
            DiktatError(4, 1, ruleId, "${TOP_LEVEL_ORDER.warnText()} fun foo() {}", true),
            DiktatError(5, 1, ruleId, "${TOP_LEVEL_ORDER.warnText()} fun String.foo() {}", true),
            DiktatError(6, 1, ruleId, "${TOP_LEVEL_ORDER.warnText()} private val et = 0", true),
            DiktatError(7, 1, ruleId, "${TOP_LEVEL_ORDER.warnText()} public const val g = 9.8", true),
            DiktatError(8, 1, ruleId, "${TOP_LEVEL_ORDER.warnText()} object B {}", true)
        )
    }
}
