package org.diktat.ruleset.chapter1

import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.diktat.ruleset.rules.IdentifierNaming
import org.diktat.ruleset.constants.Warnings.*

class MethodNamingWarnTest {
    @Test
    fun `method name incorrect, part 1`() {
        assertThat(
            IdentifierNaming().lint(
                """
                  class SomeClass {
                    fun /* */ methODTREE(): String {

                    }
                  }
                """.trimIndent()
            )
        ).containsExactly(LintError(2, 13, "identifier-naming", "${FUNCTION_NAME_INCORRECT_CASE.warnText()} methODTREE"))
    }

    @Test
    fun `method name incorrect, part 2`() {
        assertThat(
            IdentifierNaming().lint(
                """
                  class TestPackageName {
                    fun method_two(): String {
                        return ""
                    }
                  }
                """.trimIndent()
            )
        ).containsExactly(LintError(2, 7, "identifier-naming", "${FUNCTION_NAME_INCORRECT_CASE.warnText()} method_two"))
    }

    @Test
    fun `method name incorrect, part 3`() {
        assertThat(
            IdentifierNaming().lint(
                """
                    fun String.methODTREE(): String {
                        fun TEST(): Unit {
                            return ""
                        }
                    }
                """.trimIndent()
            )
        ).containsExactly(
            LintError(1, 12, "identifier-naming", "${FUNCTION_NAME_INCORRECT_CASE.warnText()} methODTREE"),
            LintError(2, 9, "identifier-naming", "${FUNCTION_NAME_INCORRECT_CASE.warnText()} TEST")
        )
    }

    @Test
    fun `method name incorrect, part 4`() {
        assertThat(
            IdentifierNaming().lint(
                """
                  class TestPackageName {
                    fun methODTREE(): String {
                    }
                  }
                """.trimIndent()
            )
        ).containsExactly(LintError(2, 7, "identifier-naming", "${FUNCTION_NAME_INCORRECT_CASE.warnText()} methODTREE"))
    }

    @Test
    fun `method name incorrect, part 5`() {
        assertThat(
            IdentifierNaming().lint(
                """
                  class TestPackageName {
                    fun methODTREE() {
                    }
                  }
                """.trimIndent()
            )
        ).containsExactly(LintError(2, 7, "identifier-naming", "${FUNCTION_NAME_INCORRECT_CASE.warnText()} methODTREE"))
    }

    @Test
    fun `boolean method name incorrect`() {
        assertThat(
            IdentifierNaming().lint(
                """
                 fun someBooleanCheck(): Boolean {
                     return false
                 }
                """.trimIndent()
            )
        ).containsExactly(LintError(1, 5, "identifier-naming", "${FUNCTION_BOOLEAN_PREFIX.warnText()} someBooleanCheck"))
    }
}
