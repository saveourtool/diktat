package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter6.ExtensionFunctionsSameNameRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames.EXTENSION_FUNCTION_SAME_SIGNATURE
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class ExtensionFunctionsSameNameWarnTest : LintTestBase(::ExtensionFunctionsSameNameRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${ExtensionFunctionsSameNameRule.nameId}"

    @Test
    @Tag(EXTENSION_FUNCTION_SAME_SIGNATURE)
    fun `should trigger on functions with same signatures`() {
        lintMethod(
            """
                |open class A
                |class B: A(), C
                |class D: A()
                |
                |fun A.foo() = "A"
                |fun B.foo() = "B"
                |fun D.foo() = "D"
                |
                |fun printClassName(s: A) { print(s.foo()) }
                |
                |fun main() { printClassName(B()) }
            """.trimMargin(),
            LintError(5, 1, ruleId, "${Warnings.EXTENSION_FUNCTION_SAME_SIGNATURE.warnText()} fun A.foo[] and fun B.foo[]"),
            LintError(5, 1, ruleId, "${Warnings.EXTENSION_FUNCTION_SAME_SIGNATURE.warnText()} fun A.foo[] and fun D.foo[]"),
            LintError(6, 1, ruleId, "${Warnings.EXTENSION_FUNCTION_SAME_SIGNATURE.warnText()} fun A.foo[] and fun B.foo[]"),
            LintError(7, 1, ruleId, "${Warnings.EXTENSION_FUNCTION_SAME_SIGNATURE.warnText()} fun A.foo[] and fun D.foo[]"),
        )
    }

    @Test
    @Tag(EXTENSION_FUNCTION_SAME_SIGNATURE)
    fun `should trigger on functions with same signatures 2`() {
        lintMethod(
            """
                |open class A
                |class B: A(), C
                |
                |fun A.foo(some: Int) = "A"
                |fun B.foo(some: Int) = "B"
                |
                |fun printClassName(s: A) { print(s.foo()) }
                |
                |fun main() { printClassName(B()) }
            """.trimMargin(),
            LintError(4, 1, ruleId, "${Warnings.EXTENSION_FUNCTION_SAME_SIGNATURE.warnText()} fun A.foo[some] and fun B.foo[some]"),
            LintError(5, 1, ruleId, "${Warnings.EXTENSION_FUNCTION_SAME_SIGNATURE.warnText()} fun A.foo[some] and fun B.foo[some]")
        )
    }

    @Test
    @Tag(EXTENSION_FUNCTION_SAME_SIGNATURE)
    fun `should not trigger on functions with different signatures`() {
        lintMethod(
            """
                |open class A
                |class B: A(), C
                |
                |fun A.foo(): Boolean = return true
                |fun B.foo() = "B"
                |
                |fun printClassName(s: A) { print(s.foo()) }
                |
                |fun main() { printClassName(B()) }
            """.trimMargin()
        )
    }

    @Test
    @Tag(EXTENSION_FUNCTION_SAME_SIGNATURE)
    fun `should not trigger on functions with different signatures 2`() {
        lintMethod(
            """
                |open class A
                |class B: A(), C
                |
                |fun A.foo() = return true
                |fun B.foo(some: Int) = "B"
                |
                |fun printClassName(s: A) { print(s.foo()) }
                |
                |fun main() { printClassName(B()) }
            """.trimMargin()
        )
    }

    @Test
    @Tag(EXTENSION_FUNCTION_SAME_SIGNATURE)
    fun `should not trigger on functions with unrelated classes`() {
        lintMethod(
            """
                |interface A
                |class B: A
                |class C
                |
                |fun C.foo() = "C"
                |fun B.foo() = "B"
                |
                |fun printClassName(s: A) { print(s.foo()) }
                |
                |fun main() { printClassName(B()) }
            """.trimMargin()
        )
    }

    @Test
    @Tag(EXTENSION_FUNCTION_SAME_SIGNATURE)
    fun `should not trigger on regular func`() {
        lintMethod(
            """
                |interface A
                |class B: A
                |class C
                |
                |fun foo() = "C"
                |fun bar() = "B"
                |
                |fun printClassName(s: A) { print(s.foo()) }
                |
                |fun main() { printClassName(B()) }
            """.trimMargin()
        )
    }

    @Test
    @Disabled
    @Tag(EXTENSION_FUNCTION_SAME_SIGNATURE)
    fun `should trigger on classes in other files`() {
        lintMethod(
            """
                |fun A.foo() = "A"
                |fun B.foo() = "B"
                |
                |fun printClassName(s: A) { print(s.foo()) }
                |
                |fun main() { printClassName(B()) }
            """.trimMargin(),
            LintError(1, 1, ruleId, "${Warnings.EXTENSION_FUNCTION_SAME_SIGNATURE.warnText()} fun A.foo() and fun B.foo()"),
            LintError(2, 1, ruleId, "${Warnings.EXTENSION_FUNCTION_SAME_SIGNATURE.warnText()} fun A.foo() and fun B.foo()")
        )
    }
}
