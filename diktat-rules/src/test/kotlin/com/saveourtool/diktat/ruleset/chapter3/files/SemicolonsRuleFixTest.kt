package com.saveourtool.diktat.ruleset.chapter3.files

import com.saveourtool.diktat.ruleset.rules.chapter3.files.SemicolonsRule
import com.saveourtool.diktat.util.FixTestBase
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class SemicolonsRuleFixTest : FixTestBase("test/paragraph3/semicolons", ::SemicolonsRule) {
    @Test
    @Tag(WarningNames.REDUNDANT_SEMICOLON)
    fun `should remove redundant semicolons`() {
        fixAndCompare("SemicolonsExpected.kt", "SemicolonsTest.kt")
    }
}
