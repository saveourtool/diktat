package org.cqfn.diktat.ruleset.chapter3.files

import org.cqfn.diktat.ruleset.constants.Warnings.TOP_LEVEL_ORDER
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter3.files.TopLevelOrderRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TopLevelOrderRuleWarnTest : LintTestBase(::TopLevelOrderRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${TopLevelOrderRule.nameId}"

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
            LintError(1, 1, ruleId, "${TOP_LEVEL_ORDER.warnText()} class A {}", true),
            LintError(2, 1, ruleId, "${TOP_LEVEL_ORDER.warnText()} lateinit var q: String", true),
            LintError(3, 1, ruleId, "${TOP_LEVEL_ORDER.warnText()} interface B {}", true),
            LintError(4, 1, ruleId, "${TOP_LEVEL_ORDER.warnText()} fun foo() {}", true),
            LintError(5, 1, ruleId, "${TOP_LEVEL_ORDER.warnText()} fun String.foo() {}", true),
            LintError(6, 1, ruleId, "${TOP_LEVEL_ORDER.warnText()} private val et = 0", true),
            LintError(7, 1, ruleId, "${TOP_LEVEL_ORDER.warnText()} public const val g = 9.8", true),
            LintError(8, 1, ruleId, "${TOP_LEVEL_ORDER.warnText()} object B {}", true)
        )
    }
}
