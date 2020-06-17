package com.huawei.rri.fixbot.ruleset.huawei.chapter2

import com.huawei.rri.fixbot.ruleset.huawei.rules.KdocFormatting
import com.huawei.rri.fixbot.ruleset.huawei.utils.format
import org.assertj.core.api.Assertions
import org.junit.Assert
import org.junit.Test
import test_framework.processing.TestComparatorUnit

class KdocFormattingFixTest {

    private val testComparatorUnit = TestComparatorUnit("test/paragraph2/kdoc/") { text, fileName ->
        KdocFormatting().format(text, fileName)
    }

    @Test
    fun `there should be no blank line between kdoc and it's declaration code`() {
        Assertions.assertThat(
            testComparatorUnit
                .compareFilesFromResources("KdocEmptyLineExpected.kt", "KdocEmptyLineTest.kt")
        ).isEqualTo(true)
    }

    @Test
    fun `there should be exactly one white space after tag name`() {
        Assert.assertTrue(
            testComparatorUnit
                .compareFilesFromResources("SpacesAfterTagExpected.kt", "SpacesAfterTagTest.kt")
        )
    }

    @Test
    fun `tags should be ordered in KDocs`() {
        Assert.assertTrue(
            testComparatorUnit
                .compareFilesFromResources("OrderedTagsExpected.kt", "OrderedTagsTest.kt")
        )
    }

    @Test
    fun `special tags should have newline after them`() {
        Assert.assertTrue(
            testComparatorUnit
                .compareFilesFromResources("SpecialTagsInKdocExpected.kt", "SpecialTagsInKdocTest.kt")
        )
    }
}
