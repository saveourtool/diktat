package com.huawei.rri.fixbot.ruleset.huawei.chapter2

import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings
import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings.KDOC_WITHOUT_RETURN_TAG
import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings.KDOC_WITHOUT_THROWS_TAG
import com.huawei.rri.fixbot.ruleset.huawei.rules.KdocMethods
import com.huawei.rri.fixbot.ruleset.huawei.utils.lintMethod
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import test_framework.processing.TestComparatorUnit

class KdocSignatureTest {
    private val funCode = """
        fun doubleInt(a: Int): Int {
            if (Config.condition) throw IllegalStateException()
            return 2 * a
        }
    """.trimIndent()

    @Test
    fun `Accessible methods with parameters, return type and throws should have proper KDoc (positive example)`() {
        val validCode = """
            /**
            * Test method
            * @param a - dummy integer
            * @return doubled value
            * @throws IllegalStateException
            */
            $funCode
        """.trimIndent()

        lintMethod(KdocMethods(), validCode)
    }

    @Test
    fun `Warning should not be triggered for private functions`() {
        val validCode = "private $funCode"

        lintMethod(KdocMethods(), validCode)
    }

    @Test
    fun `Empty parameter list should not trigger warning about @param`() {
        val validCode = """
            /**
            * Test method
            * @return zero
            * @throws IllegalStateException
            */
            fun foo(): Int {
                return 0
            }
        """.trimIndent()

        lintMethod(KdocMethods(), validCode)
    }

    @Test
    fun `All methods with parameters should have @param KDoc`() {
        val invalidKDoc = """
            /**
            * Test method
            * @return doubled value
            * @throws IllegalStateException
            */
        """.trimIndent()
        val invalidCode = """
            $invalidKDoc
            $funCode
        """.trimIndent()

        lintMethod(KdocMethods(), invalidCode, LintError(1, 13, "kdoc-methods",
            "${Warnings.KDOC_WITHOUT_PARAM_TAG.warnText()} a")
        )
    }

    @Test
    fun `All methods with parameters should have @param KDoc for each parameter`() {
        val invalidKDoc = """
            /**
            * Test method
            * @param a - dummy integer
            * @return doubled value
            * @throws IllegalStateException
            */
        """.trimIndent()
        val invalidCode = """
            $invalidKDoc
            fun addInts(a: Int, b: Int): Int = a + b
        """.trimIndent()

        lintMethod(KdocMethods(), invalidCode, LintError(1, 13, "kdoc-methods",
            "${Warnings.KDOC_WITHOUT_PARAM_TAG.warnText()} b")
        )
    }

    @Test
    fun `All methods with explicit return type excluding Unit should have @return KDoc`() {
        val invalidKDoc = """
            /**
            * Test method
            * @param a - dummy integer
            * @throws IllegalStateException
            */
        """.trimIndent()
        val invalidCode = """
            $invalidKDoc
            $funCode
        """.trimIndent()

        lintMethod(KdocMethods(), invalidCode, LintError(1, 13, "kdoc-methods",
            "${KDOC_WITHOUT_RETURN_TAG.warnText()} "))
    }

    @Test
    fun `All methods with throw in method body should have @throws KDoc`() {
        val invalidKDoc = """
            /**
            * Test method
            * @param a - dummy integer
            * @return doubled value
            */
        """.trimIndent()
        val invalidCode = """
            $invalidKDoc
            $funCode
        """.trimIndent()

        lintMethod(KdocMethods(), invalidCode, LintError(1, 13, "kdoc-methods",
            "${KDOC_WITHOUT_THROWS_TAG.warnText()} "))
    }

    @Test
    fun `Linter shouldn't detect throws inside comments`() {
        val invalidKDoc = """
            /**
            * Test method
            * @param a - dummy integer
            * @return doubled value
            */
        """.trimIndent()
        val invalidCode = """
            $invalidKDoc
            fun foo(a: Int) {
                // throw Exception()
                return bar
            }
        """.trimIndent()

        lintMethod(KdocMethods(), invalidCode)
    }

    @Test
    fun `Rule should suggest KDoc template for missing KDocs`() {
        val testComparatorUnit = TestComparatorUnit("test/paragraph2/kdoc") { text, fileName ->
            KtLint.format(
                KtLint.Params(
                    text = text,
                    ruleSets = listOf(RuleSet("huawei-codestyle", KdocMethods())),
                    fileName = fileName,
                    cb = { _, _ -> }
                )
            )
        }

        assertTrue(
            testComparatorUnit
                .compareFilesFromResources("EmptyKdocFixed.kt", "EmptyKdocTest.kt")
        )
    }
}
