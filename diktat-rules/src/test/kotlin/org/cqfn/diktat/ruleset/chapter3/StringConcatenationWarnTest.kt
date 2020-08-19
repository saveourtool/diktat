package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.StringConcatenationRule
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class StringConcatenationWarnTest {

    private val ruleId = "$DIKTAT_RULE_SET_ID:string-concatenation"
    private val canBeAutoCorrected = false

    @Test
    @Tag("STRING_CONCATENATION")
    fun `string concatenation - only strings`() {
        lintMethod(StringConcatenationRule(),
                """
                    | val a = "my string" + "string" + value + "other value"
                    |
                """.trimMargin(),
                LintError(1, 10, ruleId, Warnings.STRING_CONCATENATION.warnText() +
                        " \"my string\" + \"string\" + value + \"other value\"", canBeAutoCorrected)
        )
    }

    @Test
    @Tag("STRING_CONCATENATION")
    fun `string concatenation - simple string and integers`() {
        lintMethod(StringConcatenationRule(),
                """
                    | val a = "my string" + 1 + 2 + 3
                    |
                """.trimMargin(),
                LintError(1, 10, ruleId, Warnings.STRING_CONCATENATION.warnText() +
                        " \"my string\" + 1 + 2 + 3", canBeAutoCorrected)
        )
    }

    @Test
    @Tag("STRING_CONCATENATION")
    // FixMe: need to check and think if this codeblock should trigger warning or not
    fun `string concatenation - toString function in string templates`() {
        lintMethod(StringConcatenationRule(),
                """
                    | val a = (1 + 2).toString() + "my string" + 3
                    |
                """.trimMargin()
        )
    }

    @Test
    @Tag("STRING_CONCATENATION")
    fun `string concatenation - toString and variables`() {
        lintMethod(StringConcatenationRule(),
                """
                    | val myObject = 12
                    | val a = (1 + 2).toString() + "my string" + 3 + "string" + myObject + myObject
                    |
                """.trimMargin()
        )
    }

    @Test
    @Tag("STRING_CONCATENATION")
    fun `string concatenation - toString and variables with braces`() {
        lintMethod(StringConcatenationRule(),
                """
                    | val myObject = 12
                    | val a = (1 + 2).toString() + "my string" + ("string" + myObject) + myObject
                    |
                """.trimMargin(),
                LintError(2, 46, ruleId, Warnings.STRING_CONCATENATION.warnText() +
                        " (1 + 2).toString() + \"my string\" + (\"string\" + myObject) + myObject", canBeAutoCorrected)
        )
    }

    @Test
    @Tag("STRING_CONCATENATION")
    fun `string concatenation - function argument`() {
        lintMethod(StringConcatenationRule(),
                """
                    | fun foo1(){
                    |     foo("my string" + "other string" + (1 + 2 + 3))
                    | }
                """.trimMargin(),
                LintError(2, 10, ruleId, Warnings.STRING_CONCATENATION.warnText() +
                        " \"my string\" + \"other string\" + (1 + 2 + 3)", canBeAutoCorrected)
        )
    }

    @Test
    @Tag("STRING_CONCATENATION")
    fun `string concatenation - string and braces`() {
        lintMethod(StringConcatenationRule(),
                """
                    | val myObject = 12
                    | val a = "my string" + "other string" + (1 + 2 + 3)
                    |
                """.trimMargin(),
                LintError(2, 10, ruleId, Warnings.STRING_CONCATENATION.warnText() +
                        " \"my string\" + \"other string\" + (1 + 2 + 3)", canBeAutoCorrected)
        )
    }

    @Test
    @Tag("STRING_CONCATENATION")
    fun `string concatenation - several braces`() {
        lintMethod(StringConcatenationRule(),
                """
                    | val myObject = 12
                    | val a = "my string" + (1 + 2 + 3) + ("other string" + 3) + (1 + 2 + 3)
                    |
                """.trimMargin(),
                LintError(2, 10, ruleId, Warnings.STRING_CONCATENATION.warnText() +
                        " \"my string\" + (1 + 2 + 3) + (\"other string\" + 3) + (1 + 2 + 3)", canBeAutoCorrected)
        )
    }

    @Test
    @Tag("STRING_CONCATENATION")
    fun `string concatenation - multiple braces`() {
        lintMethod(StringConcatenationRule(),
                """
                    | val a = "my string" + (1 + 2 + 3) + ("other string" + 3) + (1 + (2 + 3)) + ("third string" + ("str" + 5))
                    |
                """.trimMargin(),
                LintError(1, 10, ruleId, Warnings.STRING_CONCATENATION.warnText() +
                        " \"my string\" + (1 + 2 + 3) + (\"other string\" + 3) + (1 + (2 + 3)) +" +
                        " (\"third string\" + (\"str\" + 5))", canBeAutoCorrected)
        )
    }

    @Test
    @Tag("STRING_CONCATENATION")
    fun `string concatenation - other binary operators`() {
        lintMethod(StringConcatenationRule(),
                """
                    | val a = "my string" + ("third string" + ("str" + 5 * 12 / 100)) 
                    |
                """.trimMargin(),
                LintError(1, 10, ruleId, Warnings.STRING_CONCATENATION.warnText() +
                       " \"my string\" + (\"third string\" + (\"str\" + 5 * 12 / 100))", canBeAutoCorrected)
        )
    }

    @Test
    @Tag("STRING_CONCATENATION")
    fun `string concatenation - three lines `() {
        lintMethod(StringConcatenationRule(),
                """
                    | val a = "my string" +
                    |  "string" + value +
                    |  other + value
                    |
                """.trimMargin()
        )
    }

    @Test
    @Tag("STRING_CONCATENATION")
    fun `string concatenation - two lines `() {
        lintMethod(StringConcatenationRule(),
                """
                    | val a = "my string" +
                    |  "string" + value
                    |
                """.trimMargin()
        )
    }
}
