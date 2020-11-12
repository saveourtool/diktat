package org.cqfn.diktat.ruleset.chapter6


import com.pinterest.ktlint.core.LintError
import generated.WarningNames.CUSTOM_GETTERS_SETTERS
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.CustomGetterSetterRule
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CustomGetterSetterWarnTest : LintTestBase(::CustomGetterSetterRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:custom-getter-setter"

    @Test
    @Tag(CUSTOM_GETTERS_SETTERS)
    fun `no custom getters and setters allowed`() {
        lintMethod(
                """
                    |class A {
                    |    var size: Int = 0
                    |        set(value) {
                    |            println("Side effect")
                    |            field = value
                    |        }
                    |        get() = this.hashCode() * 2
                    |}
                """.trimMargin(),
                LintError(3, 9, ruleId, "${Warnings.CUSTOM_GETTERS_SETTERS.warnText()} set"),
                LintError(7, 9, ruleId, "${Warnings.CUSTOM_GETTERS_SETTERS.warnText()} get"),
        )
    }

    @Test
    @Tag(CUSTOM_GETTERS_SETTERS)
    fun `no custom getters allowed`() {
        lintMethod(
                """
                    |class A {
                    |  
                    |        fun set(value) {
                    |            println("Side effect")
                    |        }
                    |        
                    |        fun get() = 47
                    |}
                """.trimMargin()
        )
    }
}
