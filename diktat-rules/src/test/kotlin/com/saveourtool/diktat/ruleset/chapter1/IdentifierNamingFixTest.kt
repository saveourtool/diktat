package com.saveourtool.diktat.ruleset.chapter1

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.rules.chapter1.IdentifierNaming
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class IdentifierNamingFixTest : FixTestBase(
    "test/paragraph1/naming",
    ::IdentifierNaming,
    listOf(
        RulesConfig("PACKAGE_NAME_INCORRECT", false, emptyMap()),
        RulesConfig("PACKAGE_NAME_INCORRECT_PREFIX", false, emptyMap())
    )
) {
    @Test
    @Tag(WarningNames.CLASS_NAME_INCORRECT)
    fun `regression in VARIABLE_NAME_INCORRECT_FORMAT`() {
        fixAndCompare("identifiers/IdentifierNameRegressionExpected.kt", "identifiers/IdentifierNameRegressionTest.kt")
    }

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

    @Test
    @Tag(WarningNames.TYPEALIAS_NAME_INCORRECT_CASE)
    fun `typeAlias name incorrect`() {
        fixAndCompare("identifiers/TypeAliasNameExpected.kt", "identifiers/TypeAliasNameTest.kt")
    }

    @Test
    @Tag(WarningNames.TYPEALIAS_NAME_INCORRECT_CASE)
    fun `should update property name in KDoc after fixing`() {
        fixAndCompare("identifiers/PropertyInKdocExpected.kt", "identifiers/PropertyInKdocTest.kt")
    }
}
