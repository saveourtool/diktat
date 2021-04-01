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
            |             secondAction()
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

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `simple check 3`() {
        lintMethod(
            """
            |fun foo () {
            |     if (true) {
            |         
            |         if (true) {
            |             doSomething()
            |         }
            |     }    
            |}
            """.trimMargin(),
            LintError(4, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
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
            LintError(3, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true),
            LintError(4, 14, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `many if statements`() {
        lintMethod(
            """
            |fun foo () {
            |     if (true) {
            |         if (true) {
            |             if (true) {
            |                 if (true) {
            |                     if (true) {
            |                         if (true) {
            |                             doSomething()
            |                         }
            |                     }
            |                 }
            |             }    
            |         }
            |     }    
            |}
            """.trimMargin(),
            LintError(3, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true),
            LintError(4, 14, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true),
            LintError(5, 18, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true),
            LintError(6, 22, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true),
            LintError(7, 26, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `else if statement`() {
        lintMethod(
            """
            |fun foo() {
            |    fun1()
            |    if (cond1) {
            |        fun2()
            |    } else if (cond2) {
            |        if (true) {
            |            fun3()
            |        }
            |    } else {
            |        fun4()
            |    }
            |}
            """.trimMargin(),
            LintError(6, 9, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true),
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `else if statement 2`() {
        lintMethod(
            """
            |fun foo() {
            |    fun1()
            |    if (cond1) {
            |        fun2()
            |    } else if (cond2) {
            |        if (true) {
            |           if (true) {
            |               fun3()
            |           } 
            |        }
            |    } else {
            |        fun4()
            |    }
            |}
            """.trimMargin(),
            LintError(6, 9, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true),
            LintError(7, 12, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true),
        )
    }
}
