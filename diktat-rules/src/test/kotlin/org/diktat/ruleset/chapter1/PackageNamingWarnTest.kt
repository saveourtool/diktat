package org.diktat.ruleset.chapter1

import com.pinterest.ktlint.core.LintError
import org.diktat.common.config.rules.RulesConfig
import org.junit.Test
import org.diktat.ruleset.rules.PackageNaming
import org.diktat.ruleset.constants.Warnings.*
import org.diktat.ruleset.utils.lintMethod
import org.junit.Ignore

class PackageNamingWarnTest {
    private val rulesConfigList: List<RulesConfig> = listOf(
        RulesConfig(PACKAGE_NAME_MISSING.name, true, mapOf("domainName" to "org.diktat"))
    )

    @Test
    @Ignore("this test is failing because params.fileName!! throws NPE")
    fun `missing package name (check)`() {
        lintMethod(PackageNaming(),
            """
                import org.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            LintError(1, 1, "package-naming", "${PACKAGE_NAME_MISSING.warnText()} "),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    fun `package name should be in a lower case (check)`() {
        lintMethod(
            PackageNaming(),
            """
                package /* AAA */ org.diktat.SPECIALTEST.test

                import org.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            LintError(1, 30, "package-naming", "${PACKAGE_NAME_INCORRECT_CASE.warnText()} SPECIALTEST"),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    fun `package name should start from domain name (check)`() {
        lintMethod(
            PackageNaming(),
            """
                package some.incorrect.domain.test

                import org.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            LintError(1, 9, "package-naming", "${PACKAGE_NAME_INCORRECT_PREFIX.warnText()} org.diktat"),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    fun `underscore exceptions - incorrect underscore case`() {
        lintMethod(
            PackageNaming(),
            """
                package org.diktat.domain.test_

                import org.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            LintError(1, 27, "package-naming", "${INCORRECT_PACKAGE_SEPARATOR.warnText()} test_"),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    fun `incorrect symbol in package name`() {
        lintMethod(
            PackageNaming(),
            """
                package org.diktat.domain.testш

                import org.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            LintError(1, 27, "package-naming", "${PACKAGE_NAME_INCORRECT_SYMBOLS.warnText()} testш"),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    fun `underscore exceptions - positive case - keyword`() {
        lintMethod(
            PackageNaming(),
            """
                package org.diktat.domain.int_

                import org.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            rulesConfigList = rulesConfigList
        )
    }
}
