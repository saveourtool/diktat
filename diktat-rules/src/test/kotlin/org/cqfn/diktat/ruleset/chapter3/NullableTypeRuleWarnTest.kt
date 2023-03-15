package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.constants.Warnings.NULLABLE_PROPERTY_TYPE
import org.cqfn.diktat.ruleset.rules.chapter3.NullableTypeRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.constants.WarningsNames
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
            LintError(1, 21, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} initialize explicitly", true),
            LintError(2, 15, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} initialize explicitly", true),
            LintError(3, 18, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} initialize explicitly", true),
            LintError(4, 18, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} initialize explicitly", true),
            LintError(5, 28, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} initialize explicitly", true)
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
            LintError(3, 22, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} initialize explicitly", true),
            LintError(4, 15, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} don't use nullable type", false),
            LintError(5, 15, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} don't use nullable type", false)
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
            LintError(3, 15, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} don't use nullable type", false),
            LintError(4, 22, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} initialize explicitly", true),
            LintError(5, 15, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} don't use nullable type", false)
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
            LintError(4, 18, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} initialize explicitly", false)
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
            LintError(1, 9, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} don't use nullable type", false),
            LintError(3, 9, ruleId, "${NULLABLE_PROPERTY_TYPE.warnText()} don't use nullable type", false)
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
