package org.cqfn.diktat.ruleset.chapter2

import com.pinterest.ktlint.core.LintError
import generated.WarningNames.WHITESPACE_IN_COMMENT
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.kdoc.KdocCodeBlocksFormatting
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class KdocCodeBlocksFormattingTest {

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

        lintMethod(KdocCodeBlocksFormatting(), code)
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

        lintMethod(KdocCodeBlocksFormatting(), code,
                LintError(4,5, ruleId, "${Warnings.WHITESPACE_IN_COMMENT.warnText()} //First Comment", true))
    }

    @Test
    @Tag(WHITESPACE_IN_COMMENT)
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
                    |       // Second comment
                    |       val first = 5
                    |       
                    |       // First comment
                    |       val second = 6
                    |    }
                    |}
                """.trimMargin()

        lintMethod(KdocCodeBlocksFormatting(), code)
    }
}