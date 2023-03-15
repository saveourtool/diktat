package org.cqfn.diktat.ruleset.chapter1

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.rules.chapter1.PackageNaming
import org.cqfn.diktat.util.FixTestBase

import org.cqfn.diktat.ruleset.constants.WarningsNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class PackagePathFixTest : FixTestBase(
    "test/paragraph1/naming/package/src/main/kotlin",
    ::PackageNaming,
    listOf(RulesConfig("DIKTAT_COMMON", true, mapOf("domainName" to "org.cqfn.diktat")))
) {
    @Test
    @Tag(WarningNames.PACKAGE_NAME_INCORRECT_PATH)
    fun `fixing package name that differs from a path`() {
        fixAndCompare("org/cqfn/diktat/some/name/FixIncorrectExpected.kt", "org/cqfn/diktat/some/name/FixIncorrectTest.kt")
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_INCORRECT_PATH)
    fun `fixing package name that differs from a path - regression one-word package name`() {
        fixAndCompare("org/cqfn/diktat/some/name/FixPackageRegressionExpected.kt", "org/cqfn/diktat/some/name/FixPackageRegressionTest.kt")
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_INCORRECT_PATH)
    fun `fixing package name that differs from a path without domain`() {
        fixAndCompare("some/FixIncorrectExpected.kt", "some/FixIncorrectTest.kt")
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_MISSING)
    fun `fix missing package name with file annotation`() {
        fixAndCompare("org/cqfn/diktat/some/name/FixMissingWithAnnotationExpected.kt", "org/cqfn/diktat/some/name/FixMissingWithAnnotationTest.kt")
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_MISSING)
    fun `fix missing package name with file annotation and comments`() {
        fixAndCompare("org/cqfn/diktat/some/name/FixMissingWithAnnotationExpected2.kt", "org/cqfn/diktat/some/name/FixMissingWithAnnotationTest2.kt")
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_MISSING)
    fun `fix missing package name with file annotation and comments 2`() {
        fixAndCompare("org/cqfn/diktat/some/name/FixMissingWithAnnotationExpected3.kt", "org/cqfn/diktat/some/name/FixMissingWithAnnotationTest3.kt")
    }

    // If there is no import list in code, the node is still present in the AST, but without any whitespaces around
    // So, this check covered case, when we manually add whitespace before package directive
    @Test
    @Tag(WarningNames.PACKAGE_NAME_MISSING)
    fun `fix missing package name without import list`() {
        fixAndCompare("org/cqfn/diktat/some/name/FixMissingWithoutImportExpected.kt", "org/cqfn/diktat/some/name/FixMissingWithoutImportTest.kt")
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_MISSING)
    fun `fix missing package name with a proper location without domain`() {
        fixAndCompare("some/FixMissingExpected.kt", "some/FixMissingTest.kt")
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_MISSING)
    fun `fix missing package name with a proper location`() {
        fixAndCompare("org/cqfn/diktat/some/name/FixMissingExpected.kt", "org/cqfn/diktat/some/name/FixMissingTest.kt")
    }
}
