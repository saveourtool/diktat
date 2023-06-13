package com.saveourtool.diktat.ruleset.chapter5

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings.WRONG_OVERLOADING_FUNCTION_ARGUMENTS
import com.saveourtool.diktat.ruleset.rules.chapter5.OverloadingArgumentsFunction
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class OverloadingArgumentsFunctionWarnTest : LintTestBase(::OverloadingArgumentsFunction) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${OverloadingArgumentsFunction.NAME_ID}"

    @Test
    @Tag(WarningNames.WRONG_OVERLOADING_FUNCTION_ARGUMENTS)
    fun `check simple example`() {
        lintMethod(
            """
                    |fun foo() {}
                    |
                    |fun foo(a: Int) {}
                    |
                    |fun goo(a: Double) {}
                    |
                    |fun goo(a: Float, b: Double) {}
                    |
                    |fun goo(b: Float, a: Double, c: Int) {}
                    |
                    |@Suppress("WRONG_OVERLOADING_FUNCTION_ARGUMENTS")
                    |fun goo(a: Float)
                    |
                    |fun goo(a: Double? = 0.0) {}
                    |
                    |override fun goo() {} // this definitely is not an overload case... why we were treating it as an overload? New diktat rule!
                    |
                    |class A {
                    |   fun foo() {}
                    |}
                    |
                    |abstract class B {
                    |   abstract fun foo(a: Int) // modifiers are different. This is not related to default arguments. New diktat rule!
                    |
                    |   fun foo(){}
                    |}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${WRONG_OVERLOADING_FUNCTION_ARGUMENTS.warnText()} foo", false),
        )
    }

    @Test
    @Tag(WarningNames.WRONG_OVERLOADING_FUNCTION_ARGUMENTS)
    fun `functions with modifiers`() {
        lintMethod(
            """
                    |public fun foo() {}
                    |private fun foo(a: Int) {}
                    |inline fun foo(a: Int, b: Int) {}
            """.trimMargin(),
        )
    }

    @Test
    @Tag(WarningNames.WRONG_OVERLOADING_FUNCTION_ARGUMENTS)
    fun `functions with override modifiers`() {
        lintMethod(
            """
                    |fun list(projectCoordinates: ProjectCoordinates): Flux<FileKey> = list()
                    |    .filter { it.projectCoordinates == projectCoordinates }
                    |
                    |override fun list(): Flux<FileKey> = newFileStorage.list()
                    |    .map { fileDto ->
                    |        FileKey(
                    |            projectCoordinates = fileDto.projectCoordinates,
                    |            name = fileDto.name,
                    |            uploadedMillis = fileDto.uploadedTime.toInstant(TimeZone.UTC).toEpochMilliseconds()
                    |        )
                    |    }
            """.trimMargin(),
        )
    }
    @Test
    @Tag(WarningNames.WRONG_OVERLOADING_FUNCTION_ARGUMENTS)
    fun `functions with override modifiers simple`() {
        lintMethod(
            """
                    |fun foo(a: Int) {}
                    |override fun foo() {}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_OVERLOADING_FUNCTION_ARGUMENTS)
    fun `functions with unordered, but same modifiers`() {
        lintMethod(
            """
                    |fun foo(a: Double) {}
                    |fun foo(a: Double, b: Int) {}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${WRONG_OVERLOADING_FUNCTION_ARGUMENTS.warnText()} foo", false)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_OVERLOADING_FUNCTION_ARGUMENTS)
    fun `functions with unordered, but same modifiers and different names`() {
        lintMethod(
            """
                    |fun foo(a: Double) {}
                    |fun foo(b: Double, b: Int) {}
            """.trimMargin(),
        )
    }

    @Test
    @Tag(WarningNames.WRONG_OVERLOADING_FUNCTION_ARGUMENTS)
    fun `check for extensions`() {
        lintMethod(
            """
                    private fun isComparisonWithAbs(psiElement: PsiElement) =
                        when (psiElement) {
                            is KtBinaryExpression -> psiElement.isComparisonWithAbs()
                            is KtDotQualifiedExpression -> psiElement.isComparisonWithAbs()
                            else -> false
                        }

                    private fun KtBinaryExpression.isComparisonWithAbs() =
                            takeIf { it.operationToken in comparisonOperators }
                            ?.run { left as? KtCallExpression ?: right as? KtCallExpression }
                            ?.run { calleeExpression as? KtNameReferenceExpression }
                            ?.getReferencedName()
                            ?.equals("abs")
                            ?: false

                    private fun KtBinaryExpression.isComparisonWithAbs(a: Int) =
                            takeIf { it.operationToken in comparisonOperators }
                            ?.run { left as? KtCallExpression ?: right as? KtCallExpression }
                            ?.run { calleeExpression as? KtNameReferenceExpression }
                            ?.getReferencedName()
                            ?.equals("abs")
                            ?: false

                    private fun KtBinaryExpression.isComparisonWithAbs(a: Int): Boolean {
                            return takeIf { it.operationToken in comparisonOperators }
                            ?.run { left as? KtCallExpression ?: right as? KtCallExpression }
                            ?.run { calleeExpression as? KtNameReferenceExpression }
                            ?.getReferencedName()
                            ?.equals("abs")
                            ?: false
                    }
            """.trimMargin(),
            DiktatError(8, 21, ruleId, "${WRONG_OVERLOADING_FUNCTION_ARGUMENTS.warnText()} isComparisonWithAbs", false)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_OVERLOADING_FUNCTION_ARGUMENTS)
    fun `check methods with different return types`() {
        lintMethod(
            """
                    private fun KtBinaryExpression.isComparisonWithAbs(a: Int): Boolean {
                            return takeIf { it.operationToken in comparisonOperators }
                            ?.run { left as? KtCallExpression ?: right as? KtCallExpression }
                            ?.run { calleeExpression as? KtNameReferenceExpression }
                            ?.getReferencedName()
                            ?.equals("abs")
                            ?: false
                    }

                    private fun KtBinaryExpression.isComparisonWithAbs(a: Int): Int {
                            val q = takeIf { it.operationToken in comparisonOperators }
                            ?.run { left as? KtCallExpression ?: right as? KtCallExpression }
                            ?.run { calleeExpression as? KtNameReferenceExpression }
                            ?.getReferencedName()
                            ?.equals("abs")
                            ?: false

                            if (q) return 10
                            return 11
                    }
            """.trimMargin()
        )
    }
}
