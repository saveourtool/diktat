package org.diktat.ruleset.chapter1

import org.diktat.common.config.rules.RulesConfig
import org.diktat.ruleset.constants.Warnings
import org.diktat.ruleset.rules.PackageNaming
import org.diktat.ruleset.utils.FixTestBase
import org.junit.Test

class PackagePathFixTest : FixTestBase(
    "test/paragraph1/naming/package/src/main/kotlin",
    PackageNaming(),
    listOf(RulesConfig(Warnings.PACKAGE_NAME_MISSING.name, true, mapOf("domainName" to "org.diktat")))
) {

    @Test
    fun `fixing package name that differs from a path (fix)`() {
        fixAndCompare("org/diktat/some/name/FixIncorrectExpected.kt", "org/diktat/some/name/FixIncorrectTest.kt")
    }

    @Test
    fun `fix missing package name with a proper location (fix)`() {
        fixAndCompare("org/diktat/some/name/FixMissingExpected.kt", "org/diktat/some/name/FixMissingTest.kt")
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
