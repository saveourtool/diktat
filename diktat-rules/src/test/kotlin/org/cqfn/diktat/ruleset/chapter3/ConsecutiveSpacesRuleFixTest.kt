package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.ConsecutiveSpacesRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class ConsecutiveSpacesRuleFixTest : FixTestBase("test/paragraph3/spaces", ConsecutiveSpacesRule(),
        listOf(
                RulesConfig(Warnings.TOO_MANY_CONSECUTIVE_SPACES.name, true,
                        mapOf(
                                "max_spaces" to "1",
                                "saveInitialFormattingForEnums" to "true"
                        )
                )
        )
) {
    @Test
    @Tag("TOO_MANY_SPACES")
    fun `many spaces rule enum case`() {
        fixAndCompare("TooManySpacesEnumExpected.kt", "TooManySpacesEnumTest.kt")
    }

    @Test
    @Tag("TOO_MANY_SPACES")
    fun `many spaces rule`() {
        fixAndCompare("TooManySpacesExpected.kt", "TooManySpacesTest.kt")
    }
}
