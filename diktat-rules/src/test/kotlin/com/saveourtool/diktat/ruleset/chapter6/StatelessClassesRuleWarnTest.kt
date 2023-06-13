package com.saveourtool.diktat.ruleset.chapter6

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter6.classes.StatelessClassesRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames.OBJECT_IS_PREFERRED
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class StatelessClassesRuleWarnTest : LintTestBase(::StatelessClassesRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${StatelessClassesRule.NAME_ID}"

    @Test
    @Tag(OBJECT_IS_PREFERRED)
    fun `should not trigger on class not extending any interface`() {
        lintMethod(
            """
                |class Some : I() {
                |   override fun some()
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(OBJECT_IS_PREFERRED)
    fun `should trigger on class extending interface`() {
        lintMethod(
            """
                |class Some : I {
                |   override fun some()
                |}
                |
                |interface I {
                |   fun some()
                |}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${Warnings.OBJECT_IS_PREFERRED.warnText()} class Some", true)
        )
    }

    @Test
    @Tag(OBJECT_IS_PREFERRED)
    fun `should not trigger on class with constructor`() {
        lintMethod(
            """
                |class Some(b: Int) : I {
                |
                |   override fun some()
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(OBJECT_IS_PREFERRED)
    fun `should not trigger on class with no interface in this file`() {
        lintMethod(
            """
                |class Some : I {
                |
                |   override fun some()
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(OBJECT_IS_PREFERRED)
    fun `should not trigger on class with state`() {
        lintMethod(
            """
                |class Some : I {
                |   val a = 5
                |   override fun some()
                |}
            """.trimMargin()
        )
    }
}
