package com.huawei.rri.fixbot.ruleset.huawei.chapter1

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.huawei.rri.fixbot.ruleset.huawei.rules.PackageNaming
import test_framework.processing.TestComparatorUnit

class PackagePathFixTest {
    val testComparatorUnit = TestComparatorUnit("test/paragraph1/naming/package/src/main/kotlin/some/name", ::format)


    @Test
    fun `fixing package name that differs from a path (fix)`() {
        assertThat(
            testComparatorUnit
                .compareFilesFromResources("FixIncorrectExpected.kt", "FixIncorrectTest.kt")
        ).isEqualTo(true)
    }

    @Test
    fun `fix missing package name with a proper location (fix)`() {
        assertThat(
            testComparatorUnit
                .compareFilesFromResources("FixMissingExpected.kt", "FixMissingTest.kt")
        ).isEqualTo(true)
    }


    private fun format(text: String, fileName: String): String = PackageNaming().format(text, fileName)

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
