package com.saveourtool.diktat.ruleset.chapter6

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings.WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR
import com.saveourtool.diktat.ruleset.rules.chapter6.PropertyAccessorFields
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class PropertyAccessorFieldsWarnTest : LintTestBase(::PropertyAccessorFields) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${PropertyAccessorFields.NAME_ID}"

    @Test
    @Tag(WarningNames.WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR)
    fun `check simple correct examples`() {
        lintMethod(
            """
                    |class A {
                    |
                    |   var isEmpty: Boolean = false
                    |   set(value) {
                    |       println("Side effect")
                    |       field = value
                    |   }
                    |   get() = field
                    |
                    |   var isNotEmpty: Boolean = true
                    |   set(value) {
                    |       val q = isEmpty.and(true)
                    |       field = value
                    |   }
                    |   get() {
                    |       println(12345)
                    |       return field
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR)
    fun `check wrong setter and getter examples`() {
        lintMethod(
            """
                    |class A {
                    |
                    |   var isEmpty: Boolean = false
                    |   set(values) {
                    |       println("Side effect")
                    |       isEmpty = values
                    |   }
                    |
                    |   var isNotEmpty: Boolean = true
                    |   set(value) {
                    |       val q = isNotEmpty.and(true)
                    |       field = value
                    |   }
                    |   get() {
                    |       println(12345)
                    |       return isNotEmpty
                    |   }
                    |
                    |   var isNotOk: Boolean = false
                    |   set(values) {
                    |       this.isNotOk = values
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(4, 4, ruleId, "${WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR.warnText()} set(values) {..."),
            DiktatError(14, 4, ruleId, "${WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR.warnText()} get() {..."),
            DiktatError(20, 4, ruleId, "${WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR.warnText()} set(values) {...")
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR)
    @Suppress("TOO_LONG_FUNCTION")
    fun `check examples with local var`() {
        lintMethod(
            """
                    |class A {
                    |
                    |   var isEmpty: Boolean = false
                    |   set(values) {
                    |       fun foo() {
                    |           val isEmpty = false
                    |       }
                    |       isEmpty = values
                    |   }
                    |
                    |   var isNotOk: Boolean = false
                    |   set(valuess) {
                    |       var isNotOk = true
                    |       isNotOk = valuess
                    |   }
                    |
                    |   var isOk: Boolean = false
                    |   set(valuess) {
                    |       isOk = valuess
                    |       var isOk = true
                    |   }
                    |
                    |   var isNotEmpty: Boolean = true
                    |   set(value) {
                    |       val q = isNotEmpty
                    |       field = value
                    |   }
                    |   get() = field
                    |}
            """.trimMargin(),
            DiktatError(4, 4, ruleId, "${WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR.warnText()} set(values) {..."),
            DiktatError(18, 4, ruleId, "${WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR.warnText()} set(valuess) {..."),
            DiktatError(24, 4, ruleId, "${WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR.warnText()} set(value) {...")
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR)
    fun `shouldn't be triggered when there's a method with the same name as the property`() {
        lintMethod(
            """
                    |class A {
                    |
                    |   val blaBla: String
                    |       get() = "bla".blaBla("bla")
                    |
                    |   fun blaBla(string: String): String = this + string
                    |
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR)
    fun `shouldn't be triggered when the property is an extension property`() {
        lintMethod(
            """
                    |class A {
                    |
                    |   fun String.foo() = 42
                    |       val String.foo: Int
                    |       get() = foo
                    |
                    |}
            """.trimMargin()
        )
    }
}
