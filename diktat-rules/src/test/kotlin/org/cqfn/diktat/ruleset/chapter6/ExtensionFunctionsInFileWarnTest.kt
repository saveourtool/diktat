package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter6.ExtensionFunctionsInFileRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames.EXTENSION_FUNCTION_WITH_CLASS
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class ExtensionFunctionsInFileWarnTest : LintTestBase(::ExtensionFunctionsInFileRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:extension-functions-class-file"

    @Test
    @Tag(EXTENSION_FUNCTION_WITH_CLASS)
    fun `should warn on function`() {
        lintMethod(
            """
                |class Some1 private constructor () {
                |
                |}
                |
                |private fun String.coolStr() {
                |
                |}
            """.trimMargin(),
            LintError(5, 1, ruleId, "${Warnings.EXTENSION_FUNCTION_WITH_CLASS.warnText()} fun coolStr")
        )
    }

    @Test
    @Tag(EXTENSION_FUNCTION_WITH_CLASS)
    fun `should warn on several functions`() {
        lintMethod(
            """
                |class Some1 private constructor () {
                |
                |}
                |
                |private fun /* Random comment */ String.coolStr() {
                |
                |}
                |
                |private fun Another.extMethod() {
                |
                |}
            """.trimMargin(),
            LintError(5, 1, ruleId, "${Warnings.EXTENSION_FUNCTION_WITH_CLASS.warnText()} fun coolStr"),
            LintError(9, 1, ruleId, "${Warnings.EXTENSION_FUNCTION_WITH_CLASS.warnText()} fun extMethod"),
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
    fun `should raise a warning when extension function is in the class`() {
        lintMethod(
            """
                |class Some {
                |   
                |   fun String.str() {
                |   
                |   }
                |}
            """.trimMargin(),
            LintError(3, 4, ruleId, "${Warnings.EXTENSION_FUNCTION_WITH_CLASS.warnText()} fun str")
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
}
