package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.NoSpacesRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.Test

class NoSpacesRuleFixTest : FixTestBase("test/paragraph3/spaces", NoSpacesRule(),
        listOf(
                RulesConfig(Warnings.TOO_MANY_SPACES.name, true,
                        mapOf(
                                "max_spaces" to "1",
                                "saveInitialFormattingForEnums" to "true"
                        )
                )
        )
) {
    @Test
    fun `many spaces rule enum case`() {
        fixAndCompare("NoSpacesEnumExpected.kt", "NoSpacesEnumTest.kt")
    }

    @Test
    fun `many spaces rule`() {
        fixAndCompare("NoSpacesExpected.kt", "NoSpacesTest.kt")
    }
}
