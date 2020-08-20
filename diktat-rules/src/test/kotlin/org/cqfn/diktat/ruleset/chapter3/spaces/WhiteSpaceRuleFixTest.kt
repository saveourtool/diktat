package org.cqfn.diktat.ruleset.chapter3.spaces

import generated.WarningNames
import org.cqfn.diktat.ruleset.rules.WhiteSpaceRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class WhiteSpaceRuleFixTest : FixTestBase("test/paragraph3/spaces", WhiteSpaceRule()) {
    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `should keep single whitespace between keyword and opening parentheses`() {
        fixAndCompare("WhiteSpaceBeforeLParExpected.kt", "WhiteSpaceBeforeLParTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `should keep single whitespace between keyword and opening brace`() {
        fixAndCompare("WhiteSpaceBeforeLBraceExpected.kt", "WhiteSpaceBeforeLBraceTest.kt")
    }
}
