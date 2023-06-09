package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter3.LongNumericalValuesSeparatedRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames.LONG_NUMERICAL_VALUES_SEPARATED
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class LongNumericalValuesSeparatedWarnTest : LintTestBase(::LongNumericalValuesSeparatedRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${LongNumericalValuesSeparatedRule.NAME_ID}"
    private val rulesConfig: List<RulesConfig> = listOf(
        RulesConfig(Warnings.LONG_NUMERICAL_VALUES_SEPARATED.name, true,
            mapOf("maxNumberLength" to "2"))
    )

    @Test
    @Tag(LONG_NUMERICAL_VALUES_SEPARATED)
    fun `check properties test bad`() {
        lintMethod(
            """
                    |fun foo() {
                    |   val oneMillion = 100000000000
                    |   val creditCardNumber = 1234567890123456L
                    |   val socialSecurityNumber = 999999999L
                    |   val hexBytes = 0xFFECDE5E
                    |   val hexBytes2 = 0xF
                    |   val bytes = 0b110100110_01101001_10010100_10010010
                    |   val flo = 192.312341341344355345
                    |   val flo2 = 192.31234134134435_5345
                    |   val hundred = 100
                    |}
            """.trimMargin(),
            DiktatError(2, 21, ruleId, "${Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnText()} 100000000000", true),
            DiktatError(3, 27, ruleId, "${Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnText()} 1234567890123456L", true),
            DiktatError(4, 31, ruleId, "${Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnText()} 999999999L", true),
            DiktatError(5, 19, ruleId, "${Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnText()} 0xFFECDE5E", true),
            DiktatError(7, 16, ruleId, "${Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnText()} this block is too long 110100110", false),
            DiktatError(7, 16, ruleId, "${Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnText()} this block is too long 01101001", false),
            DiktatError(7, 16, ruleId, "${Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnText()} this block is too long 10010100", false),
            DiktatError(7, 16, ruleId, "${Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnText()} this block is too long 10010010", false),
            DiktatError(8, 14, ruleId, "${Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnText()} 192.312341341344355345", true),
            DiktatError(9, 15, ruleId, "${Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnText()} this block is too long 31234134134435", false),
            DiktatError(9, 15, ruleId, "${Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnText()} this block is too long 5345", false)

        )
    }

    @Test
    @Tag(LONG_NUMERICAL_VALUES_SEPARATED)
    fun `check properties test good`() {
        lintMethod(
            """
                    |fun foo() {
                    |   val oneMillion = 1_000_000_000_000
                    |   val creditCardNumber = 1_234_567_890_123_456L
                    |   val socialSecurityNumber = 999_999_999L
                    |   val hexBytes = 0xFF_EC_DE_5E
                    |   val bytes = 0b11_010_010_011_010_011_001_010_010_010_010
                    |   val flo = 192.312_341_341_345
                    |   val ten = 10
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(LONG_NUMERICAL_VALUES_SEPARATED)
    fun `check properties test bad 2`() {
        lintMethod(
            """
                    |fun foo() {
                    |   val oneMillion = 100
                    |   val creditCardNumber = 1234566L
                    |   val socialSecurityNumber = 999L
                    |   val hexBytes = 0xFFE
                    |   val bytes = 0b110100
                    |   val flo = 192.312
                    |}
            """.trimMargin(),
            DiktatError(2, 21, ruleId, "${Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnText()} 100", true),
            DiktatError(3, 27, ruleId, "${Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnText()} 1234566L", true),
            DiktatError(4, 31, ruleId, "${Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnText()} 999L", true),
            DiktatError(5, 19, ruleId, "${Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnText()} 0xFFE", true),
            DiktatError(6, 16, ruleId, "${Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnText()} 0b110100", true),
            DiktatError(7, 14, ruleId, "${Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnText()} 192.312", true),
            rulesConfigList = rulesConfig
        )
    }

    @Test
    @Tag(LONG_NUMERICAL_VALUES_SEPARATED)
    fun `check func params test good`() {
        lintMethod(
            """
                    |fun foo(val one = 100_000_000) {
                    |
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(LONG_NUMERICAL_VALUES_SEPARATED)
    fun `check func params test bad`() {
        lintMethod(
            """
                    |fun foo(val one = 100000000) {
                    |
                    |}
            """.trimMargin(),
            DiktatError(1, 19, ruleId, "${Warnings.LONG_NUMERICAL_VALUES_SEPARATED.warnText()} 100000000", true)
        )
    }
}
