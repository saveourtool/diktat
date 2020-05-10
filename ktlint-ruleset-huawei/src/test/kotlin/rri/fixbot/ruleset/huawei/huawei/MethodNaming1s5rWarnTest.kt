package rri.fixbot.ruleset.huawei.huawei

import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import rri.fixbot.ruleset.huawei.IdentifierNaming1s2r
import rri.fixbot.ruleset.huawei.constants.Warnings.*

class MethodNaming1s5rWarnTest {
    @Test
    fun `method name incorrect, part 1`() {
        assertThat(
            IdentifierNaming1s2r().lint(
                """
                  class SomeClass {
                    fun /* */ methODTREE(): String {

                    }
                  }
                """.trimIndent()
            )
        ).containsExactly(LintError(2, 13, "identifier-naming", "${FUNCTION_NAME_INCORRECT_CASE.text} methODTREE"))
    }

    @Test
    fun `method name incorrect, part 2`() {
        assertThat(
            IdentifierNaming1s2r().lint(
                """
                  class TestPackageName {
                    fun method_two(): String {
                        return ""
                    }
                  }
                """.trimIndent()
            )
        ).containsExactly(LintError(2, 7, "identifier-naming", "${FUNCTION_NAME_INCORRECT_CASE.text} method_two"))
    }

    @Test
    fun `method name incorrect, part 3`() {
        assertThat(
            IdentifierNaming1s2r().lint(
                """
                    fun String.methODTREE(): String {
                        fun TEST(): Unit {
                            return ""
                        }
                    }
                """.trimIndent()
            )
        ).containsExactly(
            LintError(1, 12, "identifier-naming", "${FUNCTION_NAME_INCORRECT_CASE.text} methODTREE"),
            LintError(2, 9, "identifier-naming", "${FUNCTION_NAME_INCORRECT_CASE.text} TEST")
        )
    }

    @Test
    fun `method name incorrect, part 4`() {
        assertThat(
            IdentifierNaming1s2r().lint(
                """
                  class TestPackageName {
                    fun methODTREE(): String {
                    }
                  }
                """.trimIndent()
            )
        ).containsExactly(LintError(2, 7, "identifier-naming", "${FUNCTION_NAME_INCORRECT_CASE.text} methODTREE"))
    }

    @Test
    fun `method name incorrect, part 5`() {
        assertThat(
            IdentifierNaming1s2r().lint(
                """
                  class TestPackageName {
                    fun methODTREE() {
                    }
                  }
                """.trimIndent()
            )
        ).containsExactly(LintError(2, 7, "identifier-naming", "${FUNCTION_NAME_INCORRECT_CASE.text} methODTREE"))
    }

    @Test
    fun `boolean method name incorrect`() {
        assertThat(
            IdentifierNaming1s2r().lint(
                """
                 fun someBooleanCheck(): Boolean {
                     return false
                 }
                """.trimIndent()
            )
        ).containsExactly(LintError(1, 5, "identifier-naming", "${FUNCTION_BOOLEAN_PREFIX.text} someBooleanCheck"))
    }
}
