package com.huawei.rri.fixbot.ruleset.huawei.chapter2

import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings.BLANK_LINE_AFTER_KDOC
import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings.KDOC_NO_DEPRECATED_TAG
import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings.KDOC_NO_EMPTY_TAGS
import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings.KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS
import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings.KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS
import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings.KDOC_WRONG_SPACES_AFTER_TAG
import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings.KDOC_WRONG_TAGS_ORDER
import com.huawei.rri.fixbot.ruleset.huawei.rules.KdocFormatting
import com.huawei.rri.fixbot.ruleset.huawei.utils.lintMethod
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
            LintError(5, 4, "kdoc-formatting", "${BLANK_LINE_AFTER_KDOC.warnText()} SUPER_CONSTANT"),
            LintError(11, 4, "kdoc-formatting", "${BLANK_LINE_AFTER_KDOC.warnText()} SomeName"),
            LintError(16, 8, "kdoc-formatting", "${BLANK_LINE_AFTER_KDOC.warnText()} variable"),
            LintError(22, 8, "kdoc-formatting", "${BLANK_LINE_AFTER_KDOC.warnText()} somePublicFunction"),
            LintError(31, 4, "kdoc-formatting", "${BLANK_LINE_AFTER_KDOC.warnText()} someFunction")
        )
    }

    private val funCode = """
         fun foo(a: Int): Int {
             if (false) throw IllegalStateException()
             return 2 * a
         }
    """.trimIndent()

    @Test
    fun `@deprecated tag is not allowed`() {
        val invalidCode = """
            /**
             * @deprecated use foo instead
             */
            fun bar() = Unit
        """.trimIndent()

        lintMethod(KdocFormatting(), invalidCode, LintError(2, 4, "kdoc-formatting",
            "${KDOC_NO_DEPRECATED_TAG.warnText()} @deprecated use foo instead"))
    }

    @Test
    fun `no empty descriptions in tag blocks are allowed`() {
        val invalidCode = """
            /**
             * @param a
             * @return
             * @throws IllegalStateException
             */
             $funCode
        """.trimIndent()

        lintMethod(KdocFormatting(), invalidCode,
            LintError(3, 16, "kdoc-formatting",
                "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false),
            LintError(3, 16, "kdoc-formatting",
                "${KDOC_WRONG_SPACES_AFTER_TAG.warnText()} @return", false))
    }

    @Test
    fun `KDocs should contain only one white space between tag and its content (positive example)`() {
        val validCode = """
            /**
             * @param a dummy int
             * @return doubled value
             * @throws IllegalStateException
             */
             $funCode
        """.trimIndent()

        lintMethod(KdocFormatting(), validCode)
    }

    @Test
    fun `KDocs should contain only one white space between tag and its content`() {
        val invalidCode = """
            /**
             * @param  a dummy int
             * @param b   dummy int
             * @return  doubled value
             * @throws${'\t'}IllegalStateException
             */
             $funCode
        """.trimIndent()

        lintMethod(KdocFormatting(), invalidCode,
            LintError(2, 16, "kdoc-formatting",
                "${KDOC_WRONG_SPACES_AFTER_TAG.warnText()} @param", true),
            LintError(3, 16, "kdoc-formatting",
                "${KDOC_WRONG_SPACES_AFTER_TAG.warnText()} @param", true),
            LintError(4, 16, "kdoc-formatting",
                "${KDOC_WRONG_SPACES_AFTER_TAG.warnText()} @return", true),
            LintError(5, 16, "kdoc-formatting",
                "${KDOC_WRONG_SPACES_AFTER_TAG.warnText()} @throws", true))
    }

    @Test
    fun `tags should be ordered in KDocs (positive example)`() {
        val validCode = """
            /**
             * @param a dummy int
             * @return doubled value
             * @throws IllegalStateException
             */
             $funCode
        """.trimIndent()

        lintMethod(KdocFormatting(), validCode)
    }

    @Test
    fun `tags should be ordered in KDocs`() {
        val invalidCode = """
            /**
             * @return doubled value
             * @throws IllegalStateException
             * @param a dummy int
             */
             $funCode
        """.trimIndent()

        lintMethod(KdocFormatting(), invalidCode,
            LintError(2, 16, "kdoc-formatting",
                "${KDOC_WRONG_TAGS_ORDER.warnText()} @return, @throws, @param", true))
    }

    @Test
    fun `newlines are not allowed between basic tags`() {
        val invalidCode = """
            /**
             * @param a dummy int
             *
             * @return doubled value

             * @throws IllegalStateException
             */
             $funCode
        """.trimIndent()

        lintMethod(KdocFormatting(), invalidCode,
            LintError(2, 16, "kdoc-formatting",
                "${KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS.warnText()} @param", true),
            LintError(4, 16, "kdoc-formatting",
                "${KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS.warnText()} @return", true))
    }

    @Test
    fun `special tags should have exactly one newline after them (positive example)`() {
        val validCode = """
            /**
             * @implSpec stuff
             * implementation details
             *
             * @apiNote foo
             *
             * @implNote bar
             *
             */
            $funCode
        """.trimIndent()

        lintMethod(KdocFormatting(), validCode)
    }

    @Test
    fun `special tags should have exactly one newline after them (no newline)`() {
        val invalidCode = """
            /**
             * @implSpec stuff
             * @apiNote foo
             * @implNote bar
             */
            $funCode
        """.trimIndent()

        lintMethod(KdocFormatting(), invalidCode,
            LintError(2, 16, "kdoc-formatting",
                "${KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS.warnText()} @implSpec, @apiNote, @implNote", true))
    }

    @Test
    fun `special tags should have exactly one newline after them (many lines)`() {
        val invalidCode = """
            /**
             * @implSpec stuff
             *

             * @apiNote foo
             *
             *
             *
             * @implNote bar

             */
            $funCode
        """.trimIndent()

        lintMethod(KdocFormatting(), invalidCode,
            LintError(2, 16, "kdoc-formatting",
                "${KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS.warnText()} @implSpec, @apiNote, @implNote", true))
    }
}
