package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter3.LongNumericalValuesSeparatedRule
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames.LONG_NUMERICAL_VALUES_SEPARATED
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class LongNumericalValuesSeparatedFixTest : FixTestBase("test/paragraph3/long_numbers", ::LongNumericalValuesSeparatedRule) {
    private val rulesConfig: List<RulesConfig> = listOf(
        RulesConfig(Warnings.LONG_NUMERICAL_VALUES_SEPARATED.name, true,
            mapOf("maxNumberLength" to "5", "maxBlockLength" to "3"))
    )

    @Test
    @Tag(LONG_NUMERICAL_VALUES_SEPARATED)
    fun `should add delimiters`() {
        fixAndCompare("LongNumericalValuesExpected.kt", "LongNumericalValuesTest.kt", rulesConfig)
    }
}
