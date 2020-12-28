package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.classes.DataClassesRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames.USE_DATA_CLASS
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class DataClassesRuleWarnTest : LintTestBase(::DataClassesRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:data-classes"

    @Test
    @Tag(USE_DATA_CLASS)
    fun `trigger on default class`() {
        lintMethod(
            """
                    |class Some(val a: Int = 5) {
                    |
                    |}
                """.trimMargin(),
            LintError(1, 1, ruleId, "${Warnings.USE_DATA_CLASS.warnText()} Some")
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `should trigger - dont forget to consider this class in fix`() {
        lintMethod(
            """
                    |class Test {
                    |   var a: Int = 0
                    |          get() = field
                    |          set(value: Int) { field = value}
                    |}
                """.trimMargin(),
            LintError(1, 1, ruleId, "${Warnings.USE_DATA_CLASS.warnText()} Test")
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `should not trigger if there is some logic in accessor`() {
        lintMethod(
            """
                    |class Test {
                    |   var a: Int = 0
                    |          get() = field
                    |          set(value: Int) { 
                    |              field = value
                    |              someFun(value)
                    |          }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `should not trigger on class with bad modifiers`() {
        lintMethod(
            """
                    |data class Some(val a: Int = 5) {
                    |
                    |}
                    |
                    |abstract class Another() {}
                    |
                    |open class Open(){}
                    |
                    |sealed class Clazz{}
                    |
                    |data class CheckInner {
                    |   inner class Inner {}
                    |}
                    |
                    |enum class Num {
                    |
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `should not trigger on classes with functions`() {
        lintMethod(
            """
                    |class Some {
                    |   val prop = 5
                    |   private fun someFunc() {}
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `should not trigger on classes with no property in constructor`() {
        lintMethod(
            """
                    |class B(map: Map<Int,Int>) {}
                    |
                    |class A(val map: Map<Int, Int>) {}
                """.trimMargin(),
            LintError(3, 1, ruleId, "${Warnings.USE_DATA_CLASS.warnText()} A")
        )
    }
}
