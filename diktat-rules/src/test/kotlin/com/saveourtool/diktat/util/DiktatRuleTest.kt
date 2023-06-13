package com.saveourtool.diktat.util

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.BLANK_LINE_BETWEEN_PROPERTIES
import com.saveourtool.diktat.ruleset.constants.Warnings.WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES
import com.saveourtool.diktat.ruleset.rules.chapter3.ClassLikeStructuresOrderRule

import com.saveourtool.diktat.api.DiktatError
import org.junit.jupiter.api.Test

class DiktatRuleTest : LintTestBase(::ClassLikeStructuresOrderRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${ClassLikeStructuresOrderRule.NAME_ID}"
    private val codeTemplate = """
        |class Example {
        |   private val FOO = 42
        |   private val log = LoggerFactory.getLogger(Example.javaClass)
        |   // blank line between property
        |   private val some = 2
        |}
    """.trimMargin()
    private val rulesConfigAllDisabled = listOf(
        RulesConfig(BLANK_LINE_BETWEEN_PROPERTIES.name, enabled = false),
        RulesConfig(WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES.name, enabled = false)
    )
    private val rulesConfigOneRuleIsEnabled = listOf(
        RulesConfig(BLANK_LINE_BETWEEN_PROPERTIES.name, enabled = true),
        RulesConfig(WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES.name, enabled = false)
    )

    @Test
    fun `check that if all inspections are disabled then rule won't run`() {
        lintMethod(codeTemplate, rulesConfigList = rulesConfigAllDisabled)
    }

    @Test
    fun `check that if one inspection is enabled then rule will run`() {
        lintMethod(codeTemplate,
            DiktatError(4, 4, ruleId, "${BLANK_LINE_BETWEEN_PROPERTIES.warnText()} some", true),
            rulesConfigList = rulesConfigOneRuleIsEnabled
        )
    }
}
