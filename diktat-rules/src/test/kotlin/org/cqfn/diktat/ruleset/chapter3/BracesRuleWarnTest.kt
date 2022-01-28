package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.constants.Warnings.NO_BRACES_IN_CONDITIONALS_AND_LOOPS
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter3.BracesInConditionalsAndLoopsRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class BracesRuleWarnTest : LintTestBase(::BracesInConditionalsAndLoopsRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:braces-rule"

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `should check braces in if statement without else branch - positive example`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if (x > 0) {
                    |        bar()
                    |    }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `should check braces in if statement without else branch`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if (x > 0)
                    |        bar()
                    |        
                    |    if (y < 0) baz()
                    |}
                """.trimMargin(),
            LintError(2, 5, ruleId, "${NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnText()} IF", true),
            LintError(5, 5, ruleId, "${NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnText()} IF", true)
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `should check braces in if statements - positive example`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if (x > 0) {
                    |        bar()
                    |    } else if (x == 0) {
                    |        bzz()
                    |    } else {
                    |        baz()
                    |    }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `should check braces in if statements`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if (x > 0)
                    |        bar()
                    |    else if (x == 0)
                    |        bzz()
                    |    else
                    |        baz()
                    |}
                """.trimMargin(),
            LintError(2, 5, ruleId, "${NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnText()} IF", true),
            LintError(4, 10, ruleId, "${NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnText()} IF", true),
            LintError(6, 5, ruleId, "${NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnText()} ELSE", true)
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `should check braces in if statements - exception for single line if`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if (x > 0) bar() else baz()
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `should check braces in if statements - exception for let`() {
        lintMethod(
            """
            |fun foo() {
            |    if (a) {
            |        bar()
            |    } else b?.let {
            |        baz()
            |    }
            |        ?: run {
            |            qux()
            |        }
            |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `should check braces in if statements - exception for let 2`() {
        lintMethod(
            """
            |fun foo() {
            |    if (a) {
            |        bar()
            |    } else b?.let {
            |        baz()
            |    }
            |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `should check braces in if statements - exception for run`() {
        lintMethod(
            """
            |fun foo() {
            |    if (a) {
            |        bar()
            |    } else b!!.run {
            |        baz()
            |    }
            |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `should check braces in if statements - exception for apply`() {
        lintMethod(
            """
            |fun foo() {
            |    if (a) {
            |        bar()
            |    } else b.apply {
            |        baz()
            |    }
            |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `should check braces in if statements, apply exists, but braces are needed`() {
        lintMethod(
            """
            |fun foo() {
            |    if (a) {
            |        bar()
            |    } else baz(b.apply { id = 5 })
            |}
            """.trimMargin(),
            LintError(4, 7, ruleId, "${NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnText()} ELSE", true)
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `should check braces in if statements, apply exists, but braces are needed 2`() {
        lintMethod(
            """
            |fun foo() {
            |    if (a) {
            |        bar()
            |    } else 
            |        c.baz(b.apply {id = 5})
            |}
            """.trimMargin(),
            LintError(4, 7, ruleId, "${NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnText()} ELSE", true)
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `should correctly detect single line if`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if (x > 0) {
                    |        bar()
                    |    } else if (x == 0) bar() else baz()
                    |}
                """.trimMargin(),
            LintError(4, 12, ruleId, "${NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnText()} IF", true),
            LintError(4, 30, ruleId, "${NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnText()} ELSE", true)
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `should check braces in if statements - empty body`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if (x > 0)
                    |    else if (x == 0)
                    |    else foo()
                    |}
                """.trimMargin(),
            LintError(2, 5, ruleId, "${NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnText()} IF", true),
            LintError(3, 10, ruleId, "${NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnText()} IF", true),
            LintError(4, 5, ruleId, "${NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnText()} ELSE", true)
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `should warn if single line branches in when are used with braces`() {
        lintMethod(
            """
                    |fun foo() {
                    |    when (x) {
                    |        OPTION_1 -> {
                    |            foo()
                    |            println()
                    |        }
                    |        OPTION_2 -> { bar() }
                    |        OPTION_3 -> {
                    |            baz()
                    |        }
                    |        OPTION_4 -> {
                    |            // description
                    |            bzz()
                    |        }
                    |    }
                    |}
                """.trimMargin(),
            LintError(7, 21, ruleId, "${NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnText()} WHEN", true),
            LintError(8, 21, ruleId, "${NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnText()} WHEN", true)
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `for loops should have braces - positive example`() {
        lintMethod(
            """
                    |fun foo() {
                    |    for (i in 1..100) {
                    |        println(i)
                    |    }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `for loops should have braces`() {
        lintMethod(
            """
                    |fun foo() {
                    |    for (i in 1..100)
                    |        println(i)
                    |}
                """.trimMargin(),
            LintError(2, 5, ruleId, "${NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnText()} FOR", true)
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `while loops should have braces - positive example`() {
        lintMethod(
            """
                    |fun foo() {
                    |    while (condition) {
                    |        println("")
                    |    }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `while loops should have braces`() {
        lintMethod(
            """
                    |fun foo() {
                    |    while (condition)
                    |        println("")
                    |}
                """.trimMargin(),
            LintError(2, 5, ruleId, "${NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnText()} WHILE", true)
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `do-while loops should have braces - positive example`() {
        lintMethod(
            """
                    |fun foo() {
                    |    do {
                    |        println("")
                    |    } while (condition)
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `do-while loops should have braces`() {
        lintMethod(
            """
                    |fun foo() {
                    |    do println(i) while (condition)
                    |}
                """.trimMargin(),
            LintError(2, 5, ruleId, "${NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnText()} DO_WHILE", true)
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_CONDITIONALS_AND_LOOPS)
    fun `do-while loops should have braces - empty body`() {
        lintMethod(
            """
                    |fun foo() {
                    |    do while (condition)
                    |}
                """.trimMargin(),
            LintError(2, 5, ruleId, "${NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnText()} DO_WHILE", true)
        )
    }
}
