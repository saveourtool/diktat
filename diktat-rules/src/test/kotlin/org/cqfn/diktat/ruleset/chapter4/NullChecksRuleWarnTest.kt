package org.cqfn.diktat.ruleset.chapter4

import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.chapter4.NullChecksRule
import org.cqfn.diktat.util.LintTestBase

import org.cqfn.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class NullChecksRuleWarnTest : LintTestBase(::NullChecksRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${NullChecksRule.NAME_ID}"

    @Test
    @Tag(WarningNames.AVOID_NULL_CHECKS)
    fun `equals to null`() {
        lintMethod(
            """
                | fun foo() {
                |     var myVar: Int? = null
                |     if (myVar == null) {
                |         println("null")
                |         return
                |     }
                | }
            """.trimMargin(),
            DiktatError(3, 10, ruleId, "${Warnings.AVOID_NULL_CHECKS.warnText()} use '.let/.also/?:/e.t.c' instead of myVar == null", true),
        )
    }

    @Test
    @Tag(WarningNames.AVOID_NULL_CHECKS)
    fun `equals to null in a chain of binary expressions`() {
        lintMethod(
            """
                | fun foo() {
                |     var myVar: Int? = null
                |     if ((myVar == null) && (true) || isValid) {
                |         println("null")
                |         return
                |     }
                |     myVar ?: kotlin.run {
                |       println("null")
                |     }
                | }
            """.trimMargin(),
            DiktatError(3, 11, ruleId, Warnings.AVOID_NULL_CHECKS.warnText() +
                    " use '.let/.also/?:/e.t.c' instead of myVar == null", true),
        )
    }

    @Test
    @Tag(WarningNames.AVOID_NULL_CHECKS)
    fun `not equals to null`() {
        lintMethod(
            """
                | fun foo() {
                |     if (myVar != null) {
                |         println("not null")
                |         return
                |     }
                | }
            """.trimMargin(),
            DiktatError(2, 10, ruleId, Warnings.AVOID_NULL_CHECKS.warnText() +
                    " use '.let/.also/?:/e.t.c' instead of myVar != null", true),
        )
    }

    @Test
    @Tag(WarningNames.AVOID_NULL_CHECKS)
    fun `if-else null comparison with return value`() {
        lintMethod(
            """
                | fun foo() {
                |     val anotherVal = if (myVar != null) {
                |                          println("not null")
                |                           1
                |                      } else {
                |                           2
                |                      }
                | }
            """.trimMargin(),
            DiktatError(2, 27, ruleId, Warnings.AVOID_NULL_CHECKS.warnText() +
                    " use '.let/.also/?:/e.t.c' instead of myVar != null", true),
        )
    }

    @Test
    @Tag(WarningNames.AVOID_NULL_CHECKS)
    fun `if-else null comparison with no return value`() {
        lintMethod(
            """
                | fun foo() {
                |     if (myVar !== null) {
                |            println("not null")
                |     } else {
                |            println("null")
                |     }
                | }
            """.trimMargin(),
            DiktatError(2, 10, ruleId, Warnings.AVOID_NULL_CHECKS.warnText() +
                    " use '.let/.also/?:/e.t.c' instead of myVar !== null", true),
        )
    }

    @Test
    @Tag(WarningNames.AVOID_NULL_CHECKS)
    fun `equals to null, but not in if`() {
        lintMethod(
            """
                | fun foo0() {
                |     if (true) {
                |         fun foo() {
                |             var myVar: Int? = null
                |             val myVal = myVar == null
                |             foo1(myVar == null)
                |             println("null")
                |         }
                |      }
                | }
            """.trimMargin(),
        )
    }

    @Test
    @Tag(WarningNames.AVOID_NULL_CHECKS)
    fun `equals to null, but in complex else-if statement`() {
        lintMethod(
            """
                | fun foo0() {
                |     if (myVar != null) {
                |        println("not null")
                |      } else if (true) {
                |        println()
                |      }
                | }
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.AVOID_NULL_CHECKS)
    fun `equals to null, but in complex else-if statement with dummy comment`() {
        lintMethod(
            """
                | fun foo0() {
                |     if (myVar != null) {
                |        println("not null")
                |      } else /* test comment */ if (true) {
                |        println()
                |      }
                | }
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.AVOID_NULL_CHECKS)
    fun `equals to null, but the expression is not a else-if`() {
        lintMethod(
            """
                | fun foo0() {
                |     if (myVar != null) {
                |        println("not null")
                |      } else {
                |           if (true) {
                |                println()
                |           }
                |      }
                | }
            """.trimMargin(),
            DiktatError(2, 10, ruleId, "${Warnings.AVOID_NULL_CHECKS.warnText()} use '.let/.also/?:/e.t.c'" +
                    " instead of myVar != null", true),
        )
    }

    @Test
    @Tag(WarningNames.AVOID_NULL_CHECKS)
    fun `require statements - adding `() {
        lintMethod(
            """
                | fun foo0(myVar: String?) {
                |     require(myVar != null)
                | }
            """.trimMargin(),
            DiktatError(2, 14, ruleId, Warnings.AVOID_NULL_CHECKS.warnText() +
                    " use 'requireNotNull' instead of require(myVar != null)", true),
        )
    }

    @Test
    @Tag(WarningNames.AVOID_NULL_CHECKS)
    fun `null check in lambda which is in if-statement is ok`() {
        lintMethod(
            """
                |fun foo() {
                |    if (leftSide?.any { it == null } == true) {
                |        return
                |    }
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.AVOID_NULL_CHECKS)
    fun `null check in lambda which is in require is ok`() {
        lintMethod(
            """
                |fun foo() {
                |    require(leftSide?.any { it == null })
                |}
            """.trimMargin()
        )
    }
}
