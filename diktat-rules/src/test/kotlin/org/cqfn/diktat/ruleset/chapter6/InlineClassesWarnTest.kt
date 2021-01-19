package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.ruleset.constants.Warnings.INLINE_CLASS_CAN_BE_USED
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.classes.InlineClassesRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class InlineClassesWarnTest : LintTestBase(::InlineClassesRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:inline-classes"

    @Test
    @Tag(WarningNames.INLINE_CLASS_CAN_BE_USED)
    fun `should not trigger on inline class`() {
        lintMethod(
            """
                |inline class Name(val s: String) {}
            """.trimMargin()
        )
    }


    @Test
    @Tag(WarningNames.INLINE_CLASS_CAN_BE_USED)
    fun `should trigger on regular class`() {
        lintMethod(
            """
                |class Some {
                |   val config = Config()
                |}
            """.trimMargin(),
            LintError(1, 1, ruleId, "${INLINE_CLASS_CAN_BE_USED.warnText()} class Some", true)
        )
    }

    @Test
    @Tag(WarningNames.INLINE_CLASS_CAN_BE_USED)
    fun `should trigger on class with appropriate modifiers`() {
        lintMethod(
            """
                |final class Some {
                |   val config = Config()
                |}
            """.trimMargin(),
            LintError(1, 1, ruleId, "${INLINE_CLASS_CAN_BE_USED.warnText()} class Some", true)
        )
    }

    @Test
    @Tag(WarningNames.INLINE_CLASS_CAN_BE_USED)
    fun `should not trigger on class with inappropriate modifiers`() {
        lintMethod(
            """
                |abstract class Some {
                |   val config = Config()
                |}
            """.trimMargin()
        )
    }
}
