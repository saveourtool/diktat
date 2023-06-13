package com.saveourtool.diktat.ruleset.utils

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter3.CollapseIfStatementsRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import org.junit.jupiter.api.Test

class SuppressAnnotatedExpressionTest : LintTestBase(::CollapseIfStatementsRule) {
    private val ruleId: String = "$DIKTAT_RULE_SET_ID:${CollapseIfStatementsRule.NAME_ID}"

    @Test
    fun `should lint errors without suppress`() {
        val code =
            """
                |fun foo() {
                |   if (true) {
                |       if (true) {
                |           if (true) {
                |
                |           }
                |       }
                |   }
                |}
            """.trimMargin()
        lintMethod(code,
            DiktatError(3, 8, ruleId, "${Warnings.COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true),
            DiktatError(4, 12, ruleId, "${Warnings.COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
        )
    }

    @Test
    fun `should suppress annotated expressions`() {
        val code =
            """
                |fun foo() {
                |   if (true) {
                |       @Suppress("COLLAPSE_IF_STATEMENTS")
                |       if (true) {
                |           if (true) {
                |
                |           }
                |       }
                |   }
                |}
            """.trimMargin()
        lintMethod(code)
    }
}
