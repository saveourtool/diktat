package com.saveourtool.diktat.ruleset.chapter2

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.constants.Warnings.IF_ELSE_COMMENTS
import com.saveourtool.diktat.ruleset.rules.chapter2.kdoc.CommentsFormatting
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CommentsFormattingTest : LintTestBase(::CommentsFormatting) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${CommentsFormatting.NAME_ID}"

    @Test
    @Tag(WarningNames.COMMENT_WHITE_SPACE)
    fun `check white space before comment good`() {
        val code =
            """
                |package com.saveourtool.diktat.ruleset.chapter3
                |
                |class Example {
                |    // First Comment
                |    private val log = LoggerFactory.getLogger(Example.javaClass)
                |}
            """.trimMargin()

        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.COMMENT_WHITE_SPACE)
    fun `check white space before comment bad 2`() {
        val code =
            """
                |package com.saveourtool.diktat.ruleset.chapter3
                |
                |class Example {
                |    val s = RulesConfig(WRONG_INDENTATION.name, true,
                |            mapOf(
                |                    "newlineAtEnd" to "true",     // comment
                |                    "extendedIndentOfParameters" to "true",
                |                    "alignedParameters" to "true",
                |                    "extendedIndentAfterOperators" to "true"
                |            )
                |    )
                |}
            """.trimMargin()

        lintMethod(code,
            DiktatError(6, 51, ruleId, "${Warnings.COMMENT_WHITE_SPACE.warnText()} There should be 2 space(s) before comment text, but there are too many in // comment", true))
    }

    @Test
    @Tag(WarningNames.COMMENT_WHITE_SPACE)
    fun `check white space before comment bad 3`() {
        val code =
            """
                |package com.saveourtool.diktat.ruleset.chapter3
                |
                |@Suppress("RULE")    // asdasd
                |class Example {
                |
                |}
            """.trimMargin()

        lintMethod(code,
            DiktatError(3, 22, ruleId, "${Warnings.COMMENT_WHITE_SPACE.warnText()} There should be 2 space(s) before comment text, but there are too many in // asdasd", true))
    }

    @Test
    @Tag(WarningNames.COMMENT_WHITE_SPACE)
    fun `check white space before comment good2`() {
        val code =
            """
                |package com.saveourtool.diktat.ruleset.chapter3
                |
                |/* This is a comment */
                |class Example {
                |    /**
                |    *
                |    * Some Comment
                |    */
                |    private val log = LoggerFactory.getLogger(Example.javaClass)
                |
                |    fun a() {
                |       // When comment
                |       when(1) {
                |           1 -> print(1)
                |       }
                |    }
                |    /*
                |       Some Comment
                |    */
                |}
            """.trimMargin()

        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.COMMENT_WHITE_SPACE)
    fun `check comment before package good`() {
        val code =
            """
                |// This is a comment before package
                |package com.saveourtool.diktat.ruleset.chapter3
                |
                |// This is a comment
                |class Example {
                |
                |}
            """.trimMargin()

        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.COMMENT_WHITE_SPACE)
    fun `check white space before comment bad`() {
        val code =
            """
                |package com.saveourtool.diktat.ruleset.chapter3
                |
                |class Example {
                |    //First Comment
                |    private val log = LoggerFactory.getLogger(Example.javaClass)
                |
                |    /**
                |    *      Some comment
                |    */
                |
                |    /*     Comment */
                |}
            """.trimMargin()

        lintMethod(code,
            DiktatError(4, 5, ruleId, "${Warnings.COMMENT_WHITE_SPACE.warnText()} There should be 1 space(s) before comment token in //First Comment", true),
            DiktatError(11, 5, ruleId, "${Warnings.COMMENT_WHITE_SPACE.warnText()} There should be 1 space(s) before comment token in /*     Comment */", true))
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES_AROUND_KDOC)
    fun `check new line above comment good`() {
        val code =
            """
                |package com.saveourtool.diktat.ruleset.chapter3
                |
                |class Example {
                |    private val log = LoggerFactory.getLogger(Example.javaClass)
                |
                |    // Another Comment
                |    private val some = 5
                |
                |    fun someFunc() {
                |       /* First comment */
                |       val first = 5  // Some comment
                |
                |       /**
                |       * kDoc comment
                |       * some text
                |       */
                |       val second = 6
                |
                |       /**
                |       * asdasd
                |       */
                |       fun testFunc() {
                |           val a = 5  // Some Comment
                |
                |           // Fun in fun Block
                |           val b = 6
                |       }
                |    }
                |}
            """.trimMargin()

        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES_AROUND_KDOC)
    fun `check file new line above comment good`() {
        val code =
            """
                |package com.saveourtool.diktat.ruleset.chapter3
                |
                |// Some comment
                |class Example {
                |
                |}
                |
                |// Some comment 2
                |class AnotherExample {
                |
                |}
            """.trimMargin()

        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES_AROUND_KDOC)
    fun `check file new line above comment bad`() {
        val code =
            """
                |package com.saveourtool.diktat.ruleset.chapter3
                |// Some comment
                |class Example {
                |
                |}
                |
                |// Some comment 2
                |class AnotherExample {
                |
                |}
            """.trimMargin()

        lintMethod(code,
            DiktatError(2, 1, ruleId, "${Warnings.WRONG_NEWLINES_AROUND_KDOC.warnText()} // Some comment", true))
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES_AROUND_KDOC)
    fun `check file new line above comment bad - block and kDOC comments`() {
        val code =
            """
                |package com.saveourtool.diktat.ruleset.chapter3
                |/* Some comment */
                |class Example {
                |
                |}
                |/**
                |* Some comment 2
                |*/
                |
                |class AnotherExample {
                |
                |}
            """.trimMargin()

        lintMethod(code,
            DiktatError(2, 1, ruleId, "${Warnings.WRONG_NEWLINES_AROUND_KDOC.warnText()} /* Some comment */", true),
            DiktatError(6, 1, ruleId, "${Warnings.WRONG_NEWLINES_AROUND_KDOC.warnText()} /**...", true),
            DiktatError(8, 3, ruleId, "${Warnings.WRONG_NEWLINES_AROUND_KDOC.warnText()} redundant blank line after /**...", true))
    }

    @Test
    @Tag(WarningNames.COMMENT_WHITE_SPACE)
    fun `check right side comments - good`() {
        val code =
            """
                |package com.saveourtool.diktat.ruleset.chapter3
                |
                |/* Some comment */
                |class Example {
                |   val a = 5  // This is a comment
                |}
            """.trimMargin()

        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.COMMENT_WHITE_SPACE)
    fun `check right side comments - bad`() {
        val code =
            """
                |package com.saveourtool.diktat.ruleset.chapter3
                |
                |/* Some comment */
                |class Example {
                |   val a = 5// This is a comment
                |}
            """.trimMargin()

        lintMethod(code,
            DiktatError(5, 13, ruleId, "${Warnings.COMMENT_WHITE_SPACE.warnText()} There should be 2 space(s) before comment text, but are none in // This is a comment", true))
    }

    @Test
    @Tag(WarningNames.IF_ELSE_COMMENTS)
    fun `if - else comments good`() {
        val code =
            """
                |package com.saveourtool.diktat.ruleset.chapter3
                |
                |class Example {
                |   fun someFunc() {
                |       // general if comment
                |       if(a = 5) {
                |
                |       }
                |       else {
                |           // Good Comment
                |           print(5)
                |       }
                |   }
                |}
            """.trimMargin()

        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.IF_ELSE_COMMENTS)
    fun `if - else comments good 2`() {
        val code =
            """
                |package com.saveourtool.diktat.ruleset.chapter3
                |
                |class Example {
                |   fun someFunc() {
                |       // general if comment
                |       if(a = 5) {
                |
                |       } else
                |           // Good Comment
                |           print(5)
                |   }
                |}
            """.trimMargin()

        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.IF_ELSE_COMMENTS)
    fun `if - else comments bad`() {
        val code =
            """
                |package com.saveourtool.diktat.ruleset.chapter3
                |
                |class Example {
                |   fun someFunc() {
                |       // general if comment
                |       if(a = 5) {
                |
                |       }
                |       // Bad Comment
                |       else {
                |           print(5)
                |       }
                |   }
                |}
            """.trimMargin()

        lintMethod(code,
            DiktatError(6, 8, ruleId, "${IF_ELSE_COMMENTS.warnText()} // Bad Comment", true))
    }

    @Test
    @Tag(WarningNames.IF_ELSE_COMMENTS)
    fun `if - else comments bad 3`() {
        val code =
            """
                |package com.saveourtool.diktat.ruleset.chapter3
                |
                |class Example {
                |   fun someFunc() {
                |       // general if comment
                |       if(a = 5) {
                |
                |       }  /* Some comment */ else {
                |           print(5)
                |       }
                |   }
                |}
            """.trimMargin()

        lintMethod(code,
            DiktatError(6, 8, ruleId, "${IF_ELSE_COMMENTS.warnText()} /* Some comment */", true))
    }

    @Test
    @Tag(WarningNames.IF_ELSE_COMMENTS)
    fun `if - else comments bad 4`() {
        val code =
            """
                |package com.saveourtool.diktat.ruleset.chapter3
                |
                |class Example {
                |   fun someFunc() {
                |       // general if comment
                |       if(a = 5) {
                |
                |       }  /* Some comment */ else
                |           print(5)
                |   }
                |}
            """.trimMargin()

        lintMethod(code,
            DiktatError(6, 8, ruleId, "${IF_ELSE_COMMENTS.warnText()} /* Some comment */", true))
    }

    @Test
    @Tag(WarningNames.IF_ELSE_COMMENTS)
    fun `should not trigger on comment`() {
        val code =
            """
                |package com.saveourtool.diktat.ruleset.chapter3
                |
                |class Example {
                |   fun someFunc() {
                |       // general if comment
                |       if(a = 5) {
                |           /* Some comment */
                |       } else {
                |           print(5)
                |       }
                |   }
                |}
            """.trimMargin()

        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.FIRST_COMMENT_NO_BLANK_LINE)
    fun `first comment no space in if - else bad`() {
        val code =
            """
                |package com.saveourtool.diktat.ruleset.chapter3
                |
                |class Example {
                |   fun someFunc() {
                |       // general if comment
                |       if(a = 5) {
                |
                |       } else {  // Bad Comment
                |           print(5)
                |       }
                |   }
                |}
            """.trimMargin()

        lintMethod(code,
            DiktatError(8, 18, ruleId, "${Warnings.FIRST_COMMENT_NO_BLANK_LINE.warnText()} // Bad Comment", true))
    }

    @Test
    @Tag(WarningNames.COMMENT_WHITE_SPACE)
    fun `check comment in class bad`() {
        val code =
            """
                |package com.saveourtool.diktat.ruleset.chapter3
                |
                |class Example {
                |    // First Comment
                |    private val log = LoggerFactory.getLogger(Example.javaClass)  // secondComment
                |}
            """.trimMargin()

        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.COMMENT_WHITE_SPACE)
    fun `check comment on file level`() {
        val code =
            """
                | /*
                |  * heh
                |  */
                |package com.saveourtool.diktat.ruleset.chapter3
                |
                |class Example {
                |}
            """.trimMargin()

        lintMethod(code,
            DiktatError(1, 2, ruleId, "${Warnings.COMMENT_WHITE_SPACE.warnText()} There should be 0 space(s) before comment text, but are 1 in /*...", true))
    }

    @Test
    @Tag(WarningNames.COMMENT_WHITE_SPACE)
    fun `check comment on file level without space`() {
        val code =
            """
                |/*
                |  * heh
                |  */
                |package com.saveourtool.diktat.ruleset.chapter3
                |
                |class Example {
                |}
            """.trimMargin()

        lintMethod(code)
    }

    /**
     * `indent(1)` and `style(9)` style comments.
     */
    @Test
    @Tag(WarningNames.COMMENT_WHITE_SPACE)
    fun `indent-style header in a block comment should produce no warnings`() =
        lintMethod(indentStyleComment)

    internal companion object {
        @Language("kotlin")
        internal val indentStyleComment = """
        |/*-
        | * This is an indent-style comment, and it's different from regular
        | * block comments in C-like languages.
        | *
        | * Code formatters should not wrap or reflow its content, so you can
        | * safely insert code fragments:
        | *
        | * ```
        | * int i = 42;
        | * ```
        | *
        | * or ASCII diagrams:
        | *
        | * +-----+
        | * | Box |
        | * +-----+
        | */
        """.trimMargin()
    }
}
