package org.cqfn.diktat.ruleset.utils

import org.cqfn.diktat.ruleset.rules.IdentifierNaming
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Test

class SuppressTest {

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
        lintMethod(IdentifierNaming(), code)
    }
}