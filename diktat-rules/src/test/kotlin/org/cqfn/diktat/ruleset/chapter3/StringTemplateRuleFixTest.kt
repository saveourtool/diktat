package org.cqfn.diktat.ruleset.chapter3

import generated.WarningNames
import org.cqfn.diktat.ruleset.rules.StringTemplateFormatRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class StringTemplateRuleFixTest : FixTestBase("test/paragraph3/string_template", ::StringTemplateFormatRule) {
    @Test
    fun `should fix enum order`() {
        fixAndCompare("StringTemplateExpected.kt", "StringTemplateTest.kt")
    }
}
