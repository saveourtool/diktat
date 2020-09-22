package org.cqfn.diktat.ruleset.chapter4

import com.pinterest.ktlint.core.LintError
import generated.WarningNames.GENERIC_VARIABLE_WRONG_DECLARATION
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.VariableGenericTypeDeclarationRule
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class VariableGenericTypeDeclarationRuleWarnTest : LintTestBase(::VariableGenericTypeDeclarationRule) {

    private val ruleId = "$DIKTAT_RULE_SET_ID:variable-generic-type"

    @Test
    @Tag(GENERIC_VARIABLE_WRONG_DECLARATION)
    fun `property with generic type good`() {
        lintMethod(
                """
                    |class SomeClass {
                    |   val myVariable: Map<Int, String> = emptyMap()
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(GENERIC_VARIABLE_WRONG_DECLARATION)
    fun `property with generic type bad`() {
        lintMethod(
                """
                    |class SomeClass {
                    |   val myVariable: Map<Int, String> = emptyMap<Int, String>()
                    |   val any = Array<Any>(3) { "" }
                    |}
                """.trimMargin(),
                LintError(2,4, ruleId,
                        "${Warnings.GENERIC_VARIABLE_WRONG_DECLARATION.warnText()} val myVariable: Map<Int, String> = emptyMap<Int, String>()", true),
                LintError(3,4, ruleId,
                        "${Warnings.GENERIC_VARIABLE_WRONG_DECLARATION.warnText()} val any = Array<Any>(3) { \"\" }", false)
        )
    }

    @Test
    @Tag(GENERIC_VARIABLE_WRONG_DECLARATION)
    fun `property in function good`() {
        lintMethod(
                """
                    |class SomeClass {
                    |   private fun someFunc(myVariable: Map<Int, String> = emptyMap()) {
                    |       
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(GENERIC_VARIABLE_WRONG_DECLARATION)
    fun `property in function bad`() {
        lintMethod(
                """
                    |class SomeClass {
                    |   private fun someFunc(myVariable: Map<Int, String> = emptyMap<Int, String>()) {
                    |       
                    |   }
                    |}
                """.trimMargin(),
                LintError(2,25, ruleId,
                        "${Warnings.GENERIC_VARIABLE_WRONG_DECLARATION.warnText()} myVariable: Map<Int, String> = emptyMap<Int, String>()", true)
        )
    }
}