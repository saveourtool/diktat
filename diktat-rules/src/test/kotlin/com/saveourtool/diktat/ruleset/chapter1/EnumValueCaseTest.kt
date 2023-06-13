package com.saveourtool.diktat.ruleset.chapter1

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getRuleConfig
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter1.IdentifierNaming
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class EnumValueCaseTest : FixTestBase("test/paragraph1/naming", ::IdentifierNaming) {
    private val rulesConfigSnakeCaseEnum: List<RulesConfig> = listOf(
        RulesConfig(Warnings.ENUM_VALUE.name, true,
            mapOf("enumStyle" to "snakeCase"))
    )
    private val rulesConfigPascalCaseEnum: List<RulesConfig> = listOf(
        RulesConfig(Warnings.ENUM_VALUE.name, true,
            mapOf("enumStyle" to "pascalCase"))
    )
    private val rulesConfigEnumUnknownStyle: List<RulesConfig> = listOf(
        RulesConfig(Warnings.ENUM_VALUE.name, true,
            mapOf("enumStyle" to "otherCase"))
    )

    @Test
    @Tag(WarningNames.ENUM_VALUE)
    fun `incorrect enum snake case value fix`() {
        fixAndCompare("enum_/EnumValueSnakeCaseExpected.kt", "enum_/EnumValueSnakeCaseTest.kt", rulesConfigSnakeCaseEnum)
    }

    @Test
    @Tag(WarningNames.ENUM_VALUE)
    fun `incorrect enum pascal case value fix`() {
        fixAndCompare("enum_/EnumValuePascalCaseExpected.kt", "enum_/EnumValuePascalCaseTest.kt", rulesConfigPascalCaseEnum)
    }

    @Test
    @Tag(WarningNames.ENUM_VALUE)
    fun `incorrect enum unknown style`() {
        assertThrows<IllegalStateException> {
            IdentifierNaming.IdentifierNamingConfiguration(
                rulesConfigEnumUnknownStyle.getRuleConfig(Warnings.ENUM_VALUE)
                    ?.configuration ?: emptyMap()
            )
        }
    }
}
