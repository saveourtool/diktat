package com.saveourtool.diktat.ruleset.chapter5

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter5.AvoidNestedFunctionsRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames.AVOID_NESTED_FUNCTIONS
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class AvoidNestedFunctionsWarnTest : LintTestBase(::AvoidNestedFunctionsRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${AvoidNestedFunctionsRule.NAME_ID}"

    @Test
    @Tag(AVOID_NESTED_FUNCTIONS)
    fun `nested function`() {
        lintMethod(
            """
                    |fun foo() {
                    |   someFunc()
                    |   fun bar(a:String, b:Int) {
                    |       val param = 5
                    |       val repeatFun: String.(Int) -> String = { times -> this.repeat(times) }
                    |       param.value
                    |       repeatFun(45)
                    |       some(a)
                    |       some(b)
                    |   }
                    |
                    |}
            """.trimMargin(),
            DiktatError(3, 4, ruleId, "${Warnings.AVOID_NESTED_FUNCTIONS.warnText()} fun bar", false)
        )
    }

    @Test
    @Tag(AVOID_NESTED_FUNCTIONS)
    fun `anonymous function`() {
        val code = """
            package com.saveourtool.diktat.test

            fun foo() {
                val sum: (Int) -> Int = fun(x): Int = x + x
            }

        """.trimIndent()
        lintMethod(code)
    }

    @Test
    @Tag(AVOID_NESTED_FUNCTIONS)
    fun `several nested functions`() {
        lintMethod(
            """
                    |fun foo() {
                    |
                    |   fun bar() {
                    |       fun baz() {
                    |       }
                    |   }
                    |
                    |}
            """.trimMargin(),
            DiktatError(3, 4, ruleId, "${Warnings.AVOID_NESTED_FUNCTIONS.warnText()} fun bar", true),
            DiktatError(4, 8, ruleId, "${Warnings.AVOID_NESTED_FUNCTIONS.warnText()} fun baz", true)
        )
    }

    @Test
    @Tag(AVOID_NESTED_FUNCTIONS)
    fun `no nested functions`() {
        lintMethod(
            """
                    |class SomeClass {
                    |   fun someFunc() {}
                    |
                    |   fun anotherFunc() {}
                    |
                    |   fun moreFunction() {}
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(AVOID_NESTED_FUNCTIONS)
    fun `nested functions in anonymous class`() {
        lintMethod(
            """
                    |class SomeClass {
                    |fun some() {
                    |       listOf(
                    |              RuleSet("test", object : Rule("astnode-utils-test") {
                    |                   override fun visit(node: ASTNode,
                    |                   autoCorrect: Boolean,
                    |                   emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
                    |                   applyToNode(node, counter)
                    |                   }
                    |           }))
                    |   }
                    |}
            """.trimMargin()
        )
    }
}
