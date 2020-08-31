package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.NoBracesLambdasRule
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class NoBracesLambdasRuleWarnTest {

    private val ruleId = "$DIKTAT_RULE_SET_ID:no-braces-lambdas"

    @Test
    @Tag(WarningNames.NO_BRACES_IN_LAMBDAS)
    fun `excess braces in lambda bad`() {
        lintMethod(NoBracesLambdasRule(),
                """
                    |val a = { b: String, c: String -> {
                    |       null
                    |    }
                    |}
                    |fun foo() {
                    |   some.map {x-> 
                    |       x + y
                    |       print(a)
                    |   }
                    |}
                """.trimMargin(),
                LintError(1, 35, ruleId, "${Warnings.NO_BRACES_IN_LAMBDAS.warnText()} text", true)
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_LAMBDAS)
    fun `excess braces in lambda good`() {
        lintMethod(NoBracesLambdasRule(),
                """
                    |val a = { b: String, c: String -> null
                    |}
                    |fun foo() {
                    |   some.map {x-> x}
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_LAMBDAS)
    fun `excess braces in lambda good 2`() {
        lintMethod(NoBracesLambdasRule(),
                """
                    |val a = { b: String, c: String -> 
                    |       null
                    |       print(x)
                    |    
                    |}
                    |fun foo() {
                    |   some.map {x -> x}
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_LAMBDAS)
    fun `excess braces in lambda good 3`() {
        lintMethod(NoBracesLambdasRule(),
                """
                    |val lambda1: (String) -> Unit = { name: String -> println("Hello, World!") }
                    |val lambda2: (String) -> Unit = { name -> println("Hello, World!") }
                    |val lambda4: (String) -> Unit = { println("Hello, World!") }
                    |val lambda5: (Int, Int) -> Int = {  x, y -> 
                    |print(x)
                    |print(y)
                    |x + y
                    |}
                    |val lambda6 = {x: Int, y: Int -> x + y }
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_LAMBDAS)
    fun `excess braces in lambda good 4`() {
        lintMethod(NoBracesLambdasRule(),
                """
                    |private fun isValidYear(year: String?): Boolean { 
                    |val yearValidator: (String?) -> Boolean = { !it.isNullOrEmpty() && it.toInt() in 1877..2019 }
                    |return yearValidator(year)
                    |}
                """.trimMargin()
        )
    }
}
