package org.cqfn.diktat.ruleset.chapter4

import org.cqfn.diktat.ruleset.constants.Warnings.FLOAT_IN_ACCURATE_CALCULATIONS
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter4.calculations.AccurateCalculationsRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class AccurateCalculationsWarnTest : LintTestBase(::AccurateCalculationsRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${AccurateCalculationsRule.NAME_ID}"

    private fun warnText(ref: String, expr: String) = "${FLOAT_IN_ACCURATE_CALCULATIONS.warnText()} float value of <$ref> used in arithmetic expression in $expr"

    @Test
    @Tag(WarningNames.FLOAT_IN_ACCURATE_CALCULATIONS)
    fun `should detect comparison (equals) with float literal`() {
        lintMethod(
            """
                    |class Example {
                    |    fun foo() {
                    |        x == 1.0
                    |        1.0 == x
                    |        x.equals(1.0)
                    |        1.0.equals(x)
                    |    }
                    |}
            """.trimMargin(),
            LintError(3, 9, ruleId, warnText("1.0", "x == 1.0"), false),
            LintError(4, 9, ruleId, warnText("1.0", "1.0 == x"), false),
            LintError(5, 9, ruleId, warnText("1.0", "x.equals(1.0)"), false),
            LintError(6, 9, ruleId, warnText("1.0", "1.0.equals(x)"), false)
        )
    }

    @Test
    @Tag(WarningNames.FLOAT_IN_ACCURATE_CALCULATIONS)
    fun `should detect comparison with float literal`() {
        lintMethod(
            """
                    |class Example {
                    |    fun foo() {
                    |        x > 1.0
                    |        1.0 > x
                    |        x.compareTo(1.0)
                    |        1.0.compareTo(x)
                    |    }
                    |}
            """.trimMargin(),
            LintError(3, 9, ruleId, warnText("1.0", "x > 1.0"), false),
            LintError(4, 9, ruleId, warnText("1.0", "1.0 > x"), false),
            LintError(5, 9, ruleId, warnText("1.0", "x.compareTo(1.0)"), false),
            LintError(6, 9, ruleId, warnText("1.0", "1.0.compareTo(x)"), false)
        )
    }

    @Test
    @Tag(WarningNames.FLOAT_IN_ACCURATE_CALCULATIONS)
    fun `should detect comparisons with local floating-point variables - 1`() {
        lintMethod(
            """
                    |class Example {
                    |    fun foo() {
                    |        val x = 1.0
                    |        x == 1
                    |    }
                    |}
            """.trimMargin(),
            LintError(4, 9, ruleId, warnText("x", "x == 1"), false)
        )
    }

    @Test
    @Tag(WarningNames.FLOAT_IN_ACCURATE_CALCULATIONS)
    fun `should detect comparisons with local floating-point variables - 2`() {
        lintMethod(
            """
                    |class Example {
                    |    fun foo() {
                    |        val x = 1L
                    |        list.forEach {
                    |            val x = 1.0
                    |            x == 1
                    |        }
                    |    }
                    |}
            """.trimMargin(),
            LintError(6, 13, ruleId, warnText("x", "x == 1"), false)
        )
    }

    @Test
    @Tag(WarningNames.FLOAT_IN_ACCURATE_CALCULATIONS)
    fun `should detect comparisons with local floating-point variables - 3`() {
        lintMethod(
            """
                    |class Example {
                    |    fun foo() {
                    |        val x = 1L
                    |        list.forEach {
                    |            obj.let {
                    |                x == 1
                    |            }
                    |            val x = 1.0
                    |        }
                    |    }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.FLOAT_IN_ACCURATE_CALCULATIONS)
    fun `should detect different operations with operators`() {
        lintMethod(
            """
                    |class Example {
                    |    fun foo() {
                    |        val x = 1.0
                    |        x == 1
                    |        x + 2
                    |        // x++
                    |        x += 2
                    |        x - 2
                    |        // x--
                    |        x -= 2
                    |        x * 2
                    |        x *= 2
                    |        x / 2
                    |        x /= 2
                    |        x % 2
                    |        x %= 2
                    |    }
                    |}
            """.trimMargin(),
            LintError(4, 9, ruleId, warnText("x", "x == 1"), false),
            LintError(5, 9, ruleId, warnText("x", "x + 2"), false),
            // LintError(6, 9, ruleId, warnText("x", "x++"), false),
            LintError(7, 9, ruleId, warnText("x", "x += 2"), false),
            LintError(8, 9, ruleId, warnText("x", "x - 2"), false),
            // LintError(9, 9, ruleId, warnText("x", "x--"), false),
            LintError(10, 9, ruleId, warnText("x", "x -= 2"), false),
            LintError(11, 9, ruleId, warnText("x", "x * 2"), false),
            LintError(12, 9, ruleId, warnText("x", "x *= 2"), false),
            LintError(13, 9, ruleId, warnText("x", "x / 2"), false),
            LintError(14, 9, ruleId, warnText("x", "x /= 2"), false),
            LintError(15, 9, ruleId, warnText("x", "x % 2"), false),
            LintError(16, 9, ruleId, warnText("x", "x %= 2"), false)
        )
    }

    @Test
    @Tag(WarningNames.FLOAT_IN_ACCURATE_CALCULATIONS)
    fun `should allow arithmetic operations inside abs in comparison`() {
        lintMethod(
            """
                    |import kotlin.math.abs
                    |
                    |fun foo() {
                    |    if (abs(1.0 - 0.999) < 1e-6) {
                    |        println("Comparison with tolerance")
                    |    }
                    |
                    |    1e-6 > abs(1.0 - 0.999)
                    |    abs(1.0 - 0.999).compareTo(1e-6) < 0
                    |    1e-6.compareTo(abs(1.0 - 0.999)) < 0
                    |    abs(1.0 - 0.999) == 1e-6
                    |
                    |    abs(1.0 - 0.999) < eps
                    |    eps > abs(1.0 - 0.999)
                    |
                    |    val x = 1.0
                    |    val y = 0.999
                    |    abs(x - y) < eps
                    |    eps > abs(x - y)
                    |    abs(1.0 - 0.999) == eps
                    |}
            """.trimMargin(),
            LintError(11, 5, ruleId, warnText("1e-6", "abs(1.0 - 0.999) == 1e-6"), false),
            LintError(11, 9, ruleId, warnText("1.0", "1.0 - 0.999"), false),
            LintError(20, 9, ruleId, warnText("1.0", "1.0 - 0.999"), false)
        )
    }
}
