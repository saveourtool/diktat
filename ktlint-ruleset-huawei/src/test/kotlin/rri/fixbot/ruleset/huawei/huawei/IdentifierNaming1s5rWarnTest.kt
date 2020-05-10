package rri.fixbot.ruleset.huawei.huawei

import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import rri.fixbot.ruleset.huawei.IdentifierNaming1s2r
import rri.fixbot.ruleset.huawei.constants.Warnings.*

class IdentifierNaming1s5rWarnTest {
    @Test
    fun `method name incorrect, part 1`() {
        assertThat(
            IdentifierNaming1s2r().lint(
                """
                  class METHOD1 {
                    fun /* */ methODTREE(): String {

                    }
                  }
                """.trimIndent()
            )
        ).isEmpty()
    }

    @Test
    fun `method name incorrect, part 2`() {
        assertThat(
            IdentifierNaming1s2r().lint(
                """
                  class TestPackageName {
                    fun method_two(): String {
                    }
                    return ""
                  }
                """.trimIndent()
            )
        ).isEmpty()
    }

    @Test
    fun `method name incorrect, part 3`() {
        assertThat(
            IdentifierNaming1s2r().lint(
                """
                    fun String.methODTREE(): String {
                        fun TEST(): Unit {
                        }
                        return ""
                    }
                """.trimIndent()
            )
        ).isEmpty()
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
        ).isEmpty()
    }

    @Test
    fun `method name incorrect, part 5`() {
        assertThat(
            IdentifierNaming1s2r().lint(
                """
                  class TestPackageName {
                    fun methODTREE() {
                    }
                    return ""
                  }
                """.trimIndent()
            )
        ).isEmpty()
    }

    @Test
    fun `boolean method name incorrect`() {
        assertThat(
            IdentifierNaming1s2r().lint(
                """
                 fun someBooleabCheck(): Boolean {
                     return false
                 }
                """.trimIndent()
            )
        ).isEmpty()
    }
}
