package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter3.StringConcatenationRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class StringConcatenationWarnTest : LintTestBase(::StringConcatenationRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${StringConcatenationRule.NAME_ID}"
    private val canBeAutoCorrected = true

    @Test
    @Tag(WarningNames.STRING_CONCATENATION)
    fun `string concatenation - only strings`() {
        lintMethod(
            """
                    | val a = "my string" + "string" + value + "other value"
                    |
            """.trimMargin(),
            DiktatError(1, 10, ruleId, Warnings.STRING_CONCATENATION.warnText() +
                    " \"my string\" + \"string\" + value + \"other value\"", canBeAutoCorrected)
        )
    }

    @Test
    @Tag(WarningNames.STRING_CONCATENATION)
    fun `string concatenation - simple string and integers`() {
        lintMethod(
            """
                    | val a = "my string" + 1 + 2 + 3
                    |
            """.trimMargin(),
            DiktatError(1, 10, ruleId, Warnings.STRING_CONCATENATION.warnText() +
                    " \"my string\" + 1 + 2 + 3", canBeAutoCorrected)
        )
    }

    @Test
    @Tag(WarningNames.STRING_CONCATENATION)
    // FixMe: need to check and think if this codeblock should trigger warning or not
    fun `string concatenation - toString function in string templates`() {
        lintMethod(
            """
                    | val a = (1 + 2).toString() + "my string" + 3
                    |
            """.trimMargin(),
            DiktatError(1, 10, ruleId, Warnings.STRING_CONCATENATION.warnText() +
                    " (1 + 2).toString() + \"my string\" + 3", canBeAutoCorrected)
        )
    }

    @Test
    @Tag(WarningNames.STRING_CONCATENATION)
    fun `string concatenation - toString and variables`() {
        lintMethod(
            """
                    | val myObject = 12
                    | val a = (1 + 2).toString() + "my string" + 3 + "string" + myObject + myObject
                    |
            """.trimMargin(),
            DiktatError(2, 10, ruleId, Warnings.STRING_CONCATENATION.warnText() +
                    " (1 + 2).toString() + \"my string\" + 3 + \"string\" + myObject + myObject", canBeAutoCorrected)
        )
    }

    @Test
    @Tag(WarningNames.STRING_CONCATENATION)
    fun `string concatenation - toString and variables with braces`() {
        lintMethod(
            """
                    | val myObject = 12
                    | val a = (1 + 2).toString() + "my string" + ("string" + myObject) + myObject
                    |
            """.trimMargin(),
            DiktatError(2, 10, ruleId, Warnings.STRING_CONCATENATION.warnText() +
                    " (1 + 2).toString() + \"my string\" + (\"string\" + myObject) + myObject", canBeAutoCorrected)
        )
    }

    @Test
    @Tag(WarningNames.STRING_CONCATENATION)
    fun `string concatenation - function argument`() {
        lintMethod(
            """
                    | fun foo1(){
                    |     foo("my string" + "other string" + (1 + 2 + 3))
                    | }
            """.trimMargin(),
            DiktatError(2, 10, ruleId, Warnings.STRING_CONCATENATION.warnText() +
                    " \"my string\" + \"other string\" + (1 + 2 + 3)", canBeAutoCorrected)
        )
    }

    @Test
    @Tag(WarningNames.STRING_CONCATENATION)
    fun `string concatenation - string and braces`() {
        lintMethod(
            """
                    | val myObject = 12
                    | val a = "my string" + "other string" + (1 + 2 + 3)
                    |
            """.trimMargin(),
            DiktatError(2, 10, ruleId, Warnings.STRING_CONCATENATION.warnText() +
                    " \"my string\" + \"other string\" + (1 + 2 + 3)", canBeAutoCorrected)
        )
    }

    @Test
    @Tag(WarningNames.STRING_CONCATENATION)
    fun `string concatenation - several braces`() {
        lintMethod(
            """
                    | val myObject = 12
                    | val a = "my string" + (1 + 2 + 3) + ("other string" + 3) + (1 + 2 + 3)
                    |
            """.trimMargin(),
            DiktatError(2, 10, ruleId, Warnings.STRING_CONCATENATION.warnText() +
                    " \"my string\" + (1 + 2 + 3) + (\"other string\" + 3) + (1 + 2 + 3)", canBeAutoCorrected)
        )
    }

    @Test
    @Tag(WarningNames.STRING_CONCATENATION)
    fun `string concatenation - multiple braces`() {
        lintMethod(
            """
                    | val a = "my string" + (1 + 2 + 3) + ("other string" + 3) + (1 + (2 + 3)) + ("third string" + ("str" + 5))
                    |
            """.trimMargin(),
            DiktatError(1, 10, ruleId, Warnings.STRING_CONCATENATION.warnText() +
                    " \"my string\" + (1 + 2 + 3) + (\"other string\" + 3) + (1 + (2 + 3)) +" +
                    " (\"third string\" + (\"str\" + 5))", canBeAutoCorrected)
        )
    }

    @Test
    @Tag(WarningNames.STRING_CONCATENATION)
    fun `string concatenation - other binary operators`() {
        lintMethod(
            """
                    | val a = "my string" + ("third string" + ("str" + 5 * 12 / 100))
                    |
            """.trimMargin(),
            DiktatError(1, 10, ruleId, Warnings.STRING_CONCATENATION.warnText() +
                    " \"my string\" + (\"third string\" + (\"str\" + 5 * 12 / 100))", canBeAutoCorrected)
        )
    }

    @Test
    @Tag(WarningNames.STRING_CONCATENATION)
    fun `string concatenation - three lines `() {
        lintMethod(
            """
                    | val a = "my string" +
                    |  "string" + value +
                    |  other + value
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.STRING_CONCATENATION)
    fun `string concatenation - two lines `() {
        lintMethod(
            """
                    | val a = "my string" +
                    |  "string" + value
                    |
            """.trimMargin()
        )
    }
}
