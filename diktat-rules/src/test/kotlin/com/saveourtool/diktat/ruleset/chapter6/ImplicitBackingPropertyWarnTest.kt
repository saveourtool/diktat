package com.saveourtool.diktat.ruleset.chapter6

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter6.ImplicitBackingPropertyRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames.NO_CORRESPONDING_PROPERTY
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class ImplicitBackingPropertyWarnTest : LintTestBase(::ImplicitBackingPropertyRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${ImplicitBackingPropertyRule.NAME_ID}"

    @Test
    @Tag(NO_CORRESPONDING_PROPERTY)
    fun `not trigger on backing property`() {
        lintMethod(
            """
                    |class Some(val a: Int = 5) {
                    |   private var _table: Map<String, Int>? = null
                    |   val table:Map<String, Int>
                    |       get() {
                    |           if (_table == null) {
                    |               _table = HashMap()
                    |           }
                    |           return _table ?: throw AssertionError("Set to null by another thread")
                    |       }
                    |       set(value) { field = value }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(NO_CORRESPONDING_PROPERTY)
    fun `trigger on backing property`() {
        lintMethod(
            """
                    |class Some(val a: Int = 5) {
                    |   private var a: Map<String, Int>? = null
                    |   val table:Map<String, Int>
                    |       get() {
                    |           if (a == null) {
                    |               a = HashMap()
                    |           }
                    |           return a ?: throw AssertionError("Set to null by another thread")
                    |       }
                    |       set(value) { field = value }
                    |}
            """.trimMargin(),
            DiktatError(3, 4, ruleId, "${Warnings.NO_CORRESPONDING_PROPERTY.warnText()} table has no corresponding property with name _table")
        )
    }

    @Test
    @Tag(NO_CORRESPONDING_PROPERTY)
    fun `don't trigger on regular backing property`() {
        lintMethod(
            """
                    |class Some(val a: Int = 5) {
                    |   private var _a: Map<String, Int>? = null
                    |   private val _some:Int? = null
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(NO_CORRESPONDING_PROPERTY)
    fun `don't trigger on regular property`() {
        lintMethod(
            """
                    |class Some(val a: Int = 5) {
                    |   private var a: Map<String, Int>? = null
                    |   private val some:Int? = null
                    |   private val _prop: String? = null
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(NO_CORRESPONDING_PROPERTY)
    fun `should not trigger if property has field in accessor`() {
        lintMethod(
            """
                    |class Some(val a: Int = 5) {
                    |   val table:Map<String, Int>
                    |       set(value) { field = value }
                    |   val _table: Map<String,Int>? = null
                    |
                    |   val some: Int
                    |       get() = 3
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(NO_CORRESPONDING_PROPERTY)
    fun `should not trigger on property with constant return`() {
        lintMethod(
            """
                    |class Some(val a: Int = 5) {
                    |   val table:Int
                    |       get() {
                    |           return 3
                    |       }
                    |       set(value) { field = value }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(NO_CORRESPONDING_PROPERTY)
    fun `should not trigger on property with chain call return`() {
        lintMethod(
            """
                    |class Some(val a: Int = 5) {
                    |   val table:Int
                    |       get() {
                    |           val some = listOf(1,2,3)
                    |           return some.filter { it -> it == 3}.first()
                    |       }
                    |       set(value) { field = value }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(NO_CORRESPONDING_PROPERTY)
    fun `should not trigger set accessor`() {
        lintMethod(
            """
                    |class Some(val a: Int = 5) {
                    |   val foo
                    |       set(value) {
                    |           if(isDelegate) log.debug(value)
                    |           field = value
                    |       }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(NO_CORRESPONDING_PROPERTY)
    fun `should trigger set accessor`() {
        lintMethod(
            """
                    |class Some(val a: Int = 5) {
                    |   val foo
                    |       set(value) {
                    |           if(isDelegate) log.debug(value)
                    |           a = value
                    |       }
                    |}
            """.trimMargin(),
            DiktatError(2, 4, ruleId, "${Warnings.NO_CORRESPONDING_PROPERTY.warnText()} foo has no corresponding property with name _foo")
        )
    }
}
