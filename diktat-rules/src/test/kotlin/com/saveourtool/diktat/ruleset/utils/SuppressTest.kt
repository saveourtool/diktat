package com.saveourtool.diktat.ruleset.utils

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter1.IdentifierNaming
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import org.junit.jupiter.api.Test

class SuppressTest : LintTestBase(::IdentifierNaming) {
    private val ruleId: String = "$DIKTAT_RULE_SET_ID:${IdentifierNaming.NAME_ID}"

    @Test
    fun `test suppress on class`() {
        val code =
            """
                @Suppress("FUNCTION_NAME_INCORRECT_CASE", "BACKTICKS_PROHIBITED")
                class SomeClass {
                    fun /* */ methODTREE(): String {

                    }

                    fun `some`() {}
                }
            """.trimIndent()
        lintMethod(code)
    }

    @Test
    fun `check suppress on method`() {
        lintMethod(
            """
                    |class SomeClass {
                    |
                    |   @Suppress("FUNCTION_NAME_INCORRECT_CASE")
                    |   fun /* */ methODTREE(): String {
                    |
                    |       fun soMEMETHOD() {
                    |
                    |       }
                    |
                    |   }
                    |
                    |   fun /* */ methODTREEASA(): String {
                    |
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(12, 14, ruleId, "${Warnings.FUNCTION_NAME_INCORRECT_CASE.warnText()} methODTREEASA",
                true)
        )
    }

    @Test
    fun `check suppress on variable`() {
        lintMethod(
            """
                    |class SomeClass {
                    |
                    |   @Suppress("FUNCTION_NAME_INCORRECT_CASE")
                    |   fun /* */ methODTREE(): String {
                    |       @Suppress( "VARIABLE_NAME_INCORRECT_FORMAT" )
                    |       var SOMEvar = 5
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    fun `test suppress on file`() {
        val code =
            """
                @file:Suppress("FUNCTION_NAME_INCORRECT_CASE")

                class SomeClass {
                    fun /* */ methODTREE(): String {

                    }
                }
            """.trimIndent()
        lintMethod(code)
    }

    @Test
    fun `test suppress field`() {
        val code =
            """
                class SomeClass(@field:Suppress("IDENTIFIER_LENGTH") val a:String) {
                    fun /* */ method(): String {

                    }
                }
            """.trimIndent()
        lintMethod(code)
    }

    @Test
    fun `test suppress field with set`() {
        val code =
            """
                class SomeClass() {
                    @set:[Suppress("IDENTIFIER_LENGTH") Inject]
                    val a = 5

                    fun /* */ method(): String {

                    }
                }
            """.trimIndent()
        lintMethod(code)
    }

    @Test
    fun `check simple wrong enum`() {
        lintMethod(
            """
                    |@set:[Suppress("WRONG_DECLARATION_ORDER") Suppress("IDENTIFIER_LENGTH") Suppress("CONFUSING_IDENTIFIER_NAMING")]
                    |enum class Alph {
                    |   D,
                    |   C,
                    |   A,
                    |   B,
                    |   ;
                    |}
            """.trimMargin()
        )
    }

    @Test
    fun `test suppress on class bad`() {
        val code =
            """
                @Suppress()
                class SomeClass {
                    fun /* */ methODTREE(): String {

                    }
                }
            """.trimIndent()
        lintMethod(code,
            DiktatError(3, 15, "$DIKTAT_RULE_SET_ID:${IdentifierNaming.NAME_ID}",
                "${Warnings.FUNCTION_NAME_INCORRECT_CASE.warnText()} methODTREE", true))
    }
}
