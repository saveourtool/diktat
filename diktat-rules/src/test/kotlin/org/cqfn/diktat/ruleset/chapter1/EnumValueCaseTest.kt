package org.cqfn.diktat.ruleset.chapter1

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.rules.IdentifierNaming
import org.cqfn.diktat.util.FixTestBase
import org.junit.Test

class EnumValueCaseTest : FixTestBase ("test/paragraph1/naming", IdentifierNaming(),
        listOf(
                RulesConfig("ENUM_VALUE", true, mapOf()))
){
    @Test
    fun `incorrect enum value (fix)`() {
        fixAndCompare("enum_/EnumValueCaseExpected.kt", "enum_/EnumValueCaseTest.kt")
    }
}