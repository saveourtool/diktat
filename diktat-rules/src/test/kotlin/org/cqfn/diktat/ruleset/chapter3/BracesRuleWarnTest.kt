package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.constants.Warnings.NO_BRACES_IN_CONDITIONALS_AND_LOOPS
import org.cqfn.diktat.ruleset.rules.BracesInConditionalsAndLoopsRule
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Test

class BracesRuleWarnTest {
    private val ruleId = "$DIKTAT_RULE_SET_ID:braces-rule"

    @Test
    fun `should check braces in if statement without else branch - positive example`() {
        lintMethod(BracesInConditionalsAndLoopsRule(),
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
    fun `should check braces in if statement without else branch`() {
        lintMethod(BracesInConditionalsAndLoopsRule(),
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
    fun `should check braces in if statements - positive example`() {
        lintMethod(BracesInConditionalsAndLoopsRule(),
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
    fun `should check braces in if statements`() {
        lintMethod(BracesInConditionalsAndLoopsRule(),
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
    fun `should check braces in if statements - exception for single line if`() {
        lintMethod(BracesInConditionalsAndLoopsRule(),
                """
                    |fun foo() {
                    |    if (x > 0) bar() else baz()
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `should correctly detect single line if`() {
        lintMethod(BracesInConditionalsAndLoopsRule(),
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
    fun `should check braces in if statements - empty body`() {
        lintMethod(BracesInConditionalsAndLoopsRule(),
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
    fun `should warn if single line branches in when are used with braces`() {
        lintMethod(BracesInConditionalsAndLoopsRule(),
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
                    |    }
                    |}
                """.trimMargin(),
                LintError(7, 21, ruleId, "${NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnText()} WHEN", true),
                LintError(8, 21, ruleId, "${NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnText()} WHEN", true)
        )
    }

    @Test
    fun `for loops should have braces - positive example`() {
        lintMethod(BracesInConditionalsAndLoopsRule(),
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
    fun `for loops should have braces`() {
        lintMethod(BracesInConditionalsAndLoopsRule(),
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
    fun `while loops should have braces - positive example`() {
        lintMethod(BracesInConditionalsAndLoopsRule(),
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
    fun `while loops should have braces`() {
        lintMethod(BracesInConditionalsAndLoopsRule(),
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
    fun `do-while loops should have braces - positive example`() {
        lintMethod(BracesInConditionalsAndLoopsRule(),
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
    fun `do-while loops should have braces`() {
        lintMethod(BracesInConditionalsAndLoopsRule(),
                """
                    |fun foo() {
                    |    do println(i) while (condition)
                    |}
                """.trimMargin(),
                LintError(2, 5, ruleId, "${NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnText()} DO_WHILE", true)
        )
    }

    @Test
    fun `do-while loops should have braces - empty body`() {
        lintMethod(BracesInConditionalsAndLoopsRule(),
                """
                    |fun foo() {
                    |    do while (condition)
                    |}
                """.trimMargin(),
                LintError(2, 5, ruleId, "${NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnText()} DO_WHILE", true)
        )
    }
}
