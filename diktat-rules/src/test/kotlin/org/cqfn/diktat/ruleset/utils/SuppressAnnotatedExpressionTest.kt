package org.cqfn.diktat.ruleset.utils

import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter3.CollapseIfStatementsRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
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
            LintError(3, 8, ruleId, "${Warnings.COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true),
            LintError(4, 12, ruleId, "${Warnings.COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
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
