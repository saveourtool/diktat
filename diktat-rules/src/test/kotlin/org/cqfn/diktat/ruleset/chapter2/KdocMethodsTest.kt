package org.cqfn.diktat.ruleset.chapter2

import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_WITHOUT_RETURN_TAG
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_WITHOUT_THROWS_TAG
import org.cqfn.diktat.ruleset.rules.kdoc.KdocMethods
import org.cqfn.diktat.util.lintMethod
import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.junit.Test

class KdocMethodsTest {
    private val ruleId: String = "$DIKTAT_RULE_SET_ID:kdoc-methods"

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

        lintMethod(KdocMethods(), invalidCode, LintError(1, 13, ruleId,
                "${Warnings.KDOC_WITHOUT_PARAM_TAG.warnText()} doubleInt (a)")
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

        lintMethod(KdocMethods(), invalidCode, LintError(1, 12, ruleId,
                "${Warnings.KDOC_WITHOUT_PARAM_TAG.warnText()} addInts (b)")
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

        lintMethod(KdocMethods(), invalidCode, LintError(1, 13, ruleId,
                "${KDOC_WITHOUT_RETURN_TAG.warnText()} doubleInt"))
    }

    @Test
    fun `All methods with expression body should have @return tag or explicitly set return type to Unit`() {
        val kdocWithoutReturn = """
            /**
             * Test method
             * @param a - dummy integer
             * @throws IllegalStateException
             */
        """.trimIndent()

        val invalidCode = """
            $kdocWithoutReturn
            fun foo(a: Int) = bar(2 * a)

            $kdocWithoutReturn
            fun bar(a: Int): Unit = this.list.add(a)

            private val list = mutableListOf<Int>()
        """.trimIndent()

        lintMethod(KdocMethods(), invalidCode, LintError(1, 12, ruleId,
                "${KDOC_WITHOUT_RETURN_TAG.warnText()} foo"))
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

        lintMethod(KdocMethods(), invalidCode, LintError(1, 13, ruleId,
                "${KDOC_WITHOUT_THROWS_TAG.warnText()} doubleInt (IllegalStateException)"))
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
    fun `All thrown exceptions should be in KDoc`() {
        val invalidCode = """
            /**
             * Test method
             * @param a - dummy integer
             * @return doubled value
             * @throws IllegalStateException
             */
            fun doubleInt(a: Int): Int {
                if (Config.condition) throw IllegalStateException()
                if (Config.condition2) throw IllegalAccessException()
                return 2 * a
            }
        """.trimIndent()

        lintMethod(KdocMethods(), invalidCode, LintError(1, 1, ruleId,
                "${KDOC_WITHOUT_THROWS_TAG.warnText()} doubleInt (IllegalAccessException)"))
    }
}
