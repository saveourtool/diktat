package com.saveourtool.diktat.ruleset.chapter3.files

import com.saveourtool.diktat.api.DiktatError
import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings.REDUNDANT_SEMICOLON
import com.saveourtool.diktat.ruleset.rules.chapter3.files.SemicolonsRule
import com.saveourtool.diktat.util.LintTestBase
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class SemicolonsRuleWarnTest : LintTestBase(::SemicolonsRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${SemicolonsRule.NAME_ID}"

    @Test
    @Tag(WarningNames.REDUNDANT_SEMICOLON)
    fun `should forbid EOL semicolons`() {
        lintMethod(
            """
                    |enum class Example {
                    |    A,
                    |    B
                    |    ;
                    |
                    |    fun foo() {};
                    |    val a = 0;
                    |    val b = if (condition) { bar(); baz()} else qux
                    |};
            """.trimMargin(),
            DiktatError(6, 17, ruleId, "${REDUNDANT_SEMICOLON.warnText()} fun foo() {};", true),
            DiktatError(7, 14, ruleId, "${REDUNDANT_SEMICOLON.warnText()} val a = 0;", true),
            DiktatError(9, 2, ruleId, "${REDUNDANT_SEMICOLON.warnText()} };", true)
        )
    }
}
