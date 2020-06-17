package com.huawei.rri.fixbot.ruleset.huawei.chapter1

import com.huawei.rri.fixbot.ruleset.huawei.rules.PackageNaming
import com.huawei.rri.fixbot.ruleset.huawei.utils.format
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import test_framework.processing.TestComparatorUnit

class PackagePathFixTest {
    private val testComparatorUnitWithDomain = TestComparatorUnit("test/paragraph1/naming/package/src/main/kotlin/com/huawei/some/name", ::format)
    private val testComparatorUnit = TestComparatorUnit("test/paragraph1/naming/package/src/main/kotlin/some", ::format)
    private fun format(text: String, fileName: String): String = PackageNaming().format(text, fileName)

    @Test
    fun `fixing package name that differs from a path (fix)`() {
        assertThat(
            testComparatorUnitWithDomain
                .compareFilesFromResources("FixIncorrectExpected.kt", "FixIncorrectTest.kt")
        ).isEqualTo(true)
    }

    @Test
    fun `fix missing package name with a proper location (fix)`() {
        assertThat(
            testComparatorUnitWithDomain
                .compareFilesFromResources("FixMissingExpected.kt", "FixMissingTest.kt")
        ).isEqualTo(true)
    }

    @Test
    fun `fixing package name that differs from a path without domain (fix)`() {
        assertThat(
            testComparatorUnit
                .compareFilesFromResources("FixIncorrectExpected.kt", "FixIncorrectTest.kt")
        ).isEqualTo(true)
    }

    @Test
    fun `fix missing package name with a proper location without domain (fix)`() {
        assertThat(
            testComparatorUnit
                .compareFilesFromResources("FixMissingExpected.kt", "FixMissingTest.kt")
        ).isEqualTo(true)
    }
}
