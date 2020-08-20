package org.cqfn.diktat.ruleset.chapter3.spaces

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_WHITESPACE
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.WhiteSpaceRule
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class WhiteSpaceRuleWarnTest {
    private val ruleId = "$DIKTAT_RULE_SET_ID:horizontal-whitespace"
    private fun keywordWarn(keyword: String, sep: String) =
            "${WRONG_WHITESPACE.warnText()} keyword '$keyword' should be separated from '$sep' with a whitespace"

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `keywords should have space before opening parenthesis and braces - positive example`() {
        lintMethod(WhiteSpaceRule(),
                """
                    |class Example : SuperExample {
                    |    constructor(val a: Int)
                    |
                    |    fun foo() {
                    |         if (condition) { }
                    |         else { }
                    |         for (i in 1..100) { }
                    |         when (expression) { }
                    |    }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `keywords should have space before opening parenthesis`() {
        lintMethod(WhiteSpaceRule(),
                """
                    |class Example {
                    |    fun foo() {
                    |         if(condition) { }
                    |         for  (i in 1..100) { }
                    |         when(expression) { }
                    |    }
                    |}
                """.trimMargin(),
                LintError(3, 10, ruleId, keywordWarn("if", "("), true),
                LintError(4, 10, ruleId, keywordWarn("for", "("), true),
                LintError(5, 10, ruleId, keywordWarn("when", "("), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `constructor should not have space before opening parenthesis`() {
        lintMethod(WhiteSpaceRule(),
                """
                    |class Example {
                    |    constructor (val a: Int)
                    |}
                """.trimMargin(),
                LintError(2, 5, ruleId, "${WRONG_WHITESPACE.warnText()} keyword 'constructor' should not be separated from '(' with a whitespace", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `keywords should have space before opening braces`() {
        lintMethod(WhiteSpaceRule(),
                """
                    |class Example {
                    |    fun foo() {
                    |         if (condition) { }
                    |         else{}
                    |         try{ }
                    |         finally{ }
                    |    }
                    |}
                """.trimMargin(),
                LintError(4, 10, ruleId, keywordWarn("else", "{"), true),
                LintError(5, 10, ruleId, keywordWarn("try", "{"), true),
                LintError(6, 10, ruleId, keywordWarn("finally", "{"), true)
        )
    }
}
