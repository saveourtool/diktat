package org.cqfn.diktat.ruleset.chapter1

import org.cqfn.diktat.ruleset.rules.IdentifierNaming
import org.cqfn.diktat.util.FixTestBase
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.StringWarnings
import org.junit.jupiter.api.Tag
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
    @Tag(StringWarnings.CLASS_NAME_INCORRECT)
    fun `incorrect class name fix`() {
        fixAndCompare("class_/IncorrectClassNameExpected.kt", "class_/IncorrectClassNameTest.kt")
    }

    @Test
    @Tag(StringWarnings.OBJECT_NAME_INCORRECT)
    fun `incorrect object name fix`() {
        fixAndCompare("object_/IncorrectObjectNameExpected.kt", "object_/IncorrectObjectNameTest.kt")
    }

    @Test
    @Tag(StringWarnings.ENUM_VALUE)
    fun `incorrect enum values case fix`() {
        fixAndCompare("enum_/EnumValueCaseExpected.kt", "enum_/EnumValueCaseTest.kt")
    }

    @Test
    @Tag(StringWarnings.CONSTANT_UPPERCASE)
    fun `incorrect constant name case fix`() {
        fixAndCompare("identifiers/ConstantValNameExpected.kt", "identifiers/ConstantValNameTest.kt")
    }

    @Test
    @Tag(StringWarnings.VARIABLE_NAME_INCORRECT_FORMAT)
    fun `incorrect variable name case fix`() {
        fixAndCompare("identifiers/VariableNamingExpected.kt", "identifiers/VariableNamingTest.kt")
    }

    @Test
    @Tag(StringWarnings.VARIABLE_HAS_PREFIX)
    fun `incorrect variable prefix fix`() {
        fixAndCompare("identifiers/PrefixInNameExpected.kt", "identifiers/PrefixInNameTest.kt")
    }

    @Test
    @Tag(StringWarnings.VARIABLE_HAS_PREFIX)
    fun `incorrect lambda argument case fix`() {
        fixAndCompare("identifiers/LambdaArgExpected.kt", "identifiers/LambdaArgTest.kt")
    }
}
