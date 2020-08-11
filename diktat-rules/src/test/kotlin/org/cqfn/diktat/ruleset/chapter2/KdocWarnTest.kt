package org.cqfn.diktat.ruleset.chapter2

import com.pinterest.ktlint.core.LintError
import org.junit.jupiter.api.Test
import org.cqfn.diktat.ruleset.constants.Warnings.*
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.kdoc.KdocComments
import org.cqfn.diktat.util.lintMethod

class KdocWarnTest {
    private val ruleId: String = "$DIKTAT_RULE_SET_ID:kdoc-comments"

    @Test
    fun `all public classes should be documented with KDoc`() {
        val code =
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
        lintMethod(KdocComments(), code,
                LintError(1, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} SomeGoodName"),
                LintError(6, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} SomeOtherGoodName"),
                LintError(9, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} SomeNewGoodName"),
                LintError(12, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} SomeOtherNewGoodName")
        )
    }

    @Test
    fun `all internal classes should be documented with KDoc`() {
        val code =
                """
                    internal class SomeGoodName {
                    }
                """.trimIndent()
        lintMethod(KdocComments(), code, LintError(
                1, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} SomeGoodName")
        )
    }

    @Test
    fun `all internal and public functions on top-level should be documented with Kdoc`() {
        val code =
                """
                    fun someGoodName() {
                    }

                    internal fun someGoodNameNew(): String {
                        return " ";
                    }
                """.trimIndent()
        lintMethod(KdocComments(), code,
                LintError(1, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} someGoodName"),
                LintError(4, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} someGoodNameNew")
        )
    }

    @Test
    fun `all internal and public functions on top-level should be documented with Kdoc (positive case)`() {
        val code =
                """
                    private fun someGoodName() {
                    }
                """.trimIndent()
        lintMethod(KdocComments(), code)
    }

    @Test
    fun `positive Kdoc case with private class`() {
        val code =
                """
                    private class SomeGoodName {
                    }
                """.trimIndent()
        lintMethod(KdocComments(), code)
    }

    @Test
    fun `Kdoc should present for each class element`() {
        val code =
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
        lintMethod(KdocComments(), code,
                LintError(5, 5, ruleId, "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} variable"),
                LintError(7, 5, ruleId, "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} perfectFunction"),
                LintError(13, 5, ruleId, "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} InternalClass")
        )
    }

    @Test
    fun `Kdoc should present for each class element (positive)`() {
        val code =
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
        lintMethod(KdocComments(), code)
    }

    @Test
    fun `regression - should not force documentation on standard methods`() {
        lintMethod(KdocComments(),
                """
                    |/**
                    | * This is an example class
                    | */
                    |class Example {
                    |    override fun toString() = ""
                    |}
                """.trimMargin()
        )
    }
}
