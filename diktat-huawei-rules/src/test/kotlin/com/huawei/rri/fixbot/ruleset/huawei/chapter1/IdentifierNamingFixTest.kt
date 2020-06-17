package com.huawei.rri.fixbot.ruleset.huawei.chapter1

import com.huawei.rri.fixbot.ruleset.huawei.rules.IdentifierNaming
import com.huawei.rri.fixbot.ruleset.huawei.utils.format
import config.rules.RulesConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
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

    private fun format(text: String, fileName: String): String = IdentifierNaming().format(text, fileName,
        listOf(
            RulesConfig("PACKAGE_NAME_INCORRECT", false, ""),
            RulesConfig("PACKAGE_NAME_INCORRECT_PREFIX", false, "")
        )
    )
}
