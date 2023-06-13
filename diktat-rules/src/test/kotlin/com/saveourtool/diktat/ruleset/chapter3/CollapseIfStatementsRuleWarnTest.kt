package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings.COLLAPSE_IF_STATEMENTS
import com.saveourtool.diktat.ruleset.rules.chapter3.CollapseIfStatementsRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CollapseIfStatementsRuleWarnTest : LintTestBase(::CollapseIfStatementsRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${CollapseIfStatementsRule.NAME_ID}"

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `one nested if`() {
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
            DiktatError(3, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `simple check`() {
        lintMethod(
            """
            |fun foo () {
            |     if (cond1) {
            |         if (cond2) {
            |             firstAction()
            |             secondAction()
            |         }
            |     }
            |     if (cond3) {
            |         secondAction()
            |     }
            |}
            """.trimMargin(),
            DiktatError(3, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `simple check 2`() {
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
            DiktatError(4, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `nested if with incorrect indention`() {
        lintMethod(
            """
            |fun foo () {
            |     if (true) {
            |         if (true) {doSomething()}
            |     }
            |}
            """.trimMargin(),
            DiktatError(3, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `eol comment`() {
        lintMethod(
            """
            |fun foo () {
            |     if (true) {
            |         // Some
            |         // comments
            |         if (true) {
            |             doSomething()
            |         }
            |     }
            |}
            """.trimMargin(),
            DiktatError(5, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `block comment`() {
        lintMethod(
            """
            |fun foo () {
            |     if (true) {
            |         /*
            |          Some comments
            |         */
            |         if (true) {
            |             doSomething()
            |         }
            |     }
            |}
            """.trimMargin(),
            DiktatError(6, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `kdoc comment`() {
        lintMethod(
            """
            |fun foo () {
            |     if (true) {
            |         /**
            |          * Some comments
            |         */
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
    fun `combine comments`() {
        lintMethod(
            """
            |fun foo () {
            |     if (true) {
            |         /*
            |          Some Comments
            |         */
            |         // More comments
            |         if (true) {
            |             // comment 1
            |             val a = 5
            |             // comment 2
            |             doSomething()
            |         }
            |         // comment 3
            |     }
            |}
            """.trimMargin(),
            DiktatError(7, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `comments in compound cond`() {
        lintMethod(
            """
            |fun foo() {
            |     // comment
            |     if (cond1) {
            |         /*
            |          Some comments
            |         */
            |         // More comments
            |         if (cond2 || cond3) {
            |             doSomething()
            |         }
            |     }
            |}
            """.trimMargin(),
            DiktatError(8, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `comments already in cond`() {
        lintMethod(
            """
            |fun foo () {
            |     if (/*comment*/ true) {
            |         if (true) {
            |             doSomething()
            |         }
            |     }
            |}
            """.trimMargin(),
            DiktatError(3, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `comments already in complex cond`() {
        lintMethod(
            """
            |fun foo () {
            |     if (true && (true || false) /*comment*/) {
            |         if (true /*comment*/) {
            |             doSomething()
            |         }
            |     }
            |}
            """.trimMargin(),
            DiktatError(3, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `multiline comments already in cond`() {
        lintMethod(
            """
            |fun foo () {
            |     if (true
            |     /*comment
            |     * more comments
            |     */
            |     ) {
            |         if (true /*comment 2*/) {
            |             doSomething()
            |         }
            |     }
            |}
            """.trimMargin(),
            DiktatError(7, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `comments in multiple if-statements`() {
        lintMethod(
            """
            |fun foo() {
            |     if (cond1) {
            |         // comment
            |         if (cond2) {
            |             // comment 2
            |             if (cond3) {
            |                 doSomething()
            |             }
            |         }
            |     }
            |}
            """.trimMargin(),
            DiktatError(4, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true),
            DiktatError(6, 14, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
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
    fun `internal if with else branch`() {
        lintMethod(
            """
            |fun foo () {
            |     if (cond1) {
            |         if (cond2) {
            |             firstAction()
            |             secondAction()
            |         } else {
            |             firstAction()
            |         }
            |     } else {
            |         secondAction()
            |     }
            |}
            """.trimMargin(),
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `internal if with multiple else and elif branches`() {
        lintMethod(
            """
            |fun foo () {
            |     if (cond1) {
            |         if (cond2) {
            |             firstAction()
            |             secondAction()
            |         } else if (cond3) {
            |             firstAction()
            |         } else {
            |             val a = 5
            |         }
            |     } else {
            |         secondAction()
            |     }
            |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `if isn't last child`() {
        lintMethod(
            """
            |fun foo() {
            |   if (cond1) {
            |       if (cond2) {
            |            doSomething()
            |       }
            |       val a = 5
            |   }
            |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `external if with else branch`() {
        lintMethod(
            """
            |fun foo() {
            |   if (cond1) {
            |       if (cond2) {
            |            doSomething()
            |       }
            |   } else {
            |       val b = 1
            |   }
            |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `external if with else node, but exist nested if`() {
        lintMethod(
            """
            |fun foo() {
            |   val a = 0
            |   if (cond1) {
            |       if (cond2) {
            |           if (cond3) {
            |               doSomething()
            |           }
            |       }
            |   } else {
            |       val b = 1
            |   }
            |}
            """.trimMargin(),
            DiktatError(5, 12, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true),
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
            DiktatError(3, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true),
            DiktatError(4, 14, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
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
            DiktatError(3, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true),
            DiktatError(4, 14, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true),
            DiktatError(5, 18, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true),
            DiktatError(6, 22, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true),
            DiktatError(7, 26, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true)
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `not nested else if statement`() {
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
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `nested else if statement`() {
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
            DiktatError(7, 12, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true),
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `compound condition`() {
        lintMethod(
            """
            |fun foo () {
            |     if (cond1) {
            |         if (cond2 || cond3) {
            |             firstAction()
            |             secondAction()
            |         }
            |     }
            |     if (cond4) {
            |         secondAction()
            |     }
            |}
            """.trimMargin(),
            DiktatError(3, 10, ruleId, "${COLLAPSE_IF_STATEMENTS.warnText()} avoid using redundant nested if-statements", true),
        )
    }

    @Test
    @Tag(WarningNames.COLLAPSE_IF_STATEMENTS)
    fun `not nested compound condition`() {
        lintMethod(
            """
            |fun foo () {
            |     if (cond1) {
            |         if (cond2 || cond3) {
            |             firstAction()
            |             secondAction()
            |         }
            |         if (cond4) {
            |             secondAction()
            |         }
            |     }
            |}
            """.trimMargin()
        )
    }
}
