package org.cqfn.diktat.ruleset.chapter1

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.PackageNaming
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Test

class PackagePathFixTest : FixTestBase(
    "test/paragraph1/naming/package/src/main/kotlin",
    PackageNaming(),
    listOf(RulesConfig(Warnings.PACKAGE_NAME_MISSING.name, true, mapOf("domainName" to "org.cqfn.diktat")))
) {

    @Test
    fun `fixing package name that differs from a path (fix)`() {
        fixAndCompare("org/cqfn/diktat/some/name/FixIncorrectExpected.kt", "org/cqfn/diktat/some/name/FixIncorrectTest.kt")
    }

    @Test
    fun `fix missing package name with a proper location (fix)`() {
        fixAndCompare("org/cqfn/diktat/some/name/FixMissingExpected.kt", "org/cqfn/diktat/some/name/FixMissingTest.kt")
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
