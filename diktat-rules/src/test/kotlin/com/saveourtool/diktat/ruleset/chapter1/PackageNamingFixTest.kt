package com.saveourtool.diktat.ruleset.chapter1

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.rules.chapter1.PackageNaming
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class PackageNamingFixTest : FixTestBase(
    "test/paragraph1/naming/package",
    ::PackageNaming,
    listOf(RulesConfig("DIKTAT_COMMON", true, mapOf("domainName" to "com.saveourtool.diktat")))
) {
    @Test
    @Tag(WarningNames.PACKAGE_NAME_INCORRECT_CASE)
    fun `incorrect case of package name (fix)`() {
        fixAndCompare("FixUpperExpected.kt", "FixUpperTest.kt")
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_INCORRECT_PATH)
    fun `fixing incorrect domain name (fix)`() {
        fixAndCompare("MissingDomainNameExpected.kt", "MissingDomainNameTest.kt")
    }

    @Test
    @Tag(WarningNames.INCORRECT_PACKAGE_SEPARATOR)
    fun `incorrect usage of package separator (fix)`() {
        fixAndCompare("FixUnderscoreExpected.kt", "FixUnderscoreTest.kt")
    }
}
