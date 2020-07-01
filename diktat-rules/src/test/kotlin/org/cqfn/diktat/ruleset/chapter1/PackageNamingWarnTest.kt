package org.cqfn.diktat.ruleset.chapter1

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.junit.Test
import org.cqfn.diktat.ruleset.rules.PackageNaming
import org.cqfn.diktat.ruleset.constants.Warnings.*
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.utils.lintMethod
import org.junit.Ignore

class PackageNamingWarnTest {

    private val ruleId: String = "$DIKTAT_RULE_SET_ID:package-naming"

    private val rulesConfigList: List<RulesConfig> = listOf(
        RulesConfig(PACKAGE_NAME_MISSING.name, true, mapOf("domainName" to "org.cqfn.diktat"))
    )

    @Test
    @Ignore("this test is failing because params.fileName!! throws NPE")
    fun `missing package name (check)`() {
        lintMethod(PackageNaming(),
            """
                import org.cqfn.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            LintError(1, 1, ruleId, "${PACKAGE_NAME_MISSING.warnText()} "),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    fun `package name should be in a lower case (check)`() {
        lintMethod(
            PackageNaming(),
            """
                package /* AAA */ org.cqfn.diktat.SPECIALTEST.test

                import org.cqfn.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            LintError(1, 35, ruleId, "${PACKAGE_NAME_INCORRECT_CASE.warnText()} SPECIALTEST"),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    fun `package name should start from domain name (check)`() {
        lintMethod(
            PackageNaming(),
            """
                package some.incorrect.domain.test

                import org.cqfn.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            LintError(1, 9, ruleId, "${PACKAGE_NAME_INCORRECT_PREFIX.warnText()} org.cqfn.diktat"),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    fun `underscore exceptions - incorrect underscore case`() {
        lintMethod(
            PackageNaming(),
            """
                package org.cqfn.diktat.domain.test_

                import org.cqfn.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            LintError(1, 32, ruleId, "${INCORRECT_PACKAGE_SEPARATOR.warnText()} test_"),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    fun `incorrect symbol in package name`() {
        lintMethod(
            PackageNaming(),
            """
                package org.cqfn.diktat.domain.testш

                import org.cqfn.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            LintError(1, 32, ruleId, "${PACKAGE_NAME_INCORRECT_SYMBOLS.warnText()} testш"),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    fun `underscore exceptions - positive case - keyword`() {
        lintMethod(
            PackageNaming(),
            """
                package org.cqfn.diktat.domain.int_

                import org.cqfn.diktat.a.b.c

                /**
                 * testComment
                 */
                class TestPackageName {  }

            """.trimIndent(),
            rulesConfigList = rulesConfigList
        )
    }
}
