package com.saveourtool.diktat.ruleset.chapter2

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_WITHOUT_PARAM_TAG
import com.saveourtool.diktat.ruleset.constants.Warnings.MISSING_KDOC_ON_FUNCTION
import com.saveourtool.diktat.ruleset.rules.chapter2.kdoc.KdocMethods
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test

class KdocParamPresentWarnTest : LintTestBase(::KdocMethods) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${KdocMethods.NAME_ID}"

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_PARAM_TAG)
    fun `check simple correct example`() {
        lintMethod(
            """
                    |/**
                    |* @param a - leftOffset
                    |*/
                    |fun foo(a: Int) {}
            """.trimMargin()
        )
    }

    @Test
    @Tags(Tag(WarningNames.KDOC_WITHOUT_PARAM_TAG), Tag(WarningNames.KDOC_WITHOUT_PARAM_TAG))
    fun `check wrong example with russian letter name`() {
        lintMethod(
            """
                    |/**
                    |* @param A - leftOffset
                    |* @param В - russian letter
                    |*/
                    |fun foo(a: Int, B: Int) {}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${KDOC_WITHOUT_PARAM_TAG.warnText()} foo (a, B)", true),
            DiktatError(2, 3, ruleId, "${KDOC_WITHOUT_PARAM_TAG.warnText()} A param isn't present in argument list"),
            DiktatError(3, 3, ruleId, "${KDOC_WITHOUT_PARAM_TAG.warnText()} В param isn't present in argument list")
        )
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_PARAM_TAG)
    fun `check wrong example without param in fun`() {
        lintMethod(
            """
                    |/**
                    |* @param A - leftOffset
                    |*/
                    |fun foo() {}
            """.trimMargin(),
            DiktatError(2, 3, ruleId, "${KDOC_WITHOUT_PARAM_TAG.warnText()} A param isn't present in argument list")
        )
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_PARAM_TAG)
    fun `check empty param`() {
        lintMethod(
            """
                    |/**
                    |* @param
                    |*/
                    |fun foo() {}
                    |
                    |/**
                    |* @param
                    |*/
                    |fun foo (a: Int) {}
            """.trimMargin(),
            DiktatError(6, 1, ruleId, "${KDOC_WITHOUT_PARAM_TAG.warnText()} foo (a)", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_PARAM_TAG)
    fun `check different order`() {
        lintMethod(
            """
                    |/**
                    |* @param a - qwe
                    |* @param b - qwe
                    |*/
                    |fun foo(b: Int, a: Int) {}
            """.trimMargin()
        )
    }

    @Test
    @Tags(Tag(WarningNames.KDOC_WITHOUT_PARAM_TAG), Tag(WarningNames.MISSING_KDOC_ON_FUNCTION))
    fun `check without kdoc and fun `() {
        lintMethod(
            """
                    |fun foo(b: Int, a: Int) {}
                    |
                    |/**
                    |* @param a - qwe
                    |*/
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${MISSING_KDOC_ON_FUNCTION.warnText()} foo", true)
        )
    }
}
