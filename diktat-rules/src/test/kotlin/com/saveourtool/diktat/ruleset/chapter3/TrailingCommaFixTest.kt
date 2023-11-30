package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.DIKTAT_COMMON
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter3.TrailingCommaRule
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TrailingCommaFixTest : FixTestBase("test/paragraph3/trailing_comma", ::TrailingCommaRule) {
    private val config: List<RulesConfig> = listOf(
        RulesConfig(
            Warnings.TRAILING_COMMA.name, true,
            mapOf("valueArgument" to "true",
                "valueParameter" to "true",
                "indices" to "true",
                "whenConditions" to "true",
                "collectionLiteral" to "true",
                "typeArgument" to "true",
                "typeParameter" to "true",
                "destructuringDeclaration" to "true")),
        RulesConfig(
            DIKTAT_COMMON, true,
            mapOf("kotlinVersion" to "1.4.21"))
    )

    @Test
    @Tag(WarningNames.TRAILING_COMMA)
    fun `should add all trailing comma`() {
        fixAndCompare("TrailingCommaExpected.kt", "TrailingCommaTest.kt", config)
    }
}
