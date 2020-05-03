package rri.fixbot.ruleset.huawei.huawei

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import rri.fixbot.ruleset.huawei.PackageNaming1s3r
import test_framework.processing.TestComparatorUnit

class PackageNaming1s3rFixTest {
    val testComparatorUnit = TestComparatorUnit("test/paragraph1/rule3", ::format)

    @Test
    fun `missing package name (check)`() {
        assertThat(
            testComparatorUnit
                .compareFilesFromResources("a.kt", "b.kt")
        ).isEqualTo(true)
    }

    private fun format(text: String): String = PackageNaming1s3r().format(text)

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
