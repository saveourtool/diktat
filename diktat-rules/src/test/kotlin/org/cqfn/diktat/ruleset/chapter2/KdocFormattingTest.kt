package org.cqfn.diktat.ruleset.chapter2

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NEWLINES_BEFORE_BASIC_TAGS
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NO_DEPRECATED_TAG
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NO_EMPTY_TAGS
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_WRONG_SPACES_AFTER_TAG
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_WRONG_TAGS_ORDER
import org.cqfn.diktat.ruleset.rules.kdoc.KdocFormatting
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_EMPTY_KDOC
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.util.LintTestBase
import org.cqfn.diktat.util.testFileName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class KdocFormattingTest : LintTestBase(::KdocFormatting) {

    private val ruleId: String = "$DIKTAT_RULE_SET_ID:kdoc-formatting"

    private val funCode = """
         fun foo(a: Int): Int {
             if (false) throw IllegalStateException()
             return 2 * a
         }
    """.trimIndent()

    @Test
    @Tag(WarningNames.KDOC_EMPTY_KDOC)
    fun `empty KDocs are not allowed - example with empty KDOC_SECTION`() {
        lintMethod(
                """/**
               | *${" ".repeat(5)}
               | */
               |fun foo() = Unit
            """.trimMargin(),
                LintError(1, 1, ruleId, "${KDOC_EMPTY_KDOC.warnText()} foo", false)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_EMPTY_KDOC)
    fun `empty KDocs are not allowed - example with no KDOC_SECTION`() {
        lintMethod(
                """/**
               | */
               |fun foo() = Unit
            """.trimMargin(),
                LintError(1, 1, ruleId, "${KDOC_EMPTY_KDOC.warnText()} foo", false)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_EMPTY_KDOC)
    fun `empty KDocs are not allowed - without bound identifier`() {
        lintMethod(
                """/**
               | *
               | */
            """.trimMargin(),
                LintError(1, 1, ruleId, "${KDOC_EMPTY_KDOC.warnText()} $testFileName", false)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_EMPTY_KDOC)
    fun `empty KDocs are not allowed - with anonymous entity`() {
        lintMethod(
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
    @Tag(WarningNames.KDOC_NO_DEPRECATED_TAG)
    fun `@deprecated tag is not allowed`() {
        val invalidCode = """
            /**
             * @deprecated use foo instead
             */
            fun bar() = Unit
        """.trimIndent()

        lintMethod(invalidCode,
                LintError(2, 4, ruleId, "${KDOC_NO_DEPRECATED_TAG.warnText()} @deprecated use foo instead", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_EMPTY_TAGS)
    fun `no empty descriptions in tag blocks are allowed`() {
        val invalidCode = """
            /**
             * @param a
             * @return
             * @throws IllegalStateException
             */
             $funCode
        """.trimIndent()

        lintMethod(invalidCode,
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

        lintMethod(validCode)
    }

    @Test
    @Tag(WarningNames.KDOC_WRONG_SPACES_AFTER_TAG)
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

        lintMethod(invalidCode,
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
    @Tag(WarningNames.KDOC_WRONG_SPACES_AFTER_TAG)
    fun `check end of the line after tag isn't error`() {
        val invalidCode = """
            /**
             * @implNote
             * implNote text
             *
             * @param a dummy int
             * @param b dummy int
             * @return doubled value
             * @throws IllegalStateException
             */
             $funCode
        """.trimIndent()
        lintMethod(invalidCode)
    }

    @Test
    @Tag(WarningNames.KDOC_WRONG_TAGS_ORDER)
    fun `tags should be ordered in KDocs (positive example)`() {
        val validCode = """
            /**
             * @param a dummy int
             * @return doubled value
             * @throws IllegalStateException
             */
             $funCode
        """.trimIndent()

        lintMethod(validCode)
    }

    @Test
    @Tag(WarningNames.KDOC_WRONG_TAGS_ORDER)
    fun `tags should be ordered in KDocs`() {
        val invalidCode = """
            /**
             * @return doubled value
             * @throws IllegalStateException
             * @param a dummy int
             */
             $funCode
        """.trimIndent()

        lintMethod(invalidCode,
                LintError(2, 16, ruleId,
                        "${KDOC_WRONG_TAGS_ORDER.warnText()} @return, @throws, @param", true))
    }

    @Test
    @Tag(WarningNames.KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS)
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

        lintMethod(invalidCode,
                LintError(2, 16, ruleId,
                        "${KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS.warnText()} @param", true),
                LintError(4, 16, ruleId,
                        "${KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS.warnText()} @return", true))
    }

    @Test
    @Tag(WarningNames.KDOC_NEWLINES_BEFORE_BASIC_TAGS)
    fun `basic tags block should have empty line before if there is other KDoc content (positive example)`() {
        lintMethod(
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
    @Tag(WarningNames.KDOC_NEWLINES_BEFORE_BASIC_TAGS)
    fun `basic tags block shouldn't have empty line before if there is no other KDoc content`() {
        lintMethod(
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
    @Tag(WarningNames.KDOC_NEWLINES_BEFORE_BASIC_TAGS)
    fun `basic tags block should have empty line before if there is other KDoc content`() {
        lintMethod(
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
    @Tag(WarningNames.KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS)
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

        lintMethod(validCode)
    }

    @Test
    @Tag(WarningNames.KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS)
    fun `special tags should have exactly one newline after them (no newline)`() {
        val invalidCode = """
            /**
             * @implSpec stuff
             * @apiNote foo
             * @implNote bar
             */
            $funCode
        """.trimIndent()

        lintMethod(invalidCode,
                LintError(2, 16, ruleId,
                        "${KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS.warnText()} @implSpec, @apiNote, @implNote", true))
    }

    @Test
    @Tag(WarningNames.KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS)
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

        lintMethod(invalidCode,
                LintError(2, 16, ruleId,
                        "${KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS.warnText()} @implSpec, @apiNote, @implNote", true))
    }
}
