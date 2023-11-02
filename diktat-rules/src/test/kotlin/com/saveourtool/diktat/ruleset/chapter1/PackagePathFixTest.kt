package com.saveourtool.diktat.ruleset.chapter1

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.rules.chapter1.PackageNaming
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class PackagePathFixTest : FixTestBase(
    "test/paragraph1/naming/package/src/main/kotlin",
    ::PackageNaming,
    listOf(RulesConfig("DIKTAT_COMMON", true, mapOf("domainName" to "com.saveourtool.diktat")))
) {
    @Test
    @Tag(WarningNames.PACKAGE_NAME_INCORRECT_PATH)
    fun `fixing package name that differs from a path`() {
        fixAndCompare("com/saveourtool/diktat/some/name/FixIncorrectExpected.kt", "com/saveourtool/diktat/some/name/FixIncorrectTest.kt")
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_INCORRECT_PATH)
    fun `fixing package name that differs from a path - regression one-word package name`() {
        fixAndCompare("com/saveourtool/diktat/some/name/FixPackageRegressionExpected.kt", "com/saveourtool/diktat/some/name/FixPackageRegressionTest.kt")
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_INCORRECT_PATH)
    fun `fixing package name that differs from a path without domain`() {
        fixAndCompare("some/FixIncorrectExpected.kt", "some/FixIncorrectTest.kt")
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_MISSING)
    fun `fix missing package name with file annotation`() {
        fixAndCompare("com/saveourtool/diktat/some/name/FixMissingWithAnnotationExpected.kt", "com/saveourtool/diktat/some/name/FixMissingWithAnnotationTest.kt")
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_MISSING)
    fun `fix missing package name with file annotation and comments`() {
        fixAndCompare("com/saveourtool/diktat/some/name/FixMissingWithAnnotationExpected2.kt", "com/saveourtool/diktat/some/name/FixMissingWithAnnotationTest2.kt")
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_MISSING)
    fun `fix missing package name with file annotation and comments 2`() {
        fixAndCompare("com/saveourtool/diktat/some/name/FixMissingWithAnnotationExpected3.kt", "com/saveourtool/diktat/some/name/FixMissingWithAnnotationTest3.kt")
    }

    // If there is no import list in code, the node is still present in the AST, but without any whitespaces around
    // So, this check covered case, when we manually add whitespace before package directive
    @Test
    @Tag(WarningNames.PACKAGE_NAME_MISSING)
    fun `fix missing package name without import list`() {
        fixAndCompare("com/saveourtool/diktat/some/name/FixMissingWithoutImportExpected.kt", "com/saveourtool/diktat/some/name/FixMissingWithoutImportTest.kt")
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_MISSING)
    fun `fix missing package name with a proper location without domain`() {
        fixAndCompare("some/FixMissingExpected.kt", "some/FixMissingTest.kt")
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_MISSING)
    fun `fix missing package name with a proper location`() {
        fixAndCompare("com/saveourtool/diktat/some/name/FixMissingExpected.kt", "com/saveourtool/diktat/some/name/FixMissingTest.kt")
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_MISSING)
    fun `several empty lines after package`(@TempDir tempDir: Path) {
        fixAndCompareContent(
            expectedContent = """
                package com.saveourtool.diktat
                /**
                 * @param bar
                 * @return something
                 */
                fun foo1(bar: Bar): Baz {
                    // placeholder
                }
            """.trimIndent(),
            actualContent = """
                /**
                 * @param bar
                 * @return something
                 */
                fun foo1(bar: Bar): Baz {
                    // placeholder
                }
            """.trimIndent(),
            subFolder = "src/main/kotlin/com/saveourtool/diktat",
            tempDir = tempDir,
        ).assertSuccessful()
    }
}
