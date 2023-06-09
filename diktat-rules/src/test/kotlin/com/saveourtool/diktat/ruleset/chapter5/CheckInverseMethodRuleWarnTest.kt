package com.saveourtool.diktat.ruleset.chapter5

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter5.CheckInverseMethodRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames.INVERSE_FUNCTION_PREFERRED
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CheckInverseMethodRuleWarnTest : LintTestBase(::CheckInverseMethodRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${CheckInverseMethodRule.NAME_ID}"

    @Test
    @Tag(INVERSE_FUNCTION_PREFERRED)
    fun `should not raise warning`() {
        lintMethod(
            """
                    |fun some() {
                    |   if (list.isEmpty()) {
                    |       // some cool logic
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(INVERSE_FUNCTION_PREFERRED)
    fun `should raise warning`() {
        lintMethod(
            """
                    |fun some() {
                    |   if (!list.isEmpty()) {
                    |       // some cool logic
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(2, 14, ruleId, "${Warnings.INVERSE_FUNCTION_PREFERRED.warnText()} isNotEmpty() instead of !isEmpty()", true)
        )
    }

    @Test
    @Tag(INVERSE_FUNCTION_PREFERRED)
    fun `should consider white spaces between ! and call expression`() {
        lintMethod(
            """
                    |fun some() {
                    |   if (!  list.isEmpty()) {
                    |       // some cool logic
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(2, 16, ruleId, "${Warnings.INVERSE_FUNCTION_PREFERRED.warnText()} isNotEmpty() instead of !isEmpty()", true)
        )
    }

    @Test
    @Tag(INVERSE_FUNCTION_PREFERRED)
    fun `should consider comments between ! and call expression`() {
        lintMethod(
            """
                    |fun some() {
                    |   if (! /*cool comment*/ list.isEmpty()) {
                    |       // some cool logic
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(2, 32, ruleId, "${Warnings.INVERSE_FUNCTION_PREFERRED.warnText()} isNotEmpty() instead of !isEmpty()", true)
        )
    }
}
