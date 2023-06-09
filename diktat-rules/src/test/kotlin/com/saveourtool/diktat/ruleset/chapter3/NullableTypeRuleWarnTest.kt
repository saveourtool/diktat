package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings.NULLABLE_PROPERTY_TYPE
import com.saveourtool.diktat.ruleset.rules.chapter3.NullableTypeRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class NullableTypeRuleWarnTest : LintTestBase(::NullableTypeRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${NullableTypeRule.NAME_ID}"

    @Test
    @Tag(WarningNames.NULLABLE_PROPERTY_TYPE)
    fun `check simple property`() {
        lintMethod(
            """
                    |val a: List<Int>? = null
                    |val a: Int? = null
                    |val b: Double? = null
                    |val c: String? = null
                    |val a: MutableList<Int>? = null
            """.trimMargin(),
            DiktatError(1, 21, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} initialize explicitly", true),
            DiktatError(2, 15, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} initialize explicitly", true),
            DiktatError(3, 18, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} initialize explicitly", true),
            DiktatError(4, 18, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} initialize explicitly", true),
            DiktatError(5, 28, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} initialize explicitly", true)
        )
    }

    @Test
    @Tag(WarningNames.NULLABLE_PROPERTY_TYPE)
    fun `check property in object`() {
        lintMethod(
            """
                    |class A {
                    |   companion object {
                    |       val a: Int? = null
                    |       val b: Int? = 0
                    |       val c: Boolean? = true
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(3, 22, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} initialize explicitly", true),
            DiktatError(4, 15, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} don't use nullable type", false),
            DiktatError(5, 15, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} don't use nullable type", false)
        )
    }

    @Test
    @Tag(WarningNames.NULLABLE_PROPERTY_TYPE)
    fun `check nullable type with initialize`() {
        lintMethod(
            """
                    |class A {
                    |   companion object {
                    |       val a: Int? = 0
                    |       val b: Int? = null
                    |       val c: Boolean? = false
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(3, 15, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} don't use nullable type", false),
            DiktatError(4, 22, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} initialize explicitly", true),
            DiktatError(5, 15, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} don't use nullable type", false)
        )
    }

    @Test
    @Tag(WarningNames.NULLABLE_PROPERTY_TYPE)
    fun `d nullable type with initialize`() {
        lintMethod(
            """
                    |class A {
                    |   val rulesConfigList: List<RulesConfig>? = RulesConfigReader(javaClass.classLoader).readResource("src/test/resources/test-rules-config.yml")
                    |   val q: Int? = foo()
                    |   val e: A.Q? = null
                    |}
            """.trimMargin(),
            DiktatError(4, 18, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} initialize explicitly", false)
        )
    }

    @Test
    @Tag(WarningNames.TYPE_ALIAS)
    fun `should trigger on collection factory`() {
        lintMethod(
            """
                    | val q: List<Int>? = emptyList<Int>()
                    | val w: List<Map<Int, Int>> = emptyList<Map<Int, Int>>()
                    | val c: Set<Int>? = setOf()
                    | val d: List<Int?> = emptyList()
            """.trimMargin(),
            DiktatError(1, 9, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} don't use nullable type", false),
            DiktatError(3, 9, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} don't use nullable type", false)
        )
    }

    @Test
    @Tag(WarningNames.TYPE_ALIAS)
    fun `shouldn't trigger`() {
        lintMethod(
            """
                    | val superClassName: String? = node
                    |   .getFirstChildWithType(ElementType.SUPER_TYPE_LIST)
                    |   ?.findLeafWithSpecificType(TYPE_REFERENCE)
                    |   ?.text
                    |
                    | private val rulesConfigList: List<RulesConfig>? = rulesConfigList ?: RulesConfigReader(javaClass.classLoader).readResource("diktat-analysis.yml")
            """.trimMargin())
    }
}
