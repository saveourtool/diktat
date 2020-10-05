package org.cqfn.diktat.ruleset.chapter5

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_FUNCTION_ARGUMENTS_ORDER
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.LambdaParameterOrder
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class LambdaParameterOrderWarnTest : LintTestBase(::LambdaParameterOrder) {

    private val ruleId = "$DIKTAT_RULE_SET_ID:lambda-parameter-order"

    @Test
    @Tag(WarningNames.WRONG_FUNCTION_ARGUMENTS_ORDER)
    fun `check simple example`() {
        lintMethod(
                """
                    |fun foo(a: Int, myLambda: () -> Unit, b: Int) { }
                    |
                    |fun foo(a: Int, b: Int, myLambda: () -> Unit) = true
                    |
                    |@Suppress("WRONG_FUNCTION_ARGUMENTS_ORDER")
                    |fun foo(a: Int, myLambda: () -> Unit, b: Int) { }
                    |
                    |fun foo(a: Int, myLambdab: () -> Unit, myLambda: () -> Unit)
                    |
                    |fun foo(a: Int? = null, myLambdab: () -> Unit, myLambda: () -> Unit)
                """.trimMargin(),
                LintError(1,17, ruleId, "${WRONG_FUNCTION_ARGUMENTS_ORDER.warnText()} myLambda: () -> Unit", false),
                LintError(1,39, ruleId, "${WRONG_FUNCTION_ARGUMENTS_ORDER.warnText()} b: Int", false)
        )
    }
}
