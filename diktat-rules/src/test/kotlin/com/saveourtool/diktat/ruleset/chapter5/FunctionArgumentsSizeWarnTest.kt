package com.saveourtool.diktat.ruleset.chapter5

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.TOO_MANY_PARAMETERS
import com.saveourtool.diktat.ruleset.rules.chapter5.FunctionArgumentsSize
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class FunctionArgumentsSizeWarnTest : LintTestBase(::FunctionArgumentsSize) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${FunctionArgumentsSize.NAME_ID}"
    private val rulesConfigList: List<RulesConfig> = listOf(
        RulesConfig(TOO_MANY_PARAMETERS.name, true,
            mapOf("maxParameterListSize" to "1"))
    )

    @Test
    @Tag(WarningNames.TOO_MANY_PARAMETERS)
    fun `check simple function`() {
        lintMethod(
            """
                    |fun foo(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int) {}
                    |fun foo(a: Int, b: Int, c: Int, d: Int, e: Int, myLambda: () -> Unit) {}
                    |fun foo(a: Int, b: Int, c: Int, d: Int, myLambda: () -> Unit) {}
                    |fun foo(a: Int, b: Int, c: Int, d: Int, e: Int, myLambda: () -> Unit) = 10
                    |abstract fun foo(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int)
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${TOO_MANY_PARAMETERS.warnText()} foo has 6, but allowed 5", false),
            DiktatError(2, 1, ruleId, "${TOO_MANY_PARAMETERS.warnText()} foo has 6, but allowed 5", false),
            DiktatError(4, 1, ruleId, "${TOO_MANY_PARAMETERS.warnText()} foo has 6, but allowed 5", false),
            DiktatError(5, 1, ruleId, "${TOO_MANY_PARAMETERS.warnText()} foo has 6, but allowed 5", false)
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_PARAMETERS)
    fun `check function with config`() {
        lintMethod(
            """
                    |fun foo(a: Int, b: Int) {}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${TOO_MANY_PARAMETERS.warnText()} foo has 2, but allowed 1", false),
            rulesConfigList = rulesConfigList
        )
    }
}
