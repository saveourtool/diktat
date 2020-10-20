package org.cqfn.diktat.ruleset.chapter6

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.DataClassesRule
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class DataClassesRuleWarnTest : LintTestBase(::DataClassesRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:data-classes"

    @Test
    @Tag("USE_DATA_CLASS")
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
    @Tag("USE_DATA_CLASS")
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
                """.trimMargin()
        )
    }

    @Test
    @Tag("USE_DATA_CLASS")
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
}
