package com.huawei.rri.fixbot.ruleset.huawei.chapter1

import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings.*
import com.huawei.rri.fixbot.ruleset.huawei.rules.KdocComments

class KdocWarnTest {
    @Test
    fun `all public classes should be documented with KDoc`() {
        assertThat(
            KdocComments().lint(
                """
                    class SomeGoodName() {
                    }

                    public open class SomeOtherGoodName() {
                    }

                    open class SomeNewGoodName() {
                    }

                    public class SomeOtherNewGoodName() {
                    }

                """.trimIndent()
            )
        ).containsExactly(
            LintError(1, 1, "kdoc-comments", "${MISSING_KDOC_TOP_LEVEL.warnText} SomeGoodName"),
            LintError(4, 1, "kdoc-comments", "${MISSING_KDOC_TOP_LEVEL.warnText} SomeOtherGoodName"),
            LintError(7, 1, "kdoc-comments", "${MISSING_KDOC_TOP_LEVEL.warnText} SomeNewGoodName"),
            LintError(10, 1, "kdoc-comments", "${MISSING_KDOC_TOP_LEVEL.warnText} SomeOtherNewGoodName")
        )
    }

    @Test
    fun `all internal classes should be documented with KDoc`() {
        assertThat(
            KdocComments().lint(
                """
                    internal class SomeGoodName() {
                    }
                """.trimIndent()
            )
        ).containsExactly(LintError(
            1, 1, "kdoc-comments", "${MISSING_KDOC_TOP_LEVEL.warnText} SomeGoodName")
        )
    }

    @Test
    fun `all internal and public functions on top-level should be documented with Kdoc`() {
        assertThat(
            KdocComments().lint(
                """
                    fun someGoodName() {
                    }

                    internal fun someGoodNameNew(): String {
                        return " ";
                    }
                """.trimIndent()
            )
        ).containsExactly(
            LintError(1, 1, "kdoc-comments", "${MISSING_KDOC_TOP_LEVEL.warnText} someGoodName"),
            LintError(4, 1, "kdoc-comments", "${MISSING_KDOC_TOP_LEVEL.warnText} someGoodNameNew")
        )
    }
}
