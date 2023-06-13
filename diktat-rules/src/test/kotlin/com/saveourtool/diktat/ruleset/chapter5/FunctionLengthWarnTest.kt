package com.saveourtool.diktat.ruleset.chapter5

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.TOO_LONG_FUNCTION
import com.saveourtool.diktat.ruleset.rules.chapter5.FunctionLength
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class FunctionLengthWarnTest : LintTestBase(::FunctionLength) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${FunctionLength.NAME_ID}"
    private val rulesConfigList: List<RulesConfig> = listOf(
        RulesConfig(TOO_LONG_FUNCTION.name, true,
            mapOf("maxFunctionLength" to "5"))
    )
    private val shortRulesConfigList: List<RulesConfig> = listOf(
        RulesConfig(TOO_LONG_FUNCTION.name, true,
            mapOf("maxFunctionLength" to "2"))
    )
    private val shortRulesWithoutHeaderConfigList: List<RulesConfig> = listOf(
        RulesConfig(TOO_LONG_FUNCTION.name, true,
            mapOf("maxFunctionLength" to "3", "isIncludeHeader" to "false"))
    )

    @Test
    @Tag(WarningNames.TOO_LONG_FUNCTION)
    fun `check with all comment`() {
        lintMethod(
            """
                    |fun foo() {
                    |
                    |   //dkfgvdf
                    |
                    |   /*
                    |    * jkgh
                    |    */
                    |
                    |    /**
                    |     * dfkjvbhdfkjb
                    |     */
                    |
                    |   val x = 10
                    |   val y = 100
                    |   println(x)
                    |   val z = x + y
                    |   println(x)
                    |
                    |}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${TOO_LONG_FUNCTION.warnText()} max length is 5, but you have 7", false),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.TOO_LONG_FUNCTION)
    fun `less than max`() {
        lintMethod(
            """
                    |fun foo() {
                    |
                    |
                    |
                    |
                    |
                    |
                    |   val x = 10
                    |   val y = 100
                    |   println(x)
                    |
                    |}
            """.trimMargin(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.TOO_LONG_FUNCTION)
    fun `one line function`() {
        lintMethod(
            """
                    |fun foo(list: List<ASTNode>) = list.forEach {
                    |        if (it.element == "dfscv")
                    |           println()
                    |}
                    |
                    |fun goo() =
                    |   10
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${TOO_LONG_FUNCTION.warnText()} max length is 2, but you have 4", false),
            rulesConfigList = shortRulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.TOO_LONG_FUNCTION)
    fun `fun in class`() {
        lintMethod(
            """
                    |class A() {
                    |   val x = 10
                    |   val y  = 11
                    |
                    |   fun foo() {
                    |       if(true) {
                    |           while(true) {
                    |               println(x)
                    |               println(y)
                    |           }
                    |       }
                    |   }
                    |
                    |}
            """.trimMargin(),
            DiktatError(5, 4, ruleId, "${TOO_LONG_FUNCTION.warnText()} max length is 2, but you have 8", false),
            rulesConfigList = shortRulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.TOO_LONG_FUNCTION)
    fun `check suppress`() {
        lintMethod(
            """
                    |@Suppress("TOO_LONG_FUNCTION")
                    |class A() {
                    |   val x = 10
                    |   val y  = 11
                    |
                    |
                    |   fun foo() {
                    |       if(true) {
                    |           while(true) {
                    |               println(x)
                    |               println(y)
                    |           }
                    |       }
                    |   }
                    |
                    |}
            """.trimMargin(),
            rulesConfigList = shortRulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.TOO_LONG_FUNCTION)
    fun `only empty lines`() {
        lintMethod(
            """
                    |fun foo(list: List<ASTNode>) {
                    |
                    |
                    |
                    |
                    |}
            """.trimMargin(),
            rulesConfigList = shortRulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.TOO_LONG_FUNCTION)
    fun `fun longer but without body`() {
        lintMethod(
            """
                    |class A() {
                    |   val x = 10
                    |   val y  = 11
                    |
                    |   fun foo()
                    |   {
                    |       println(123)
                    |   }
                    |
                    |}
                    |
                    |abstract class B {
                    |   abstract fun foo()
                    |}
            """.trimMargin(),
            rulesConfigList = shortRulesWithoutHeaderConfigList
        )
    }
}
