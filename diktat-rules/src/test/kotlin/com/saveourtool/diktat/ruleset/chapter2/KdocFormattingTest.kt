package com.saveourtool.diktat.ruleset.chapter2

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_EMPTY_KDOC
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_NEWLINES_BEFORE_BASIC_TAGS
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_NO_DEPRECATED_TAG
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_NO_EMPTY_TAGS
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_WRONG_SPACES_AFTER_TAG
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_WRONG_TAGS_ORDER
import com.saveourtool.diktat.ruleset.rules.chapter2.kdoc.KdocFormatting
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class KdocFormattingTest : LintTestBase(::KdocFormatting) {
    private val ruleId: String = "$DIKTAT_RULE_SET_ID:${KdocFormatting.NAME_ID}"
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
            DiktatError(1, 1, ruleId, "${KDOC_EMPTY_KDOC.warnText()} foo", false)
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
            DiktatError(1, 1, ruleId, "${KDOC_EMPTY_KDOC.warnText()} foo", false)
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
            DiktatError(1, 1, ruleId, "${KDOC_EMPTY_KDOC.warnText()} /**...", false)
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
            DiktatError(2, 5, ruleId, "${KDOC_EMPTY_KDOC.warnText()} object", false)
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
            DiktatError(2, 4, ruleId, "${KDOC_NO_DEPRECATED_TAG.warnText()} @deprecated use foo instead", true)
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
            DiktatError(3, 16, ruleId,
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
            DiktatError(2, 16, ruleId,
                "${KDOC_WRONG_SPACES_AFTER_TAG.warnText()} @param", true),
            DiktatError(3, 16, ruleId,
                "${KDOC_WRONG_SPACES_AFTER_TAG.warnText()} @param", true),
            DiktatError(4, 16, ruleId,
                "${KDOC_WRONG_SPACES_AFTER_TAG.warnText()} @return", true),
            DiktatError(5, 16, ruleId,
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
            DiktatError(2, 16, ruleId,
                "${KDOC_WRONG_TAGS_ORDER.warnText()} @return, @throws, @param", true))
    }

    @Test
    @Tag(WarningNames.KDOC_WRONG_TAGS_ORDER)
    fun `tags should be ordered assertion issue`() {
        val invalidCode = """
            /**
             * Reporter that produces a JSON report as a [Report]
             *
             * @property out a sink for output
             *
             * @param builder additional configuration lambda for serializers module
             */
            class JsonReporter(
                override val out: BufferedSink,
                builder: PolymorphicModuleBuilder<Plugin.TestFiles>.() -> Unit = {}
            ) : Reporter
        """.trimIndent()

        lintMethod(invalidCode,
            DiktatError(4, 4, ruleId,
                "${KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS.warnText()} @property", true),
            DiktatError(4, 4, ruleId,
                "${KDOC_WRONG_TAGS_ORDER.warnText()} @property, @param", true)
        )
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
            DiktatError(2, 16, ruleId,
                "${KDOC_NO_NEWLINES_BETWEEN_BASIC_TAGS.warnText()} @param", true),
            DiktatError(4, 16, ruleId,
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
            DiktatError(3, 4, ruleId, "${KDOC_NEWLINES_BEFORE_BASIC_TAGS.warnText()} @param", true)
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
            DiktatError(4, 4, ruleId, "${KDOC_NEWLINES_BEFORE_BASIC_TAGS.warnText()} @param", true)
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
            DiktatError(2, 16, ruleId,
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
            DiktatError(2, 16, ruleId,
                "${KDOC_NO_NEWLINE_AFTER_SPECIAL_TAGS.warnText()} @implSpec, @apiNote, @implNote", true))
    }

    @Test
    @Tag(WarningNames.KDOC_CONTAINS_DATE_OR_AUTHOR)
    fun `@author tag is not allowed in header comment`() {
        lintMethod(
            """
                |/**
                | * Description of this file
                | * @author anonymous
                | */
                |
                |package com.saveourtool.diktat.example
                |
                |/**
                | * Description of this class
                | * @author anonymous
                | */
                |class Example { }
            """.trimMargin(),
            DiktatError(3, 4, ruleId, "${Warnings.KDOC_CONTAINS_DATE_OR_AUTHOR.warnText()} @author anonymous"),
            DiktatError(10, 4, ruleId, "${Warnings.KDOC_CONTAINS_DATE_OR_AUTHOR.warnText()} @author anonymous"),
        )
    }

    @Test
    @Tag(WarningNames.KDOC_CONTAINS_DATE_OR_AUTHOR)
    fun `@since tag should only contain versions`() {
        lintMethod(
            """
                |/**
                | * Description of this file
                | * @since 2019-10-11
                | * @since 19-10-11
                | * @since 2019.10.11
                | * @since 2019/10/11
                | * @since 11 Oct 2019
                | * @since 1.2.3
                | * @since 1.2.3-1
                | * @since 1.2.3-SNAPSHOT
                | * @since 1.2.3-rc-1
                | * @since 1.2.3.RELEASE
                | */
                |
                |package com.saveourtool.diktat.example
                |
                |/**
                | * Description of this file
                | * @since 2019-10-11
                | * @since 1.2.3
                | */
                |class Example { }
            """.trimMargin(),
            DiktatError(3, 4, ruleId, "${Warnings.KDOC_CONTAINS_DATE_OR_AUTHOR.warnText()} @since 2019-10-11"),
            DiktatError(4, 4, ruleId, "${Warnings.KDOC_CONTAINS_DATE_OR_AUTHOR.warnText()} @since 19-10-11"),
            DiktatError(5, 4, ruleId, "${Warnings.KDOC_CONTAINS_DATE_OR_AUTHOR.warnText()} @since 2019.10.11"),
            DiktatError(6, 4, ruleId, "${Warnings.KDOC_CONTAINS_DATE_OR_AUTHOR.warnText()} @since 2019/10/11"),
            DiktatError(7, 4, ruleId, "${Warnings.KDOC_CONTAINS_DATE_OR_AUTHOR.warnText()} @since 11 Oct 2019"),
            DiktatError(19, 4, ruleId, "${Warnings.KDOC_CONTAINS_DATE_OR_AUTHOR.warnText()} @since 2019-10-11"),
            rulesConfigList = emptyList()
        )
    }

    @Test
    @Tag(WarningNames.KDOC_CONTAINS_DATE_OR_AUTHOR)
    fun `@since tag should only contain versions - with configured regex`() {
        lintMethod(
            """
                |/**
                | * Description of this file
                | * @since 2019-10-11
                | * @since 1.2.3-rc-1
                | * @since 1.2.3.RELEASE
                | */
                |
                |package com.saveourtool.diktat.example
                |
                |/**
                | * Description of this file
                | * @since 2019-10-11
                | * @since 1.2.3
                | */
                |class Example { }
            """.trimMargin(),
            DiktatError(3, 4, ruleId, "${Warnings.KDOC_CONTAINS_DATE_OR_AUTHOR.warnText()} @since 2019-10-11"),
            DiktatError(5, 4, ruleId, "${Warnings.KDOC_CONTAINS_DATE_OR_AUTHOR.warnText()} @since 1.2.3.RELEASE"),
            DiktatError(12, 4, ruleId, "${Warnings.KDOC_CONTAINS_DATE_OR_AUTHOR.warnText()} @since 2019-10-11"),
            rulesConfigList = listOf(
                RulesConfig(
                    Warnings.KDOC_CONTAINS_DATE_OR_AUTHOR.name, true, mapOf(
                        "versionRegex" to "\\d+\\.\\d+\\.\\d+[-\\w\\d]*"
                    )
                )
            )
        )
    }
}
