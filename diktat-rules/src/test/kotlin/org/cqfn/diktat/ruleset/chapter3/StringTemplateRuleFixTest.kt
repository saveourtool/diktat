package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.rules.chapter3.StringTemplateFormatRule
import org.cqfn.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class StringTemplateRuleFixTest : FixTestBase("test/paragraph3/string_template", ::StringTemplateFormatRule) {
    @Test
    @Tag(WarningNames.STRING_TEMPLATE_CURLY_BRACES)
    fun `should fix enum order`() {
        fixAndCompare("StringTemplateExpected.kt", "StringTemplateTest.kt")
    }
}
