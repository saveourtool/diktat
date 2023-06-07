package com.saveourtool.diktat.util

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.IDENTIFIER_LENGTH
import com.saveourtool.diktat.ruleset.rules.chapter1.IdentifierNaming

import com.saveourtool.diktat.api.DiktatError
import org.junit.jupiter.api.Test

class SuppressingTest : LintTestBase(::IdentifierNaming) {
    private val ruleId: String = "$DIKTAT_RULE_SET_ID:${IdentifierNaming.NAME_ID}"
    private val rulesConfigBooleanFunctions: List<RulesConfig> = listOf(
        RulesConfig(IDENTIFIER_LENGTH.name, true, emptyMap(), setOf("MySuperSuppress"))
    )

    @Test
    fun `checking that suppression with ignoredAnnotation works`() {
        val code =
            """
                @MySuperSuppress()
                fun foo() {
                    val a = 1
                }
            """.trimIndent()
        lintMethod(code, rulesConfigList = rulesConfigBooleanFunctions)
    }

    @Test
    fun `checking that suppression with ignore everything works`() {
        val code =
            """
                @Suppress("diktat")
                fun foo() {
                    val a = 1
                }
            """.trimIndent()
        lintMethod(code)
    }

    @Test
    fun `checking that suppression with a targeted inspection name works`() {
        val code =
            """
                @Suppress("IDENTIFIER_LENGTH")
                fun foo() {
                    val a = 1
                }
            """.trimIndent()
        lintMethod(code)
    }

    @Test
    fun `negative scenario for other annotation`() {
        val code =
            """
                @MySuperSuppress111()
                fun foo() {
                    val a = 1
                }
            """.trimIndent()
        lintMethod(
            code,
            DiktatError(3,
                9,
                ruleId,
                "[IDENTIFIER_LENGTH] identifier's length is incorrect, it" +
                        " should be in range of [2, 64] symbols: a", false),
            rulesConfigList = rulesConfigBooleanFunctions,
        )
    }
}
