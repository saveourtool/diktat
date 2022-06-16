package org.cqfn.diktat.ruleset.chapter4

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.TYPE_ALIAS
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter4.TypeAliasRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class TypeAliasRuleWarnTest : LintTestBase(::TypeAliasRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${TypeAliasRule.NAME_ID}"
    private val rulesConfigListShortType: List<RulesConfig> = listOf(
        RulesConfig(TYPE_ALIAS.name, true,
            mapOf("typeReferenceLength" to "4"))
    )

    @Test
    @Tag(WarningNames.TYPE_ALIAS)
    fun `long reference with several MutableMaps`() {
        lintMethod(
            """
                    | val b: MutableMap<String, MutableList<String>>
                    | val b = listof<Int>()
            """.trimMargin(),
            LintError(1, 9, ruleId, "${TYPE_ALIAS.warnText()} too long type reference", false)
        )
    }

    @Test
    @Tag(WarningNames.TYPE_ALIAS)
    fun `check long lambda property`() {
        lintMethod(
            """
                    | var emitWarn: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
                    | var emitWarn: (offset: Int, (T) -> Boolean) -> Unit
            """.trimMargin(),
            LintError(1, 16, ruleId, "${TYPE_ALIAS.warnText()} too long type reference", false),
            LintError(2, 16, ruleId, "${TYPE_ALIAS.warnText()} too long type reference", false)
        )
    }

    @Test
    @Tag(WarningNames.TYPE_ALIAS)
    fun `correct type length`() {
        lintMethod(
            """
                    | var emitWarn: Int
                    | val b = mutableMapOf<String, MutableList<String>>()
                    |
                    | fun foo(): MutableMap<String, MutableList<String>> {
                    | }
                    |
            """.trimMargin(),
            LintError(4, 13, ruleId, "${TYPE_ALIAS.warnText()} too long type reference", false)
        )
    }

    @Test
    @Tag(WarningNames.TYPE_ALIAS)
    fun `correct type length but with configuration`() {
        lintMethod(
            """
                    | var emitWarn: Int
                    | val flag: (T) -> Boolean
                    | val list: List<List<Int>>
                    |
            """.trimMargin(),
            LintError(3, 12, ruleId, "${TYPE_ALIAS.warnText()} too long type reference", false),
            rulesConfigList = rulesConfigListShortType
        )
    }

    @Test
    @Tag(WarningNames.TYPE_ALIAS)
    fun `should ignore inheritance`() {
        lintMethod(
            """
                    | class A : JsonResourceConfigReader<List<RulesConfig>>() {
                    |   fun foo() : JsonResourceConfigReader<List<RulesConfig>> {}
                    |   val q: JsonResourceConfigReader<List<RulesConfig>>? = null
                    |   fun goo() {
                    |       class B : JsonResourceConfigReader<List<RulesConfig>> {}
                    |   }
                    | }
            """.trimMargin(),
            LintError(2, 16, ruleId, "${TYPE_ALIAS.warnText()} too long type reference", false),
            LintError(3, 11, ruleId, "${TYPE_ALIAS.warnText()} too long type reference", false)
        )
    }

    @Test
    @Tag(WarningNames.TYPE_ALIAS)
    fun `check correct examle`() {
        lintMethod(
            """
                    |typealias jsonType = JsonResourceConfigReader<List<RulesConfig>>
                    |class A : JsonResourceConfigReader<List<RulesConfig>>() {
                    |
                    |   fun foo() : jsonType {}
                    |   val q: jsonType? = null
                    |   fun goo() {
                    |       class B : JsonResourceConfigReader<List<RulesConfig>> {}
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.TYPE_ALIAS)
    fun `check lazy property`() {
        lintMethod(
            """
                    |class A {
                    |   val q: List<Map<Int, Int>> by lazy  {
                    |       emptyList<Map<Int, Int>>()
                    |   }
                    |}
            """.trimMargin(),
            LintError(2, 11, ruleId, "${TYPE_ALIAS.warnText()} too long type reference", false),
            rulesConfigList = rulesConfigListShortType
        )
    }
}
