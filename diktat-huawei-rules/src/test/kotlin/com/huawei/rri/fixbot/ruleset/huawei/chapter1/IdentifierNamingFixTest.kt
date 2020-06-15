package com.huawei.rri.fixbot.ruleset.huawei.chapter1

import com.huawei.rri.fixbot.ruleset.huawei.rules.IdentifierNaming
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.huawei.rri.fixbot.ruleset.huawei.rules.PackageNaming
import config.rules.RulesConfig
import test_framework.processing.TestComparatorUnit

class IdentifierNamingFixTest {
    @Test
    fun `incorrect class name (fix)`() {
        assertThat(
            TestComparatorUnit("test/paragraph1/naming/class_", ::format)
                .compareFilesFromResources("IncorrectClassNameExpected.kt", "IncorrectClassNameTest.kt")
        ).isEqualTo(true)
    }

    @Test
    fun `incorrect object name (fix)`() {
        assertThat(
            TestComparatorUnit("test/paragraph1/naming/object_", ::format)
                .compareFilesFromResources("IncorrectObjectNameExpected.kt", "IncorrectObjectNameTest.kt")
        ).isEqualTo(true)
    }

    @Test
    fun `incorrect enum values case (fix)`() {
        assertThat(
            TestComparatorUnit("test/paragraph1/naming/enum_", ::format)
                .compareFilesFromResources("EnumValueCaseExpected.kt", "EnumValueCaseTest.kt")
        ).isEqualTo(true)

    }

    private fun format(text: String, fileName: String): String = IdentifierNaming().format(text, fileName)

    private fun Rule.format(text: String, fileName: String): String {
        return KtLint.format(
            KtLint.Params(
                text = text,
                ruleSets = listOf(RuleSet("huawei-codestyle", this@format)),
                fileName = fileName,
                rulesConfigList = listOf(
                    RulesConfig("PACKAGE_NAME_INCORRECT", false, ""),
                    RulesConfig("PACKAGE_NAME_INCORRECT_PREFIX", false, "")
                ),
                cb = { _, _ -> }
            )
        )
    }
}
