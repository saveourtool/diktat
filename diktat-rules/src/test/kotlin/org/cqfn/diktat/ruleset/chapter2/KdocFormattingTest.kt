package org.cqfn.diktat.ruleset.chapter2

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.constants.StringWarnings
import org.cqfn.diktat.ruleset.constants.Warnings.BLANK_LINE_AFTER_KDOC
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NEWLINES_BEFORE_BASIC_TAGS
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NO_DEPRECATED_TAG
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NO_EMPTY_TAGS
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_WRONG_SPACES_AFTER_TAG
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_WRONG_TAGS_ORDER
import org.cqfn.diktat.ruleset.rules.kdoc.KdocFormatting
import org.cqfn.diktat.util.lintMethod
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_EMPTY_KDOC
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.util.TEST_FILE_NAME
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class KdocFormattingTest {

    private val ruleId: String = "$DIKTAT_RULE_SET_ID:kdoc-formatting"

    @Test
    @Tag(StringWarnings.BLANK_LINE_AFTER_KDOC)
    fun `there should be no blank line between kdoc and it's declaration code`() {
        val code =
            """
                    package org.cqfn.diktat.test.resources.test.paragraph2.kdoc

                    /**
                     * declaration for some constant
                     */

                    const val SUPER_CONSTANT = 46

                    /**
                     * Kdoc documentation
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

        lintMethod(KdocFormatting(), code,
                LintError(5, 4, ruleId, "${BLANK_LINE_AFTER_KDOC.warnText()} SUPER_CONSTANT", true),
                LintError(11, 4, ruleId, "${BLANK_LINE_AFTER_KDOC.warnText()} SomeName", true),
                LintError(16, 8, ruleId, "${BLANK_LINE_AFTER_KDOC.warnText()} variable", true),
                LintError(22, 8, ruleId, "${BLANK_LINE_AFTER_KDOC.warnText()} somePublicFunction", true),
                LintError(31, 4, ruleId, "${BLANK_LINE_AFTER_KDOC.warnText()} someFunction", true)
        )
    }

    private val funCode = """
         fun foo(a: Int): Int {
             if (false) throw IllegalStateException()
             return 2 * a
         }
    """.trimIndent()

    @Test
    @Tag(StringWarnings.KDOC_EMPTY_KDOC)
    fun `empty KDocs are not allowed - example with empty KDOC_SECTION`() {
        lintMethod(KdocFormatting(),
                """/**
               | *${" ".repeat(5)}
               | */
               |fun foo() = Unit
            """.trimMargin(),
                LintError(1, 1, ruleId, "${KDOC_EMPTY_KDOC.warnText()} foo", false)
        )
    }

    @Test
    @Tag(StringWarnings.KDOC_EMPTY_KDOC)
    fun `empty KDocs are not allowed - example with no KDOC_SECTION`() {
        lintMethod(KdocFormatting(),
                """/**
               | */
               |fun foo() = Unit
            """.trimMargin(),
                LintError(1, 1, ruleId, "${KDOC_EMPTY_KDOC.warnText()} foo", false)
        )
    }

    @Test
    @Tag(StringWarnings.KDOC_EMPTY_KDOC)
    fun `empty KDocs are not allowed - without bound identifier`() {
        lintMethod(KdocFormatting(),
                """/**
               | *
               | */
            """.trimMargin(),
                LintError(1, 1, ruleId, "${KDOC_EMPTY_KDOC.warnText()} $TEST_FILE_NAME", false)
        )
    }

    @Test
    @Tag(StringWarnings.KDOC_EMPTY_KDOC)
    fun `empty KDocs are not allowed - with anonymous entity`() {
        lintMethod(KdocFormatting(),
                """class Example {
               |    /**
               |      *
               |      */
               |    companion object { }
               |}
            """.trimMargin(),
                LintError(2, 5, ruleId, "${KDOC_EMPTY_KDOC.warnText()} object", false)
        )
    }

    @Test
    @Tag(StringWarnings.KDOC_NO_DEPRECATED_TAG)
    fun `@deprecated tag is not allowed`() {
        val invalidCode = """
            /**
             * @deprecated use foo instead
             */
            fun bar() = Unit
        """.trimIndent()

        lintMethod(KdocFormatting(), invalidCode,
                LintError(2, 4, ruleId, "${KDOC_NO_DEPRECATED_TAG.warnText()} @deprecated use foo instead", true)
        )
    }

    @Test
    @Tag(StringWarnings.KDOC_NO_EMPTY_TAGS)
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
                LintError(3, 16, ruleId,
                        "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false))
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
    @Tag(StringWarnings.KDOC_WRONG_SPACES_AFTER_TAG)
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
                LintError(2, 16, ruleId,
                        "${KDOC_WRONG_SPACES_AFTER_TAG.warnText()} @param", true),
                LintError(3, 16, ruleId,
                        "${KDOC_WRONG_SPACES_AFTER_TAG.warnText()} @param", true),
                LintError(4, 16, ruleId,
                        "${KDOC_WRONG_SPACES_AFTER_TAG.warnText()} @return", true),
                LintError(5, 16, ruleId,
                        "${KDOC_WRONG_SPACES_AFTER_TAG.warnText()} @throws", true))
    }

    @Test
    @Tag(StringWarnings.KDOC_WRONG_TAGS_ORDER)
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
    @Tag(StringWarnings.KDOC_WRONG_TAGS_ORDER)
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
                LintError(2, 16, ruleId,
                        "${KDOC_WRONG_TAGS_ORDER.warnText()} @return, @throws, @param", true))
    }

    @Test
    @Tag(StringWarnings.KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS)
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
                LintError(2, 16, ruleId,
                        "${KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS.warnText()} @param", true),
                LintError(4, 16, ruleId,
                        "${KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS.warnText()} @return", true))
    }

    @Test
    @Tag(StringWarnings.KDOC_NEWLINES_BEFORE_BASIC_TAGS)
    fun `basic tags block should have empty line before if there is other KDoc content (positive example)`() {
        lintMethod(KdocFormatting(),
                """/**
               | * Lorem ipsum
               | * dolor sit amet
               | *
               | * @param a integer parameter
               | */
               |fun test(a: Int): Unit = Unit
            """.trimMargin()
        )
    }

    @Test
    @Tag(StringWarnings.KDOC_NEWLINES_BEFORE_BASIC_TAGS)
    fun `basic tags block shouldn't have empty line before if there is no other KDoc content`() {
        lintMethod(KdocFormatting(),
                """/**
               | *
               | * @param a integer parameter
               | */
               |fun test(a: Int): Unit = Unit
            """.trimMargin(),
                LintError(3, 4, ruleId, "${KDOC_NEWLINES_BEFORE_BASIC_TAGS.warnText()} @param", true)
        )
    }

    @Test
    @Tag(StringWarnings.KDOC_NEWLINES_BEFORE_BASIC_TAGS)
    fun `basic tags block should have empty line before if there is other KDoc content`() {
        lintMethod(KdocFormatting(),
                """/**
               | * Lorem ipsum
               | * dolor sit amet
               | * @param a integer parameter
               | */
               |fun test(a: Int): Unit = Unit
            """.trimMargin(),
                LintError(4, 4, ruleId, "${KDOC_NEWLINES_BEFORE_BASIC_TAGS.warnText()} @param", true)
        )
    }

    @Test
    @Tag(StringWarnings.KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS)
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
    @Tag(StringWarnings.KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS)
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
                LintError(2, 16, ruleId,
                        "${KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS.warnText()} @implSpec, @apiNote, @implNote", true))
    }

    @Test
    @Tag(StringWarnings.KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS)
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
                LintError(2, 16, ruleId,
                        "${KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS.warnText()} @implSpec, @apiNote, @implNote", true))
    }
}
