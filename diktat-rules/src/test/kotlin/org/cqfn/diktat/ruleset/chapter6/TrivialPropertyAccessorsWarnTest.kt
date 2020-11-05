package org.cqfn.diktat.ruleset.chapter6

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.TrivialPropertyAccessors
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Test

class TrivialPropertyAccessorsWarnTest : LintTestBase(::TrivialPropertyAccessors) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:trivial-property-accessors"

    @Test
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
    fun `should trigger on trivial getter`() {
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
}