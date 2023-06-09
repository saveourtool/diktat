package com.saveourtool.diktat.ruleset.chapter6

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter6.ExtensionFunctionsInFileRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames.EXTENSION_FUNCTION_WITH_CLASS
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class ExtensionFunctionsInFileWarnTest : LintTestBase(::ExtensionFunctionsInFileRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${ExtensionFunctionsInFileRule.NAME_ID}"

    @Test
    @Tag(EXTENSION_FUNCTION_WITH_CLASS)
    fun `should warn on function`() {
        lintMethod(
            """
                |class Some private constructor () {
                |
                |}
                |
                |private fun Some.coolStr() {
                |
                |}
            """.trimMargin(),
            DiktatError(5, 1, ruleId, "${Warnings.EXTENSION_FUNCTION_WITH_CLASS.warnText()} fun coolStr")
        )
    }

    @Test
    @Tag(EXTENSION_FUNCTION_WITH_CLASS)
    fun `should warn on several functions`() {
        lintMethod(
            """
                |class Another private constructor () {
                |
                |}
                |
                |private fun /* Random comment */ Another.coolStr() {
                |
                |}
                |
                |private fun Another.extMethod() {
                |
                |}
            """.trimMargin(),
            DiktatError(5, 1, ruleId, "${Warnings.EXTENSION_FUNCTION_WITH_CLASS.warnText()} fun coolStr"),
            DiktatError(9, 1, ruleId, "${Warnings.EXTENSION_FUNCTION_WITH_CLASS.warnText()} fun extMethod"),
        )
    }

    @Test
    @Tag(EXTENSION_FUNCTION_WITH_CLASS)
    fun `should not raise a warning when there is no class`() {
        lintMethod(
            """
                |private fun String.coolStr() {
                |
                |}
                |
                |private fun /* Random comment */ Another.extMethod() {
                |
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(EXTENSION_FUNCTION_WITH_CLASS)
    fun `should not raise a warning when there is no extension functions`() {
        lintMethod(
            """
                |class Some {
                |
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(EXTENSION_FUNCTION_WITH_CLASS)
    fun `should not raise a warning when extension function is in the class`() {
        lintMethod(
            """
                |class Some {
                |
                |   fun Some.str() {
                |
                |   }
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(EXTENSION_FUNCTION_WITH_CLASS)
    fun `should not trigger on regular functions in the same file with class`() {
        lintMethod(
            """
                |class Some {
                |   fun foo() {
                |
                |   }
                |}
                |
                |fun bar() {
                |
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(EXTENSION_FUNCTION_WITH_CLASS)
    fun `should not trigger on extension functions with different class`() {
        lintMethod(
            """
                |class Some {
                |   fun foo() {
                |
                |   }
                |}
                |
                |fun String.bar() {
                |
                |}
            """.trimMargin()
        )
    }

    /**
     * See [#1586](https://github.com/saveourtool/diktat/issues/1586).
     *
     * @since 1.2.5
     */
    @Test
    @Tag(EXTENSION_FUNCTION_WITH_CLASS)
    fun `should raise no warnings for external interfaces`() {
        lintMethod(
            """
                |internal external interface External {
                |    val a: Int
                |}
                |
                |fun External.extension() = println(a)
            """.trimMargin()
        )
    }
}
