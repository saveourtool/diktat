package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter3.StringTemplateFormatRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames.STRING_TEMPLATE_CURLY_BRACES
import generated.WarningNames.STRING_TEMPLATE_QUOTES
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class StringTemplateRuleWarnTest : LintTestBase(::StringTemplateFormatRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${StringTemplateFormatRule.NAME_ID}"

    @Test
    @Tag(STRING_TEMPLATE_CURLY_BRACES)
    fun `long string template good example`() {
        lintMethod(
            """
                    |class Some {
                    |   val template = "${'$'}{::String} ${'$'}{asd.moo()}"
                    |   val some = "${'$'}{foo as Foo}"
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(STRING_TEMPLATE_CURLY_BRACES)
    fun `long string template bad example`() {
        lintMethod(
            """
                    |class Some {
                    |   val template = "${'$'}{a} ${'$'}{asd.moo()}"
                    |   val some = "${'$'}{1.0}"
                    |   val another = "${'$'}{1}"
                    |   val singleLetterCase = "${'$'}{ref}"
                    |   val digitsWithLetters = "${'$'}{1.0}asd"
                    |}
            """.trimMargin(),
            DiktatError(2, 20, ruleId, "${Warnings.STRING_TEMPLATE_CURLY_BRACES.warnText()} ${'$'}{a}", true),
            DiktatError(3, 16, ruleId, "${Warnings.STRING_TEMPLATE_CURLY_BRACES.warnText()} ${'$'}{1.0}", true),
            DiktatError(4, 19, ruleId, "${Warnings.STRING_TEMPLATE_CURLY_BRACES.warnText()} ${'$'}{1}", true),
            DiktatError(5, 28, ruleId, "${Warnings.STRING_TEMPLATE_CURLY_BRACES.warnText()} ${'$'}{ref}", true),
            DiktatError(6, 29, ruleId, "${Warnings.STRING_TEMPLATE_CURLY_BRACES.warnText()} ${'$'}{1.0}", true)
        )
    }

    @Test
    @Tag(STRING_TEMPLATE_QUOTES)
    fun `short string template bad example`() {
        lintMethod(
            """
                    |class Some {
                    |   val template = "${'$'}a"
                    |   val z = a
                    |}
            """.trimMargin(),
            DiktatError(2, 20, ruleId, "${Warnings.STRING_TEMPLATE_QUOTES.warnText()} ${'$'}a", true)
        )
    }

    @Test
    @Tag(STRING_TEMPLATE_CURLY_BRACES)
    fun `should trigger on dot after braces`() {
        lintMethod(
            """
                    |class Some {
                    |   fun some() {
                    |       val s = "abs"
                    |       println("${'$'}{s}.length is ${'$'}{s.length}")
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(4, 17, ruleId, "${Warnings.STRING_TEMPLATE_CURLY_BRACES.warnText()} ${'$'}{s}", true)
        )
    }

    @Test
    @Tag(STRING_TEMPLATE_QUOTES)
    fun `should not trigger`() {
        lintMethod(
            """
                    |class Some {
                    |   fun some() {
                    |       val price = ""${'"'}
                    |       ${'$'}9.99
                    |       ""${'"'}
                    |       val some = "${'$'}{index + 1}"
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(STRING_TEMPLATE_CURLY_BRACES)
    fun `underscore after braces - braces should not be removed`() {
        lintMethod(
            """
                    |class Some {
                    |   fun some() {
                    |       val copyTestFile = File("${'$'}{testFile()} copy ${'$'}{testFile}_copy")
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(STRING_TEMPLATE_CURLY_BRACES)
    fun `should not trigger on array access`() {
        lintMethod(
            """
                    |class Some {
                    |   fun some() {
                    |       val copyTestFile = "${'$'}{arr[0]}"
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(STRING_TEMPLATE_QUOTES)
    fun `should trigger on long string template`() {
        lintMethod(
            """
                    |class Some {
                    |   fun some() {
                    |       val x = "asd"
                    |       val trippleQuotes = ""${'"'}${'$'}x""${'"'}
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(4, 31, ruleId, "${Warnings.STRING_TEMPLATE_QUOTES.warnText()} ${'$'}x", true)
        )
    }
}
