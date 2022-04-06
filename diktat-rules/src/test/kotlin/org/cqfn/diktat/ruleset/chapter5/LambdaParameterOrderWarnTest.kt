package org.cqfn.diktat.ruleset.chapter5

import org.cqfn.diktat.ruleset.constants.Warnings.LAMBDA_IS_NOT_LAST_PARAMETER
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter5.LambdaParameterOrder
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class LambdaParameterOrderWarnTest : LintTestBase(::LambdaParameterOrder) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${LambdaParameterOrder.nameId}"

    @Test
    @Tag(WarningNames.LAMBDA_IS_NOT_LAST_PARAMETER)
    fun `check simple example`() {
        lintMethod(
            """
                    |fun foo(a: Int, myLambda: () -> Unit, b: Int) { }
                    |
                    |fun foo(a: Int, b: Int, myLambda: () -> Unit) = true
                    |
                    |@Suppress("LAMBDA_IS_NOT_LAST_PARAMETER")
                    |fun foo(a: Int, myLambda: () -> Unit, b: Int) { }
                    |
                    |fun foo(a: Int, myLambdab: () -> Unit, myLambda: () -> Unit)
                    |
                    |fun foo(a: Int? = null, myLambdab: () -> Unit, myLambda: () -> Unit)
                    |
                    |fun foo(lambda1: () -> Unit, lambda2: (() -> Unit)?) {}
                """.trimMargin(),
            LintError(1, 17, ruleId, "${LAMBDA_IS_NOT_LAST_PARAMETER.warnText()} foo", false)
        )
    }
}
