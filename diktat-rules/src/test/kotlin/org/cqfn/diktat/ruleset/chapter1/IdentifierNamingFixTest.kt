package org.cqfn.diktat.ruleset.chapter1

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.rules.IdentifierNaming
import org.cqfn.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class IdentifierNamingFixTest : FixTestBase(
    "test/paragraph1/naming",
    ::IdentifierNaming,
    listOf(
        RulesConfig("PACKAGE_NAME_INCORRECT", false, mapOf()),
        RulesConfig("PACKAGE_NAME_INCORRECT_PREFIX", false, mapOf())
    )
) {
    @Test
    @Tag(WarningNames.CLASS_NAME_INCORRECT)
    fun `incorrect class name fix`() {
        fixAndCompare("class_/IncorrectClassNameExpected.kt", "class_/IncorrectClassNameTest.kt")
    }

    @Test
    @Tag(WarningNames.OBJECT_NAME_INCORRECT)
    fun `incorrect object name fix`() {
        fixAndCompare("object_/IncorrectObjectNameExpected.kt", "object_/IncorrectObjectNameTest.kt")
    }

    @Test
    @Tag(WarningNames.ENUM_VALUE)
    fun `incorrect enum values case fix`() {
        fixAndCompare("enum_/EnumValueSnakeCaseExpected.kt", "enum_/EnumValueSnakeCaseTest.kt")
    }

    @Test
    @Tag(WarningNames.CONSTANT_UPPERCASE)
    fun `incorrect constant name case fix`() {
        fixAndCompare("identifiers/ConstantValNameExpected.kt", "identifiers/ConstantValNameTest.kt")
    }

    @Test
    @Tag(WarningNames.VARIABLE_NAME_INCORRECT_FORMAT)
    fun `incorrect variable name case fix`() {
        fixAndCompare("identifiers/VariableNamingExpected.kt", "identifiers/VariableNamingTest.kt")
    }

    @Test
    @Tag(WarningNames.VARIABLE_HAS_PREFIX)
    fun `incorrect variable prefix fix`() {
        fixAndCompare("identifiers/PrefixInNameExpected.kt", "identifiers/PrefixInNameTest.kt")
    }

    @Test
    @Tag(WarningNames.VARIABLE_NAME_INCORRECT_FORMAT)
    fun `incorrect lambda argument case fix`() {
        fixAndCompare("identifiers/LambdaArgExpected.kt", "identifiers/LambdaArgTest.kt")
    }
}
