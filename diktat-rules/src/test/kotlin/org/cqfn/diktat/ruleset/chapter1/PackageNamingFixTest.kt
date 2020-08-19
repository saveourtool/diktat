package org.cqfn.diktat.ruleset.chapter1

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.PACKAGE_NAME_MISSING
import org.cqfn.diktat.ruleset.rules.PackageNaming
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Test

class PackageNamingFixTest : FixTestBase(
    "test/paragraph1/naming/package",
    PackageNaming(),
    listOf(RulesConfig(PACKAGE_NAME_MISSING.name, true, mapOf("domainName" to "org.cqfn.diktat")))
) {
    @Test
    fun `incorrect case of package name fix`() {
        fixAndCompare("FixUpperExpected.kt", "FixUpperTest.kt")
    }

    @Test
    fun `fixing incorrect domain name fix`() {
        fixAndCompare("MissingDomainNameExpected.kt", "MissingDomainNameTest.kt")
    }

    @Test
    fun `incorrect usage of package separator fix`() {
        fixAndCompare("FixUnderscoreExpected.kt", "FixUnderscoreTest.kt")
    }
}
