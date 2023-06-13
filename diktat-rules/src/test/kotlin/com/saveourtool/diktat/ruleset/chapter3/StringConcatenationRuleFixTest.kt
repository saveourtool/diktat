package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter3.StringConcatenationRule
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class StringConcatenationRuleFixTest : FixTestBase(
    "test/paragraph3/string_concatenation",
    ::StringConcatenationRule,
    listOf(
        RulesConfig(Warnings.STRING_CONCATENATION.name, true, emptyMap())
    )
) {
    @Test
    @Tag(WarningNames.STRING_CONCATENATION)
    fun `fixing string concatenation`() {
        fixAndCompare("StringConcatenationExpected.kt", "StringConcatenationTest.kt")
    }
}
