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
    fun `simple check`() {
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
            |         someAction()
            |     } else {
            |         print("some text")
            |     }
            |} 
            """.trimMargin(),
            LintError(3, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `simple check 2`() {
        lintMethod(
            """
            |fun foo () {
            |     if (cond1) {
            |         if (cond2) {
            |             firstAction()
            |             second()
            |         }
            |         if (cond3) {
            |             secondAction()
            |         }
            |     }
            |} 
            """.trimMargin(),
            LintError(3, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
        )
    }

    // TODO: Add more checks for nested if statements with comments
    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `comments check`() {
        lintMethod(
            """
            |fun foo () {
            |     if (true) {
            |         /**
            |          * Some Comments
            |          */
            |         /*
            |          More comments
            |         */
            |         // Even more comments
            |         if (true) {
            |             doSomething()
            |         }
            |     }
            |} 
            """.trimMargin(),
            LintError(10, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `not nested if`() {
        lintMethod(
            """
            |fun foo () {
            |     if (true) {
            |         val someConstant = 5
            |         if (true) {
            |             doSomething()
            |         }
            |     }    
            |}
            """.trimMargin()
        )
    }

    // FIXME:
    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `three if statements`() {
        lintMethod(
            """
            |fun foo () {
            |     if (true) {
            |         if (true) {
            |             if (true) {
            |                 doSomething()
            |             }    
            |         }
            |     }    
            |}
            """.trimMargin(),
            LintError(3, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
        )
    }
}
