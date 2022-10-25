package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.chapter6.TrivialPropertyAccessors
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames.TRIVIAL_ACCESSORS_ARE_NOT_RECOMMENDED
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TrivialPropertyAccessorsWarnTest : LintTestBase(::TrivialPropertyAccessors) {
    private val ruleId = TrivialPropertyAccessors.NAME_ID

    @Test
    @Tag(TRIVIAL_ACCESSORS_ARE_NOT_RECOMMENDED)
    fun `should trigger on trivial getter and setter`() {
        lintMethod(
            """
                    |class Test {
                    |   val prop: Int = 0
                    |       get() { return field }
                    |       set(value) { field = value }
                    |}
            """.trimMargin(),
            LintError(3, 8, ruleId, "${Warnings.TRIVIAL_ACCESSORS_ARE_NOT_RECOMMENDED.warnText()} get() { return field }", true),
            LintError(4, 8, ruleId, "${Warnings.TRIVIAL_ACCESSORS_ARE_NOT_RECOMMENDED.warnText()} set(value) { field = value }", true)
        )
    }

    @Test
    @Tag(TRIVIAL_ACCESSORS_ARE_NOT_RECOMMENDED)
    fun `should trigger on trivial getter and setter equal case`() {
        lintMethod(
            """
                    |class Test {
                    |   val prop: Int = 0
                    |       get() = field
                    |}
            """.trimMargin(),
            LintError(3, 8, ruleId, "${Warnings.TRIVIAL_ACCESSORS_ARE_NOT_RECOMMENDED.warnText()} get() = field", true)
        )
    }

    @Test
    @Tag(TRIVIAL_ACCESSORS_ARE_NOT_RECOMMENDED)
    fun `should not trigger on getter and setter`() {
        lintMethod(
            """
                    |class Test {
                    |   val prop: Int = 0
                    |       get() {
                    |           val b = someLogic(field)
                    |           return b
                    |       }
                    |       set(value) {
                    |           val res = func(value)
                    |           field = res
                    |       }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(TRIVIAL_ACCESSORS_ARE_NOT_RECOMMENDED)
    fun `should not trigger on private setter`() {
        lintMethod(
            """
                    |class Test {
                    |   var testName: String? = null
                    |       private set
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(TRIVIAL_ACCESSORS_ARE_NOT_RECOMMENDED)
    fun `should trigger on getter without braces`() {
        lintMethod(
            """
                    |class Test {
                    |   val testName = 0
                    |       get
                    |}
            """.trimMargin(),
            LintError(3, 8, ruleId, "${Warnings.TRIVIAL_ACCESSORS_ARE_NOT_RECOMMENDED.warnText()} get", true)
        )
    }
}
