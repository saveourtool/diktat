package com.huawei.rri.fixbot.ruleset.huawei.chapter1

import com.huawei.rri.fixbot.ruleset.huawei.rules.PackageNaming
import com.huawei.rri.fixbot.ruleset.huawei.utils.FixTestBase
import org.junit.Test

class PackagePathFixTest : FixTestBase("test/paragraph1/naming/package/src/main/kotlin", PackageNaming()) {

    @Test
    fun `fixing package name that differs from a path (fix)`() {
        fixAndCompare("com/huawei/some/name/FixIncorrectExpected.kt", "com/huawei/some/name/FixIncorrectTest.kt")
    }

    @Test
    fun `fix missing package name with a proper location (fix)`() {
        fixAndCompare("com/huawei/some/name/FixMissingExpected.kt", "com/huawei/some/name/FixMissingTest.kt")
    }

    @Test
    fun `fixing package name that differs from a path without domain (fix)`() {
        fixAndCompare("some/FixIncorrectExpected.kt", "some/FixIncorrectTest.kt")
    }

    @Test
    fun `fix missing package name with a proper location without domain (fix)`() {
        fixAndCompare("some/FixMissingExpected.kt", "some/FixMissingTest.kt")
    }
}
