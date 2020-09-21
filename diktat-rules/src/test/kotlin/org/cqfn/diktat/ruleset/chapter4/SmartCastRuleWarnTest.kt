package org.cqfn.diktat.ruleset.chapter4

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.SmartCastRule
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Test

class SmartCastRuleWarnTest : LintTestBase(::SmartCastRule) {


    private val ruleId = "$DIKTAT_RULE_SET_ID:smart-cast-rule"


    @Test
    fun `if with is smart cast good`() {
        lintMethod(
                """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       if (x is String) {
                    |           val a = x.length
                    |       }
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `if with is smart cast bad`() {
        lintMethod(
                """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       if (x is String) {
                    |           val a = (x as String).length
                    |       }
                    |   }
                    |}
                """.trimMargin(),
                LintError(5, 21, ruleId, "${Warnings.SMART_CAST_NEEDED.warnText()} x as String", true)
        )
    }

    @Test
    fun `if with another if with is smart cast good`() {
        lintMethod(
                """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       if (x is String) {
                    |           val a = x.length
                    |           if (x is Int) {
                    |               val a = x.value
                    |           }
                    |       }
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `if with another if with is smart cast bad`() {
        lintMethod(
                """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       if (x is String) {
                    |           val a = x.length
                    |           if (x is Int) {
                    |               val a = (x as Int).value
                    |           }
                    |       }
                    |   }
                    |}
                """.trimMargin(),
                LintError(7, 25, ruleId, "${Warnings.SMART_CAST_NEEDED.warnText()} x as Int", true)
        )
    }

    @Test
    fun `smart cast in else good`() {
        lintMethod(
                """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       if (x !is String) {
                    |           val a = (x as String).length
                    |       } else {
                    |           val b = (x as String).length
                    |       }
                    |   }
                    |}
                """.trimMargin(),
                LintError(7, 21, ruleId, "${Warnings.SMART_CAST_NEEDED.warnText()} x as String", true)
        )
    }

    @Test
    fun `smart cast in if without braces bad`() {
        lintMethod(
                """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       if (x is String)
                    |           print((x as String).length)
                    |   }
                    |}
                """.trimMargin(),
                LintError(5, 19, ruleId, "${Warnings.SMART_CAST_NEEDED.warnText()} x as String", true)
        )
    }

    @Test
    fun `smart cast in if without braces good`() {
        lintMethod(
                """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       if (x is String)
                    |           print(x.length)
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `smart cast in else without braces good`() {
        lintMethod(
                """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       if (x !is String) {
                    |           print("asd")
                    |       }
                    |       else
                    |           print((x as String).length)
                    |   }
                    |}
                """.trimMargin(),
                LintError(8, 19, ruleId, "${Warnings.SMART_CAST_NEEDED.warnText()} x as String", true)
        )
    }

    @Test
    fun `smart cast in when bad`() {
        lintMethod(
                """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       when (x) {
                    |           is Int -> print((x as Int).length)
                    |           is String -> print("String")
                    |       }
                    |   }
                    |}
                """.trimMargin(),
                LintError(5, 29, ruleId, "${Warnings.SMART_CAST_NEEDED.warnText()} x as Int", true)
        )
    }

    @Test
    fun `smart cast in when good`() {
        lintMethod(
                """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       when (x) {
                    |           is Int -> print(x.length)
                    |           is String -> print("String")
                    |       }
                    |   }
                    |}
                """.trimMargin()
        )
    }


    @Test
    fun `if with multiple is good`() {
        lintMethod(
                """
                    |class Test {
                    |   val x = ""
                    |   val y = 3
                    |   fun someFun() {
                    |       if (x is String || y is Int) {
                    |           val a = x.length
                    |           val b = y.value
                    |       }
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `if with multiple is bad`() {
        lintMethod(
                """
                    |class Test {
                    |   val x = ""
                    |   val y = 3
                    |   fun someFun() {
                    |       if (x is String || y is Int) {
                    |           val a = (x as String).length
                    |           val b = (y as Int).value
                    |       }
                    |   }
                    |}
                """.trimMargin(),
                LintError(6, 21, ruleId, "${Warnings.SMART_CAST_NEEDED.warnText()} x as String",true),
                LintError(7, 21, ruleId, "${Warnings.SMART_CAST_NEEDED.warnText()} y as Int", true)
        )
    }
}
