package com.huawei.rri.fixbot.ruleset.huawei.chapter2

import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings.*
import com.huawei.rri.fixbot.ruleset.huawei.rules.KdocComments

class KdocWarnTest {
    @Test
    fun `all public classes should be documented with KDoc`() {
        assertThat(
            KdocComments().lint(
                """
                    class SomeGoodName {
                        private class InternalClass {
                        }
                    }

                    public open class SomeOtherGoodName {
                    }

                    open class SomeNewGoodName {
                    }

                    public class SomeOtherNewGoodName {
                    }

                """.trimIndent()
            )
        ).containsExactly(
            LintError(1, 1, "kdoc-comments", "${MISSING_KDOC_TOP_LEVEL.warnText()} SomeGoodName"),
            LintError(6, 1, "kdoc-comments", "${MISSING_KDOC_TOP_LEVEL.warnText()} SomeOtherGoodName"),
            LintError(9, 1, "kdoc-comments", "${MISSING_KDOC_TOP_LEVEL.warnText()} SomeNewGoodName"),
            LintError(12, 1, "kdoc-comments", "${MISSING_KDOC_TOP_LEVEL.warnText()} SomeOtherNewGoodName")
        )
    }

    @Test
    fun `all internal classes should be documented with KDoc`() {
        assertThat(
            KdocComments().lint(
                """
                    internal class SomeGoodName {
                    }
                """.trimIndent()
            )
        ).containsExactly(LintError(
            1, 1, "kdoc-comments", "${MISSING_KDOC_TOP_LEVEL.warnText()} SomeGoodName")
        )
    }

    @Test
    fun `all internal and public functions on top-level should be documented with Kdoc`() {
        assertThat(
            KdocComments().lint(
                """
                    fun someGoodName() {
                    }

                    internal fun someGoodNameNew(): String {
                        return " ";
                    }
                """.trimIndent()
            )
        ).containsExactly(
            LintError(1, 1, "kdoc-comments", "${MISSING_KDOC_TOP_LEVEL.warnText()} someGoodName"),
            LintError(4, 1, "kdoc-comments", "${MISSING_KDOC_TOP_LEVEL.warnText()} someGoodNameNew")
        )
    }

    @Test
    fun `all internal and public functions on top-level should be documented with Kdoc (positive case)`() {
        assertThat(
            KdocComments().lint(
                """
                    private fun someGoodName() {
                    }
                """.trimIndent()
            )
        ).isEmpty()
    }

    @Test
    fun `positive Kdoc case with private class`() {
        assertThat(
            KdocComments().lint(
                """
                    private class SomeGoodName {
                    }
                """.trimIndent()
            )
        ).isEmpty()
    }

    @Test
    fun `Kdoc should present for each class element`() {
        assertThat(
            KdocComments().lint(
                """
                    /**
                    * class that contains fields, functions and public subclasses
                    **/
                    class SomeGoodName {
                        val variable: String = ""
                        private val privateVariable: String = ""
                        fun perfectFunction() {
                        }

                        private fun privateFunction() {
                        }

                        class InternalClass {
                        }

                        private class InternalClass {
                        }
                    }
                """.trimIndent()
            )
        ).containsExactly(
            LintError(5, 5, "kdoc-comments", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} variable"),
            LintError(7, 5, "kdoc-comments", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} perfectFunction"),
            LintError(13, 5, "kdoc-comments", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} InternalClass")
        )
    }

    @Test
    fun `Kdoc should present for each class element (positive)`() {
        assertThat(
            KdocComments().lint(
                """
                    /**
                    * class that contains fields, functions and public subclasses
                    **/
                    class SomeGoodName {
                        /**
                        * class that contains fields, functions and public subclasses
                        **/
                        val variable: String = ""

                        private val privateVariable: String = ""

                        /**
                        * class that contains fields, functions and public subclasses
                        **/
                        fun perfectFunction() {
                        }

                        private fun privateFunction() {
                        }

                        /**
                        * class that contains fields, functions and public subclasses
                        **/
                        class InternalClass {
                        }

                        private class InternalClass {
                        }
                    }
                """.trimIndent()
            )
        ).isEmpty()
    }
}
