package org.cqfn.diktat.ruleset.chapter3

import generated.WarningNames.LONG_NUMERICAL_VALUES_SEPARATED
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.LongNumericalValuesSeparatedRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class LongNumericalValuesSeparatedFixTest : FixTestBase("test/paragraph3/long_numbers", LongNumericalValuesSeparatedRule()) {

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
