package org.cqfn.diktat.ruleset.chapter5

import com.pinterest.ktlint.core.LintError
import generated.WarningNames.AVOID_NESTED_FUNCTIONS
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.AvoidNestedFunctionsRule
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class AvoidNestedFunctionsWarnTest : LintTestBase(::AvoidNestedFunctionsRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:avoid-nested-functions"

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
                LintError(3,4, ruleId, "${Warnings.AVOID_NESTED_FUNCTIONS.warnText()} fun bar", false)
        )
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
                LintError(3,4, ruleId, "${Warnings.AVOID_NESTED_FUNCTIONS.warnText()} fun bar", true),
                LintError(4,8, ruleId, "${Warnings.AVOID_NESTED_FUNCTIONS.warnText()} fun baz", true)
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
}