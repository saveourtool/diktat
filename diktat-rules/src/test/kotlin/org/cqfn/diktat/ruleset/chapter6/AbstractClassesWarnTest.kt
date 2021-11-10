package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter6.classes.AbstractClassesRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames.CLASS_SHOULD_NOT_BE_ABSTRACT
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class AbstractClassesWarnTest : LintTestBase(::AbstractClassesRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:abstract-classes"

    @Test
    @Tag(CLASS_SHOULD_NOT_BE_ABSTRACT)
    fun `should not replace abstract with open`() {
        lintMethod(
            """
                |abstract class Some(val a: Int = 5) {
                |   abstract fun func() {}
                |   
                |   fun another() {}
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(CLASS_SHOULD_NOT_BE_ABSTRACT)
    fun `should replace abstract on open`() {
        lintMethod(
            """
                |abstract class Some(val a: Int = 5) {
                |    fun func() {}
                |}
            """.trimMargin(),
            LintError(1, 37, ruleId, "${Warnings.CLASS_SHOULD_NOT_BE_ABSTRACT.warnText()} Some", true)
        )
    }

    @Test
    @Tag(CLASS_SHOULD_NOT_BE_ABSTRACT)
    fun `should replace abstract on open with inner`() {
        lintMethod(
            """
                |class Some(val a: Int = 5) {
                |    fun func() {}
                |    
                |    inner abstract class Inner {
                |       fun another()
                |    }
                |}
            """.trimMargin(),
            LintError(4, 32, ruleId, "${Warnings.CLASS_SHOULD_NOT_BE_ABSTRACT.warnText()} Inner", true)
        )
    }

    @Test
    @Tag(CLASS_SHOULD_NOT_BE_ABSTRACT)
    fun `should replace abstract on open in actual or expect classes`() {
        lintMethod(
            """
                |actual abstract class CoroutineTest actual constructor() {
                |    /**
                |     * Test rule
                |     */
                |    @get:Rule
                |    var coroutineTestRule = CoroutineTestRule()
                |    
                |    /**
                |     * Run test
                |     *
                |     * @param T
                |     * @param block
                |     * @receiver a Coroutine Scope
                |     */
                |    actual fun <T> runTest(block: suspend CoroutineScope.() -> T) {
                |       runBlocking {
                |       block()
                |       }
                |    }
                |}
            """.trimMargin(),
            LintError(1, 58, ruleId, "${Warnings.CLASS_SHOULD_NOT_BE_ABSTRACT.warnText()} CoroutineTest", true)
        )
    }

    @Test
    @Tag(CLASS_SHOULD_NOT_BE_ABSTRACT)
    fun `should not remove abstract on class if there are only abstract properties`() {
        lintMethod(
            """
                |abstract class BaseUsesProcessor() {
                |    // Store uses by file
                |    abstract val a: String
                |    
                |    fun foo() {}
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(CLASS_SHOULD_NOT_BE_ABSTRACT)
    fun `should not trigger on classes that extend other classes`() {
        lintMethod(
            """
                |abstract class Example: Base() {
                |    fun foo() {}
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(CLASS_SHOULD_NOT_BE_ABSTRACT)
    fun `should trigger on classes that implement interfaces`() {
        lintMethod(
            """
                |abstract class Example: Base {
                |    fun foo() {}
                |}
            """.trimMargin(),
            LintError(1, 30, ruleId, "[CLASS_SHOULD_NOT_BE_ABSTRACT] class should not be abstract, because it has no abstract functions: Example", true)
        )
    }
}
