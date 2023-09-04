package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.api.DiktatError
import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter3.PreviewAnnotationRule
import com.saveourtool.diktat.util.LintTestBase
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test


class PreviewAnnotationWarnTest : LintTestBase(::PreviewAnnotationRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${PreviewAnnotationRule.NAME_ID}"

    @Test
    @Tag(WarningNames.PREVIEW_ANNOTATION)
    fun `no warn`() {
        lintMethod(
            """
            |@Preview
            |@Composable
            |private fun BannerPreview() {}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.PREVIEW_ANNOTATION)
    fun `method is not private`() {
        lintMethod(
            """
            |@Preview
            |@Composable
            |fun BannerPreview() {}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${Warnings.PREVIEW_ANNOTATION.warnText()} BannerPreview method should be private", true),
        )
    }

    @Test
    @Tag(WarningNames.PREVIEW_ANNOTATION)
    fun `method has no preview suffix`() {
        lintMethod(
            """
            |@Preview
            |@Composable
            |private fun Banner() {}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${Warnings.PREVIEW_ANNOTATION.warnText()} Banner method should has `Preview` suffix", true),
        )
    }

    @Test
    @Tag(WarningNames.PREVIEW_ANNOTATION)
    fun `method has no preview suffix and is not private`() {
        lintMethod(
            """
            |@Preview
            |@Composable
            |fun Banner() {}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${Warnings.PREVIEW_ANNOTATION.warnText()} Banner method should be private", true),
            DiktatError(1, 1, ruleId, "${Warnings.PREVIEW_ANNOTATION.warnText()} Banner method should has `Preview` suffix", true),
        )
    }
}
