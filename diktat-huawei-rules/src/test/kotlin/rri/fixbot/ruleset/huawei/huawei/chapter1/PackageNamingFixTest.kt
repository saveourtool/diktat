package rri.fixbot.ruleset.huawei.huawei.chapter1

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import rri.fixbot.ruleset.huawei.rules.PackageNaming
import test_framework.processing.TestComparatorUnit

class PackageNamingFixTest {
    val testComparatorUnit = TestComparatorUnit("test/paragraph1/naming/package", ::format)

    @Test
    fun `incorrect case of package name (fix)`() {
        assertThat(
            testComparatorUnit
                .compareFilesFromResources("FixUpperExpected.kt", "FixUpperTest.kt")
        ).isEqualTo(true)
    }

    @Test
    fun `missing company name (fix)`() {
        assertThat(
            testComparatorUnit
                .compareFilesFromResources("FixUpperExpected.kt", "FixUpperTest.kt")
        ).isEqualTo(true)
    }

    private fun format(text: String): String = PackageNaming().format(text)

    private fun Rule.format(text: String): String {
        return KtLint.format(
            KtLint.Params(
                text = text,
                ruleSets = listOf(RuleSet("huawei-codestyle", this@format)),
                cb = { _, _ -> }
            )
        )
    }
}
