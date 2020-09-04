package org.cqfn.diktat.ruleset.chapter2

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import generated.WarningNames.COMMENT_NEW_LINE_ABOVE
import generated.WarningNames.SPACE_BETWEEN_COMMENT_AND_CODE
import generated.WarningNames.WHITESPACE_IN_COMMENT
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.constants.Warnings.IF_ELSE_COMMENTS
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.kdoc.CommentsFormatting
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CommentsFormattingTest {

    private val ruleId = "$DIKTAT_RULE_SET_ID:kdoc-comments-codeblocks-formatting"

    @Test
    @Tag(WHITESPACE_IN_COMMENT)
    fun `check white space before comment good` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |class Example {
                    |    // First Comment
                    |    private val log = LoggerFactory.getLogger(Example.javaClass)
                    |}
                """.trimMargin()

        lintMethod(CommentsFormatting(), code)
    }

    @Test
    @Tag(WHITESPACE_IN_COMMENT)
    fun `check white space before comment bad` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |class Example {
                    |    //First Comment
                    |    private val log = LoggerFactory.getLogger(Example.javaClass)
                    |}
                """.trimMargin()

        lintMethod(CommentsFormatting(), code,
                LintError(4,5, ruleId, "${Warnings.WHITESPACE_IN_COMMENT.warnText()} //First Comment", true))
    }

    @Test
    @Tag(COMMENT_NEW_LINE_ABOVE)
    fun `check new line above comment good` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |class Example {
                    |    private val log = LoggerFactory.getLogger(Example.javaClass)
                    |    
                    |    // Another Comment
                    |    private val some = 5
                    |    
                    |    fun someFunc() {
                    |       /* First comment */
                    |       val first = 5 // Some comment
                    |       
                    |       /**
                    |       * kDoc comment
                    |       */
                    |       val second = 6
                    |       
                    |       /**
                    |       * asdasd
                    |       */
                    |       fun testFunc() {
                    |       
                    |       }
                    |    }
                    |}
                """.trimMargin()

        lintMethod(CommentsFormatting(), code)
    }

    @Test
    @Tag(COMMENT_NEW_LINE_ABOVE)
    fun `check file new line above comment good` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
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

        lintMethod(CommentsFormatting(), code)
    }

    @Test
    @Tag(COMMENT_NEW_LINE_ABOVE)
    fun `check file new line above comment bad` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
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

        lintMethod(CommentsFormatting(), code,
                LintError(2,1,ruleId, "${Warnings.COMMENT_NEW_LINE_ABOVE.warnText()} // Some comment", true))
    }

    @Test
    @Tag(COMMENT_NEW_LINE_ABOVE)
    fun `check file new line above comment bad - block and kDOC comments` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |/* Some comment */
                    |class Example {
                    |
                    |}
                    |/**
                    |* Some comment 2
                    |*/
                    |class AnotherExample {
                    |
                    |}
                """.trimMargin()

        lintMethod(CommentsFormatting(), code,
                LintError(2,1,ruleId, "${Warnings.COMMENT_NEW_LINE_ABOVE.warnText()} /* Some comment */", true),
                LintError(6,1,ruleId, "${Warnings.COMMENT_NEW_LINE_ABOVE.warnText()} /**\n" +
                        "* Some comment 2\n" +
                        "*/", true))
    }

    @Test
    @Tag(SPACE_BETWEEN_COMMENT_AND_CODE)
    fun `check right side comments - good` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |/* Some comment */
                    |class Example {
                    |   val a = 5 // This is a comment
                    |}
                """.trimMargin()

        lintMethod(CommentsFormatting(), code)
    }

    @Test
    @Tag(SPACE_BETWEEN_COMMENT_AND_CODE)
    fun `check right side comments - bad` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |/* Some comment */
                    |class Example {
                    |   val a = 5// This is a comment
                    |}
                """.trimMargin()

        lintMethod(CommentsFormatting(), code,
                LintError(5,13, ruleId, "${Warnings.SPACE_BETWEEN_COMMENT_AND_CODE.warnText()} // This is a comment", true))
    }

    @Test
    @Tag(WarningNames.IF_ELSE_COMMENTS)
    fun `if - else comments good` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
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

        lintMethod(CommentsFormatting(), code)
    }

    @Test
    @Tag(WarningNames.IF_ELSE_COMMENTS)
    fun `if - else comments bad` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
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

        lintMethod(CommentsFormatting(), code,
                LintError(6,8,ruleId, "${IF_ELSE_COMMENTS.warnText()} // Bad Comment", true))
    }
}
