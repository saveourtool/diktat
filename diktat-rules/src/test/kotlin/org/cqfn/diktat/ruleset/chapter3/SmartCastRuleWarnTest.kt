package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.SmartCastRule
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Test

class SmartCastRuleWarnTest : LintTestBase(::SmartCastRule) {


    private val ruleId = "$DIKTAT_RULE_SET_ID:smart-cast-rule"


    @Test
    fun `should omit smart cast`() {
        lintMethod(
                """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       if (x is String && x != "a") {
                    |           val a = (x as String).length
                    |           if (a == 5)
                    |               print(a)
                    |       } else if (x == "as") {
                    |       
                    |       } else if (x == "") {}
                    |       
                    |       when(x) {
                    |           is String -> {print(x)}
                    |       }
                    |       
                    |       while (x is String) {}
                    |   }
                    |}
                """.trimMargin()
        )
    }
}