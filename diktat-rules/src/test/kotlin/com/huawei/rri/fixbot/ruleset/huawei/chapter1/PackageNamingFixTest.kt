package com.huawei.rri.fixbot.ruleset.huawei.chapter1

import com.huawei.rri.fixbot.ruleset.huawei.rules.PackageNaming
import com.huawei.rri.fixbot.ruleset.huawei.utils.FixTestBase
import org.junit.Test

class PackageNamingFixTest : FixTestBase("test/paragraph1/naming/package", PackageNaming()) {
    @Test
    fun `incorrect case of package name (fix)`() {
        fixAndCompare("FixUpperExpected.kt", "FixUpperTest.kt")
    }

    @Test
    fun `fixing incorrect domain name (fix)`() {
        fixAndCompare("MissingDomainNameExpected.kt", "MissingDomainNameTest.kt")
    }

    @Test
    fun `incorrect usage of package separator (fix)`() {
        fixAndCompare("FixUnderscoreExpected.kt", "FixUnderscoreTest.kt")
    }
}
