package com.huawei.rri.fixbot.ruleset.huawei.chapter2

import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings
import com.huawei.rri.fixbot.ruleset.huawei.rules.KdocComments
import com.huawei.rri.fixbot.ruleset.huawei.rules.KdocFormatting
import com.huawei.rri.fixbot.ruleset.huawei.rules.PackageNaming
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.test.lint
import org.assertj.core.api.Assertions
import org.junit.Test
import test_framework.processing.TestComparatorUnit

class KdocFormattingFixTest {

    val testComparatorUnit = TestComparatorUnit("test/paragraph2/kdoc/", ::format)


    @Test
    fun `there should be no blank line between kdoc and it's declaration code`() {
        Assertions.assertThat(
            testComparatorUnit
                .compareFilesFromResources("KdocEmptyLineExpected.kt", "KdocEmptyLineTest.kt")
        ).isEqualTo(true)
    }

    private fun format(text: String, fileName: String): String = KdocFormatting().format(text, fileName)

    private fun Rule.format(text: String, fileName: String): String {
        return KtLint.format(
            KtLint.Params(
                text = text,
                ruleSets = listOf(RuleSet("huawei-codestyle", this@format)),
                fileName = fileName,
                cb = { _, _ -> }
            )
        )
    }

}
