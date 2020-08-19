package org.cqfn.diktat.ruleset.chapter1

import org.cqfn.diktat.ruleset.rules.IdentifierNaming
import org.cqfn.diktat.util.FixTestBase
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.junit.jupiter.api.Test

class IdentifierNamingFixTest : FixTestBase(
        "test/paragraph1/naming",
        IdentifierNaming(),
        listOf(
                RulesConfig("PACKAGE_NAME_INCORRECT", false, mapOf()),
                RulesConfig("PACKAGE_NAME_INCORRECT_PREFIX", false, mapOf())
        )
) {
    @Test
    fun `incorrect class name fix`() {
        fixAndCompare("class_/IncorrectClassNameExpected.kt", "class_/IncorrectClassNameTest.kt")
    }

    @Test
    fun `incorrect object name fix`() {
        fixAndCompare("object_/IncorrectObjectNameExpected.kt", "object_/IncorrectObjectNameTest.kt")
    }

    @Test
    fun `incorrect enum values case fix`() {
        fixAndCompare("enum_/EnumValueCaseExpected.kt", "enum_/EnumValueCaseTest.kt")
    }

    @Test
    fun `incorrect constant name case fix`() {
        fixAndCompare("identifiers/ConstantValNameExpected.kt", "identifiers/ConstantValNameTest.kt")
    }

    @Test
    fun `incorrect variable name case fix`() {
        fixAndCompare("identifiers/VariableNamingExpected.kt", "identifiers/VariableNamingTest.kt")
    }

    @Test
    fun `incorrect variable prefix fix`() {
        fixAndCompare("identifiers/PrefixInNameExpected.kt", "identifiers/PrefixInNameTest.kt")
    }

    @Test
    fun `incorrect lambda argument case fix`() {
        fixAndCompare("identifiers/LambdaArgExpected.kt", "identifiers/LambdaArgTest.kt")
    }
}
