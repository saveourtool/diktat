package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.EmptyBlock
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Test

class EmptyBlockWarnTest {

    private val ruleId = "$DIKTAT_RULE_SET_ID:empty-block-structure"

    private val rulesConfigListIgnoreEmptyBlock: List<RulesConfig> = listOf(
            RulesConfig(Warnings.EMPTY_BLOCK_STRUCTURE_ERROR.name, true,
                    mapOf("styleEmptyBlockWithNewline" to "False"))
    )

    private val rulesConfigListEmptyBlockExist: List<RulesConfig> = listOf(
            RulesConfig(Warnings.EMPTY_BLOCK_STRUCTURE_ERROR.name, true,
                    mapOf("allowEmptyBlocks" to "True"))
    )

    @Test
    fun `check if expression with empty else block`() {
        lintMethod(EmptyBlock(),
                """
                    |fun foo() {
                    |    if (x < -5) {
                    |       goo()
                    |    }
                    |    else {
                    |    }
                    |}
                """.trimMargin(),
                LintError(5,10,ruleId,"${Warnings.EMPTY_BLOCK_STRUCTURE_ERROR.warnText()} empty blocks are forbidden unless it is function with override keyword", false)
        )
    }

    @Test
    fun `check if expression with empty else block with config`() {
        lintMethod(EmptyBlock(),
                """
                    |fun foo() {
                    |    if (x < -5) {
                    |       goo()
                    |    }
                    |    else {}
                    |}
                """.trimMargin(),
                LintError(5,10,ruleId,"${Warnings.EMPTY_BLOCK_STRUCTURE_ERROR.warnText()} empty blocks are forbidden unless it is function with override keyword", false),
                rulesConfigList = rulesConfigListIgnoreEmptyBlock
        )
    }

    @Test
    fun `check fun expression with empty block and override annotation`() {
        lintMethod(EmptyBlock(),
                """
                    |override fun foo() {
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `check if expression with empty else block but with permission to use empty block`() {
        lintMethod(EmptyBlock(),
                """
                    |fun foo() {
                    |    if (x < -5) {
                    |       goo()
                    |    }
                    |    else {
                    |    }
                    |}
                """.trimMargin(),
                rulesConfigList = rulesConfigListEmptyBlockExist
        )
    }
}
