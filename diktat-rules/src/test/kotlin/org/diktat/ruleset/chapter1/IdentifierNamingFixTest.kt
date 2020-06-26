package org.diktat.ruleset.chapter1

import org.diktat.ruleset.rules.IdentifierNaming
import org.diktat.ruleset.utils.FixTestBase
import org.diktat.common.config.rules.RulesConfig
import org.junit.Test

class IdentifierNamingFixTest : FixTestBase(
    "test/paragraph1/naming",
    IdentifierNaming(),
    listOf(
            RulesConfig("PACKAGE_NAME_INCORRECT", false, mapOf()),
            RulesConfig("PACKAGE_NAME_INCORRECT_PREFIX", false, mapOf())
    )
) {

    @Test
    fun `incorrect class name (fix)`() {
        fixAndCompare("class_/IncorrectClassNameExpected.kt", "class_/IncorrectClassNameTest.kt")
    }

    @Test
    fun `incorrect object name (fix)`() {
        fixAndCompare("object_/IncorrectObjectNameExpected.kt", "object_/IncorrectObjectNameTest.kt")
    }

    @Test
    fun `incorrect enum values case (fix)`() {
        fixAndCompare("enum_/EnumValueCaseExpected.kt", "enum_/EnumValueCaseTest.kt")
    }
}
