package org.cqfn.diktat.ruleset.chapter3.spaces

import org.cqfn.diktat.ruleset.rules.WhiteSpaceRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Test

class WhiteSpaceRuleFixTest : FixTestBase("test/paragraph3/spaces", WhiteSpaceRule()) {
    @Test
    fun `should keep single whitespace between keyword and opening parentheses`() {
        fixAndCompare("WhiteSpaceBeforeLParExpected.kt", "WhiteSpaceBeforeLParTest.kt")
    }

    @Test
    fun `should keep single whitespace between keyword and opening brace`() {
        fixAndCompare("WhiteSpaceBeforeLBraceExpected.kt", "WhiteSpaceBeforeLBraceTest.kt")
    }
}
