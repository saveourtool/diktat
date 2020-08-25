package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.common.config.rules.RulesConfig
import generated.WarningNames
import org.cqfn.diktat.ruleset.constants.Warnings.EMPTY_BLOCK_STRUCTURE_ERROR
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.EmptyBlock
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class EmptyBlockWarnTest {

    private val ruleId = "$DIKTAT_RULE_SET_ID:empty-block-structure"

    private val rulesConfigListIgnoreEmptyBlock: List<RulesConfig> = listOf(
            RulesConfig(EMPTY_BLOCK_STRUCTURE_ERROR.name, true,
                    mapOf("styleEmptyBlockWithNewline" to "False"))
    )

    private val rulesConfigListEmptyBlockExist: List<RulesConfig> = listOf(
            RulesConfig(EMPTY_BLOCK_STRUCTURE_ERROR.name, true,
                    mapOf("allowEmptyBlocks" to "True"))
    )

    @Test
    @Tag(WarningNames.EMPTY_BLOCK_STRUCTURE_ERROR)
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
                LintError(5, 10, ruleId, "${EMPTY_BLOCK_STRUCTURE_ERROR.warnText()} empty blocks are forbidden unless it is function with override keyword", true)
        )
    }

    @Test
    @Tag(WarningNames.EMPTY_BLOCK_STRUCTURE_ERROR)
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
                LintError(5, 10, ruleId, "${EMPTY_BLOCK_STRUCTURE_ERROR.warnText()} empty blocks are forbidden unless it is function with override keyword", true),
                rulesConfigList = rulesConfigListIgnoreEmptyBlock
        )
    }

    @Test
    @Tag(WarningNames.EMPTY_BLOCK_STRUCTURE_ERROR)
    fun `check fun expression with empty block and override annotation`() {
        lintMethod(EmptyBlock(),
                """
                    |override fun foo() {
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.EMPTY_BLOCK_STRUCTURE_ERROR)
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

    @Test
    fun `check if expression without block`() {
        lintMethod(EmptyBlock(),
                """
                    |fun foo() {
                    |   if (node.treeParent != null) return
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `check if-else expression without block`() {
        lintMethod(EmptyBlock(),
                """
                    |fun foo() {
                    |   if (node.treeParent != null) return else println(true)
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `check for expresion and while without block`() {
        lintMethod(EmptyBlock(),
                """
                    |fun foo() {
                    |   for(x in 0..10) println(x)
                    |   val x = 10
                    |   while (x > 0)
                    |       --x
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `check empty lambda`() {
        lintMethod(EmptyBlock(),
                """
                    |fun foo() {
                    |   val y = listOf<Int>().map {} 
                    |}
                """.trimMargin(),
                LintError(2, 30, ruleId, "${EMPTY_BLOCK_STRUCTURE_ERROR.warnText()} empty blocks are forbidden unless it is function with override keyword", true)
        )
    }

    @Test
    fun `check empty lambda with config`() {
        lintMethod(EmptyBlock(),
                """
                    |fun foo() {
                    |   val y = listOf<Int>().map {} 
                    |}
                """.trimMargin(),
                LintError(2, 30, ruleId, "${EMPTY_BLOCK_STRUCTURE_ERROR.warnText()} different style for empty block", true),
                rulesConfigList = rulesConfigListEmptyBlockExist
        )
    }
}
