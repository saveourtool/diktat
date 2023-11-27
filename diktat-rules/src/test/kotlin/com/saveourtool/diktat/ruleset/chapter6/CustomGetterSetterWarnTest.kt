package com.saveourtool.diktat.ruleset.chapter6

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter6.CustomGetterSetterRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames.CUSTOM_GETTERS_SETTERS
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CustomGetterSetterWarnTest : LintTestBase(::CustomGetterSetterRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${CustomGetterSetterRule.NAME_ID}"

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
            DiktatError(3, 9, ruleId, "${Warnings.CUSTOM_GETTERS_SETTERS.warnText()} set"),
            DiktatError(7, 9, ruleId, "${Warnings.CUSTOM_GETTERS_SETTERS.warnText()} get"),
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
            """.trimMargin(),
        )
    }

    @Test
    @Tag(CUSTOM_GETTERS_SETTERS)
    fun `override getter`() {
        lintMethod(
            """
                    |interface A {
                    |    val a: Int
                    |}
                    |
                    |object B: A {
                    |    override val a: int
                    |        get() = 0
                    |}
            """.trimMargin(),
        )
    }

    @Test
    @Tag(CUSTOM_GETTERS_SETTERS)
    fun `exception case with private setter`() {
        lintMethod(
            """
                    |class A {
                    |    var size: Int = 0
                    |        private set(value) {
                    |            println("Side effect")
                    |            field = value
                    |        }
                    |        get() = this.hashCode() * 2
                    |}
            """.trimMargin(),
            DiktatError(7, 9, ruleId, "${Warnings.CUSTOM_GETTERS_SETTERS.warnText()} get"),
        )
    }

    @Test
    @Tag(CUSTOM_GETTERS_SETTERS)
    fun `exception case with protected setter`() {
        lintMethod(
            """
                    |class A {
                    |    var size: Int = 0
                    |        protected set(value) {
                    |            println("Side effect")
                    |            field = value
                    |        }
                    |        get() = this.hashCode() * 2
                    |}
            """.trimMargin(),
            DiktatError(3, 19, ruleId, "${Warnings.CUSTOM_GETTERS_SETTERS.warnText()} set"),
            DiktatError(7, 9, ruleId, "${Warnings.CUSTOM_GETTERS_SETTERS.warnText()} get"),
        )
    }

    @Test
    @Tag(CUSTOM_GETTERS_SETTERS)
    fun `should not trigger on property with backing field`() {
        lintMethod(
            """
                    |package com.example
                    |
                    |class MutableTableContainer {
                    |   private var _table: Map<String, Int>? = null
                    |
                    |   val table: Map<String, Int>
                    |       get() {
                    |           if (_table == null) {
                    |               _table = hashMapOf()
                    |           }
                    |           return _table ?: throw AssertionError("Set to null by another thread")
                    |       }
                    |       set(value) {
                    |           field = value
                    |       }
                    |
                    |}
            """.trimMargin(),
        )
    }

    @Test
    @Tag(CUSTOM_GETTERS_SETTERS)
    fun `should trigger on backing field with setter`() {
        val code =
            """
                    |package com.example
                    |
                    |class MutableTableContainer {
                    |   var _table: Map<String, Int>? = null
                    |                set(value) {
                    |                    field = value
                    |                }
                    |
                    |   val table: Map<String, Int>
                    |       get() {
                    |           if (_table == null) {
                    |               _table = hashMapOf()
                    |           }
                    |           return _table ?: throw AssertionError("Set to null by another thread")
                    |       }
                    |       set(value) {
                    |           field = value
                    |       }
                    |
                    |}
            """.trimMargin()
        lintMethod(code,
            DiktatError(5, 17, ruleId, "${Warnings.CUSTOM_GETTERS_SETTERS.warnText()} set", false),
            DiktatError(10, 8, ruleId, "${Warnings.CUSTOM_GETTERS_SETTERS.warnText()} get", false),
            DiktatError(16, 8, ruleId, "${Warnings.CUSTOM_GETTERS_SETTERS.warnText()} set", false)
        )
    }
}

