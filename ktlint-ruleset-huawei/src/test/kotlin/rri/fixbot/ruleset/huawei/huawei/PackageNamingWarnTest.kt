package rri.fixbot.ruleset.huawei.huawei

import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import rri.fixbot.ruleset.huawei.PackageNaming
import rri.fixbot.ruleset.huawei.constants.Warnings.*

class PackageNamingWarnTest {
    @Test
    fun `missing package name (check)`() {
        assertThat(
            PackageNaming().lint(
                """
                    import com.huawei.a.b.c

                    /**
                     * testComment
                     */
                    class TestPackageName {  }

                """.trimIndent()
            )
        ).containsExactly(LintError(
            1,1, "package-naming", PACKAGE_NAME_MISSING.text)
        )
    }

    @Test
    fun `package name should be in a lower case`() {
        assertThat(
            PackageNaming().lint(
                """
                    package /* AAA */ com.huawei.SPECIALTEST.test

                    import com.huawei.a.b.c

                    /**
                     * testComment
                     */
                    class TestPackageName {  }

                """.trimIndent()
            )
        ).containsExactly(LintError(1, 30, "package-naming", "${PACKAGE_NAME_INCORRECT_CASE.text} SPECIALTEST"))
    }

    @Test
    fun `package name should start from domain name`() {
        assertThat(
            PackageNaming().lint(
                """
                    package some.incorrect.domain.test

                    import com.huawei.a.b.c

                    /**
                     * testComment
                     */
                    class TestPackageName {  }

                """.trimIndent()
            )
        ).containsExactly(LintError(1, 9, "package-naming", "${PACKAGE_NAME_INCORRECT_PREFIX.text} com.huawei"))
    }

    @Test
    fun `underscore exceptions - incorrect underscore case`() {
        assertThat(
            PackageNaming().lint(
                """
                    package com.huawei.domain.test_

                    import com.huawei.a.b.c

                    /**
                     * testComment
                     */
                    class TestPackageName {  }

                """.trimIndent()
            )
        ).containsExactly(LintError(1, 27, "package-naming", "${PACKAGE_NAME_INCORRECT_SYMBOLS.text} test_"))
    }

    @Test
    fun `underscore exceptions - incorrect symbol`() {
        assertThat(
            PackageNaming().lint(
                """
                    package com.huawei.domain.testш

                    import com.huawei.a.b.c

                    /**
                     * testComment
                     */
                    class TestPackageName {  }

                """.trimIndent()
            )
        ).containsExactly(LintError(1, 27, "package-naming", "${PACKAGE_NAME_INCORRECT_SYMBOLS.text} testш"))
    }

    @Test
    fun `underscore exceptions - positive case - keyword`() {
        assertThat(
            PackageNaming().lint(
                """
                    package com.huawei.domain.int_

                    import com.huawei.a.b.c

                    /**
                     * testComment
                     */
                    class TestPackageName {  }

                """.trimIndent()
            )
        ).isEmpty()
    }
}
