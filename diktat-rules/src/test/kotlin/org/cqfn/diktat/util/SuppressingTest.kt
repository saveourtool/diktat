package org.cqfn.diktat.util

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.BACKTICKS_PROHIBITED
import org.cqfn.diktat.ruleset.constants.Warnings.CLASS_NAME_INCORRECT
import org.cqfn.diktat.ruleset.constants.Warnings.CONFUSING_IDENTIFIER_NAMING
import org.cqfn.diktat.ruleset.constants.Warnings.CONSTANT_UPPERCASE
import org.cqfn.diktat.ruleset.constants.Warnings.ENUM_VALUE
import org.cqfn.diktat.ruleset.constants.Warnings.EXCEPTION_SUFFIX
import org.cqfn.diktat.ruleset.constants.Warnings.FUNCTION_BOOLEAN_PREFIX
import org.cqfn.diktat.ruleset.constants.Warnings.GENERIC_NAME
import org.cqfn.diktat.ruleset.constants.Warnings.IDENTIFIER_LENGTH
import org.cqfn.diktat.ruleset.constants.Warnings.OBJECT_NAME_INCORRECT
import org.cqfn.diktat.ruleset.constants.Warnings.VARIABLE_HAS_PREFIX
import org.cqfn.diktat.ruleset.constants.Warnings.VARIABLE_NAME_INCORRECT
import org.cqfn.diktat.ruleset.constants.Warnings.VARIABLE_NAME_INCORRECT_FORMAT
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter1.IdentifierNaming

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
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
            LintError(3,
                9,
                ruleId,
                "[IDENTIFIER_LENGTH] identifier's length is incorrect, it" +
                    " should be in range of [2, 64] symbols: a", false),
            rulesConfigList = rulesConfigBooleanFunctions,
        )
    }
}
