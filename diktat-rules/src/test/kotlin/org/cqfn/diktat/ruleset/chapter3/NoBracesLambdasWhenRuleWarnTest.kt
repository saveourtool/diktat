package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.NoBracesLambdasWhenRule
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class NoBracesLambdasWhenRuleWarnTest {

    private val ruleId = "$DIKTAT_RULE_SET_ID:no-braces-lambdas-when"

    @Test
    @Tag(WarningNames.NO_BRACES_IN_LAMBDAS_AND_WHEN)
    fun `excess braces in lambda bad`() {
        lintMethod(NoBracesLambdasWhenRule(),
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
                LintError(1, 35, ruleId, "${Warnings.NO_BRACES_IN_LAMBDAS_AND_WHEN.warnText()} text", true)
        )
    }

    @Test
    @Tag(WarningNames.NO_BRACES_IN_LAMBDAS_AND_WHEN)
    fun `excess braces in lambda good`() {
        lintMethod(NoBracesLambdasWhenRule(),
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
    @Tag(WarningNames.NO_BRACES_IN_LAMBDAS_AND_WHEN)
    fun `excess braces in lambda good 2`() {
        lintMethod(NoBracesLambdasWhenRule(),
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
    @Tag(WarningNames.NO_BRACES_IN_LAMBDAS_AND_WHEN)
    fun `excess braces in lambda good 3`() {
        lintMethod(NoBracesLambdasWhenRule(),
                """
                    |val lambda1: (String) -> Unit = { name: String -> println("Hello, World!") }
                    |val lambda2: (String) -> Unit = { name -> println("Hello, World!") }
                    |val lambda3: (String) -> Unit = println("Hello, World!")
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
    @Tag(WarningNames.NO_BRACES_IN_LAMBDAS_AND_WHEN)
    fun `excess braces in lambda good 4`() {
        lintMethod(NoBracesLambdasWhenRule(),
                """
                    |private fun isValidYear(year: String?): Boolean { 
                    |val yearValidator: (String?) -> Boolean = { !it.isNullOrEmpty() && it.toInt() in 1877..2019 }
                    |return yearValidator(year)
                    |}
                """.trimMargin()
        )
    }
}