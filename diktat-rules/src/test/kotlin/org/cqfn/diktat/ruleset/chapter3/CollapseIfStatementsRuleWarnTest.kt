package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.constants.Warnings.COLLAPSE_IF_STATEMENTS
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter3.CollapseIfStatementsRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CollapseIfStatementsRuleWarnTest : LintTestBase(::CollapseIfStatementsRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:collapse-if"

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `check if property`() {
        lintMethod(
            """
            |fun foo () {
            |     if (true) {
            |         if (false) {
            |             doSomething()
            |         }
            |     }
            |
            |     if (false) {
            |     } else {
            |         print("some text")
            |     }
            |} 
            """.trimMargin(),
            LintError(3, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
        )
    }
}
