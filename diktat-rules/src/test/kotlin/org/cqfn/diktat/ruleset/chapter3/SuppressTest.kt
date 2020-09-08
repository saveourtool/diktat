package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.rules.IdentifierNaming
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Test

class SuppressTest : LintTestBase(::IdentifierNaming) {

    @Test
    fun `test suppress`() {
        val code =
                """
                  @Suppress("FUNCTION_NAME_INCORRECT_CASE")
                  class SomeClass {
                    fun /* */ methODTREE(): String {

                    }
                  }
                """.trimIndent()
        lintMethod(code)
    }
}
