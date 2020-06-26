package org.diktat.ruleset.chapter1

import org.diktat.common.config.rules.RulesConfig
import org.diktat.ruleset.constants.Warnings.PACKAGE_NAME_MISSING
import org.diktat.ruleset.rules.PackageNaming
import org.diktat.ruleset.utils.FixTestBase
import org.junit.Test

class PackageNamingFixTest : FixTestBase(
    "test/paragraph1/naming/package",
    PackageNaming(),
    listOf(RulesConfig(PACKAGE_NAME_MISSING.name, true, mapOf("domainName" to "org.diktat")))
) {
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
