package com.saveourtool.diktat.ruleset.chapter1

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.INCORRECT_PACKAGE_SEPARATOR
import com.saveourtool.diktat.ruleset.constants.Warnings.PACKAGE_NAME_INCORRECT_CASE
import com.saveourtool.diktat.ruleset.constants.Warnings.PACKAGE_NAME_INCORRECT_PATH
import com.saveourtool.diktat.ruleset.constants.Warnings.PACKAGE_NAME_INCORRECT_PREFIX
import com.saveourtool.diktat.ruleset.constants.Warnings.PACKAGE_NAME_INCORRECT_SYMBOLS
import com.saveourtool.diktat.ruleset.constants.Warnings.PACKAGE_NAME_MISSING
import com.saveourtool.diktat.ruleset.rules.chapter1.PackageNaming
import com.saveourtool.diktat.util.LintTestBase
import com.saveourtool.diktat.util.TEST_FILE_NAME

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class PackageNamingWarnTest : LintTestBase(::PackageNaming) {
    private val ruleId: String = "$DIKTAT_RULE_SET_ID:${PackageNaming.NAME_ID}"
    private val rulesConfigList: List<RulesConfig> = listOf(
        RulesConfig("DIKTAT_COMMON", true, mapOf("domainName" to "com.saveourtool.diktat"))
    )
    private val rulesConfigListEmptyDomainName: List<RulesConfig> = listOf(
        RulesConfig("DIKTAT_COMMON", true, mapOf("domainName" to ""))
    )
    private val rulesConfigSourceDirectories: List<RulesConfig> = listOf(
        RulesConfig("DIKTAT_COMMON", true, mapOf(
            "domainName" to "com.saveourtool.diktat",
            "srcDirectories" to "nativeMain, mobileMain",
            "testDirs" to "nativeTest"
        ))
    )

    @Test
    @Tag(WarningNames.PACKAGE_NAME_MISSING)
    fun `missing package name (check)`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                import com.saveourtool.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            tempDir = tempDir,
            fileName = TEST_FILE_NAME,
            DiktatError(1, 1, ruleId, "${PACKAGE_NAME_MISSING.warnText()} $TEST_FILE_NAME", true),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_MISSING)
    fun `missing package name with annotation (check)`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                @file:Suppress("CONSTANT_UPPERCASE")

                import com.saveourtool.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            tempDir = tempDir,
            fileName = TEST_FILE_NAME,
            DiktatError(1, 37, ruleId, "${PACKAGE_NAME_MISSING.warnText()} $TEST_FILE_NAME", true),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_MISSING)
    fun `don't add the package name to the file in buildSrc path`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                import com.saveourtool.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            tempDir = tempDir,
            fileName = "diktat/buildSrc/src/main/kotlin/Version.kt",
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_INCORRECT_CASE)
    fun `package name should be in a lower case (check)`() {
        lintMethod(
            """
                package /* AAA */ com.saveourtool.diktat.SPECIALTEST.test

                import com.saveourtool.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            DiktatError(1, 42, ruleId, "${PACKAGE_NAME_INCORRECT_CASE.warnText()} SPECIALTEST", true),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_INCORRECT_PREFIX)
    fun `package name should start from domain name (check)`() {
        lintMethod(

            """
                package some.incorrect.domain.test

                import com.saveourtool.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            DiktatError(1, 9, ruleId, "${PACKAGE_NAME_INCORRECT_PREFIX.warnText()} com.saveourtool.diktat", true),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.INCORRECT_PACKAGE_SEPARATOR)
    fun `underscore exceptions - incorrect underscore case`() {
        lintMethod(

            """
                package com.saveourtool.diktat.domain.test_

                import com.saveourtool.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            DiktatError(1, 39, ruleId, "${INCORRECT_PACKAGE_SEPARATOR.warnText()} test_", true),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_INCORRECT_SYMBOLS)
    fun `incorrect symbol in package name`() {
        lintMethod(

            """
                package com.saveourtool.diktat.domain.testш

                import com.saveourtool.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            DiktatError(1, 39, ruleId, "${PACKAGE_NAME_INCORRECT_SYMBOLS.warnText()} testш"),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_INCORRECT_SYMBOLS)
    fun `underscore exceptions - positive case - keyword`() {
        lintMethod(

            """
                package com.saveourtool.diktat.domain.int_

                import com.saveourtool.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_INCORRECT_SYMBOLS)
    fun `test source config`(@TempDir tempDir: Path) {
        lintMethodWithFile(

            """
                package com.saveourtool.diktat.domain

                import com.saveourtool.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            tempDir = tempDir,
            fileName = "diktat/diktat-rules/src/nativeMain/kotlin/com/saveourtool/diktat/domain/BlaBla.kt",
            rulesConfigList = rulesConfigSourceDirectories
        )

        lintMethodWithFile(

            """
                package com.saveourtool.diktat.domain

                import com.saveourtool.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            tempDir = tempDir,
            fileName = "diktat/diktat-rules/src/main/kotlin/com/saveourtool/diktat/domain/BlaBla.kt",
            DiktatError(1, 9, ruleId, "${PACKAGE_NAME_INCORRECT_PATH.warnText()} com.saveourtool.diktat.main.kotlin.com.saveourtool.diktat.domain", true),
            rulesConfigList = rulesConfigSourceDirectories
        )
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_INCORRECT_SYMBOLS)
    fun `test directories for test config`(@TempDir tempDir: Path) {
        lintMethodWithFile(

            """
                package com.saveourtool.diktat.domain

                import com.saveourtool.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            tempDir = tempDir,
            fileName = "diktat/diktat-rules/src/nativeTest/kotlin/com/saveourtool/diktat/domain/BlaBla.kt",
            rulesConfigList = rulesConfigSourceDirectories
        )

        lintMethodWithFile(

            """
                package com.saveourtool.diktat.domain

                import com.saveourtool.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            tempDir = tempDir,
            fileName = "diktat/diktat-rules/src/test/kotlin/com/saveourtool/diktat/domain/BlaBla.kt",
            DiktatError(1, 9, ruleId, "${PACKAGE_NAME_INCORRECT_PATH.warnText()} com.saveourtool.diktat.test.kotlin.com.saveourtool.diktat.domain", true),
            rulesConfigList = rulesConfigSourceDirectories
        )
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_INCORRECT_PATH)
    fun `regression - incorrect warning on file under test directory`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                    package com.saveourtool.diktat.ruleset.chapter1
            """.trimIndent(),
            tempDir = tempDir,
            fileName = "diktat/diktat-rules/src/test/kotlin/com/saveourtool/diktat/ruleset/chapter1/EnumValueCaseTest.kt",
            rulesConfigList = rulesConfigList
        )

        lintMethodWithFile(
            """
                    package com.saveourtool.diktat.chapter1
            """.trimIndent(),
            tempDir = tempDir,
            fileName = "diktat/diktat-rules/src/test/kotlin/com/saveourtool/diktat/ruleset/chapter1/EnumValueCaseTest.kt",
            DiktatError(1, 9, ruleId, "${PACKAGE_NAME_INCORRECT_PATH.warnText()} com.saveourtool.diktat.ruleset.chapter1", true),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_INCORRECT_PATH)
    fun `regression - should not remove special words from file path`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                    |package com.saveourtool.diktat.test.processing
            """.trimMargin(),
            tempDir = tempDir,
            fileName = "project/module/src/test/kotlin/com/saveourtool/diktat/test/processing/SpecialPackageNaming.kt",
            rulesConfigList = rulesConfigList
        )

        lintMethodWithFile(
            """
                    |package kotlin.collections
            """.trimMargin(),
            tempDir = tempDir,
            fileName = "project/module/src/main/kotlin/kotlin/collections/Collections.kt",
            rulesConfigList = listOf(
                RulesConfig("DIKTAT_COMMON", true, mapOf("domainName" to "kotlin"))
            )
        )
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_INCORRECT_PATH)
    fun `should respect KMP project structure - positive example`(@TempDir tempDir: Path) {
        listOf("main", "test", "jvmMain", "jvmTest", "androidMain", "androidTest", "iosMain", "iosTest", "jsMain", "jsTest", "commonMain", "commonTest").forEach {
            lintMethodWithFile(
                """
                    |package com.saveourtool.diktat
                """.trimMargin(),
                tempDir = tempDir,
                fileName = "project/src/$it/kotlin/com/saveourtool/diktat/Example.kt",
                rulesConfigList = rulesConfigList
            )
        }
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_INCORRECT_PATH)
    fun `should respect KMP project structure`(@TempDir tempDir: Path) {
        listOf("main", "test", "jvmMain", "jvmTest", "androidMain", "androidTest", "iosMain", "iosTest", "jsMain", "jsTest", "commonMain", "commonTest").forEach {
            lintMethodWithFile(
                """
                    |package com.saveourtool.diktat
                """.trimMargin(),
                tempDir = tempDir,
                fileName = "project/src/$it/kotlin/com/saveourtool/diktat/example/Example.kt",
                DiktatError(1, 9, ruleId, "${PACKAGE_NAME_INCORRECT_PATH.warnText()} com.saveourtool.diktat.example", true),
                rulesConfigList = rulesConfigList
            )
        }
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_INCORRECT_PATH)
    fun `should respect KMP project structure - illegal source set name`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                |package com.saveourtool.diktat
            """.trimMargin(),
            tempDir = tempDir,
            fileName = "project/src/myProjectMain/kotlin/com/saveourtool/diktat/example/Example.kt",
            DiktatError(1, 9, ruleId, "${PACKAGE_NAME_INCORRECT_PATH.warnText()} com.saveourtool.diktat.myProjectMain.kotlin.com.saveourtool.diktat.example", true),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_INCORRECT_PATH)
    fun `should warn if there is empty domain name`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                |package com.saveourtool.diktat
            """.trimMargin(),
            tempDir = tempDir,
            fileName = "project/src/main/kotlin/com/saveourtool/diktat/example/Example.kt",
            DiktatError(1, 9, ruleId, "${PACKAGE_NAME_INCORRECT_PREFIX.warnText()} ", true),
            DiktatError(1, 9, ruleId, "${PACKAGE_NAME_INCORRECT_PATH.warnText()} com.saveourtool.diktat.example", true),
            rulesConfigList = rulesConfigListEmptyDomainName
        )
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_INCORRECT_PATH)
    fun `shouldn't trigger if path contains dot`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                |package com.saveourtool.diktat.test.utils
            """.trimMargin(),
            tempDir = tempDir,
            fileName = "project/src/main/kotlin/com/saveourtool/diktat/test.utils/Example.kt",
        )
    }

    @Test
    @Tag(WarningNames.PACKAGE_NAME_INCORRECT_PATH)
    fun `shouldn't trigger for gradle script`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                |import com.saveourtool.diktat.generation.docs.generateAvailableRules
            """.trimMargin(),
            tempDir = tempDir,
            fileName = "project/build.gradle.kts",
        )
    }
}
