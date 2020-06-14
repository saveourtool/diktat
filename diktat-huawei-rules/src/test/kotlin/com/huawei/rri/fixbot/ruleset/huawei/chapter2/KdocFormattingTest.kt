package com.huawei.rri.fixbot.ruleset.huawei.chapter2

import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings
import com.huawei.rri.fixbot.ruleset.huawei.rules.KdocFormatting
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.test.lint
import org.assertj.core.api.Assertions
import org.junit.Test

class KdocFormattingTest {
    @Test
    fun `there should be no blank line between kdoc and it's declaration code`() {
        Assertions.assertThat(
            KdocFormatting().lint(
                """
                    package com.huawei.test.resources.test.paragraph2.kdoc

                    /**
                     * declaration for some constant
                     */

                    const val SUPER_CONSTANT = 46

                    /**
                     * Kdoc docummentation
                     */

                    class SomeName {
                        /**
                         * another Kdoc
                         */

                        val variable = "string"

                        /**
                         * another Kdoc
                         */

                        fun somePublicFunction() {}

                    }


                    /**
                     * another Kdoc
                     */

                    fun someFunction() {}
                """.trimIndent()
            )
        ).containsExactly(
            LintError(5, 4, "kdoc-formatting", "${Warnings.BLANK_LINE_AFTER_KDOC.warnText()} SUPER_CONSTANT"),
            LintError(11, 4, "kdoc-formatting", "${Warnings.BLANK_LINE_AFTER_KDOC.warnText()} SomeName"),
            LintError(16, 8, "kdoc-formatting", "${Warnings.BLANK_LINE_AFTER_KDOC.warnText()} variable"),
            LintError(22, 8, "kdoc-formatting", "${Warnings.BLANK_LINE_AFTER_KDOC.warnText()} somePublicFunction"),
            LintError(31, 4, "kdoc-formatting", "${Warnings.BLANK_LINE_AFTER_KDOC.warnText()} someFunction")
        )
    }
}
