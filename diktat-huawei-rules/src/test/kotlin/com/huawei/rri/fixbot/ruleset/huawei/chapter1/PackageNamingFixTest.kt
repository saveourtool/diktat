package com.huawei.rri.fixbot.ruleset.huawei.chapter1

import com.huawei.rri.fixbot.ruleset.huawei.rules.PackageNaming
import com.huawei.rri.fixbot.ruleset.huawei.utils.format
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import test_framework.processing.TestComparatorUnit

class PackageNamingFixTest {
    private val testComparatorUnit = TestComparatorUnit("test/paragraph1/naming/package") { text, fileName ->
        PackageNaming().format(text, fileName)
    }

    @Test
    fun `incorrect case of package name (fix)`() {
        assertThat(
            testComparatorUnit
                .compareFilesFromResources("FixUpperExpected.kt", "FixUpperTest.kt")
        ).isEqualTo(true)
    }

    @Test
    fun `fixing incorrect domain name (fix)`() {
        assertThat(
            testComparatorUnit
                .compareFilesFromResources("MissingDomainNameExpected.kt", "MissingDomainNameTest.kt")
        ).isEqualTo(true)
    }

    @Test
    fun `incorrect usage of package separator (fix)`() {
        assertThat(
            testComparatorUnit
                .compareFilesFromResources("FixUnderscoreExpected.kt", "FixUnderscoreTest.kt")
        ).isEqualTo(true)
    }
}
