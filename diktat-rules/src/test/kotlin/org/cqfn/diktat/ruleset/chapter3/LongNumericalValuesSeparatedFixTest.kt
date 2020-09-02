package org.cqfn.diktat.ruleset.chapter3

import generated.WarningNames.LONG_NUMERICAL_VALUES_SEPARATED
import org.cqfn.diktat.ruleset.rules.LongNumericalValuesSeparatedRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class LongNumericalValuesSeparatedFixTest : FixTestBase("test/paragraph3/long_numbers", LongNumericalValuesSeparatedRule()) {

    @Test
    @Tag(LONG_NUMERICAL_VALUES_SEPARATED)
    fun `should add delimeters`() {
        fixAndCompare("LongNumericalValuesExpected.kt", "LongNumericalValuesTest.kt")
    }
}
