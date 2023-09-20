package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.EMPTY_BLOCK_STRUCTURE_ERROR
import com.saveourtool.diktat.ruleset.rules.chapter3.EmptyBlock
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class EmptyBlockWarnTest : LintTestBase(::EmptyBlock) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${EmptyBlock.NAME_ID}"
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
        lintMethod(
            """
                    |fun foo() {
                    |    if (x < -5) {
                    |       goo()
                    |    }
                    |    else {
                    |    }
                    |}
            """.trimMargin(),
            DiktatError(5, 10, ruleId, "${EMPTY_BLOCK_STRUCTURE_ERROR.warnText()} empty blocks are forbidden unless it is function with override keyword", false)
        )
    }

    @Test
    @Tag(WarningNames.EMPTY_BLOCK_STRUCTURE_ERROR)
    fun `check if WHEN element node text is empty`() {
        lintMethod(
            """
                    |fun foo() {
                    |    when (a) {
                    |    }
                    |}
            """.trimMargin(),
            DiktatError(2, 5, ruleId, "${EMPTY_BLOCK_STRUCTURE_ERROR.warnText()} empty blocks are forbidden unless it is function with override keyword", false)
        )
    }

    @Test
    @Tag(WarningNames.EMPTY_BLOCK_STRUCTURE_ERROR)
    fun `check if expression with empty else block with config`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if (x < -5) {
                    |       goo()
                    |    }
                    |    else {}
                    |}
            """.trimMargin(),
            DiktatError(5, 10, ruleId, "${EMPTY_BLOCK_STRUCTURE_ERROR.warnText()} empty blocks are forbidden unless it is function with override keyword", false),
            rulesConfigList = rulesConfigListIgnoreEmptyBlock
        )
    }

    @Test
    @Tag(WarningNames.EMPTY_BLOCK_STRUCTURE_ERROR)
    fun `check fun expression with empty block and override annotation`() {
        lintMethod(
            """
                    |override fun foo() {
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.EMPTY_BLOCK_STRUCTURE_ERROR)
    fun `check if expression with empty else block but with permission to use empty block`() {
        lintMethod(
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
        lintMethod(
            """
                    |fun foo() {
                    |   if (node.treeParent != null) return
                    |}
            """.trimMargin()
        )
    }

    @Test
    fun `empty lambda`() {
        lintMethod(
            """
                    |fun foo() {
                    |   run { }
                    |}
            """.trimMargin(),
            rulesConfigList = rulesConfigListEmptyBlockExist
        )
    }

    @Test
    fun `check if-else expression without block`() {
        lintMethod(
            """
                    |fun foo() {
                    |   if (node.treeParent != null) return else println(true)
                    |}
            """.trimMargin()
        )
    }

    @Test
    fun `check for expresion and while without block`() {
        lintMethod(
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
    @Tag(WarningNames.EMPTY_BLOCK_STRUCTURE_ERROR)
    fun `check empty lambda with config`() {
        lintMethod(
            """
                |fun foo() {
                |   val y = listOf<Int>().map {
                |
                |   }
                |}
            """.trimMargin(),
            DiktatError(2, 30, ruleId, "${EMPTY_BLOCK_STRUCTURE_ERROR.warnText()} do not put newlines in empty lambda", true),
            rulesConfigList = rulesConfigListEmptyBlockExist
        )
    }

    @Test
    fun `check empty lambda`() {
        lintMethod(
            """
                    |fun foo() {
                    |   val y = listOf<Int>().map { }
                    |}
            """.trimMargin(),
            DiktatError(2, 30, ruleId, "${EMPTY_BLOCK_STRUCTURE_ERROR.warnText()} empty blocks are forbidden unless it is function with override keyword", false)
        )
    }

    @Test
    fun `should not trigger on anonymous SAM classes #1`() {
        lintMethod(
            """
                |fun foo() {
                |   val proj = some.create(
                |       Disposable {},
                |       config
                |   ).project
                |}
            """.trimMargin(),
            rulesConfigList = rulesConfigListEmptyBlockExist
        )
    }

    @Test
    fun `should not trigger on anonymous SAM classes #2`() {
        lintMethod(
            """
                |fun foo() {
                |   val some = Disposable {}
                |   val proj = some.create(
                |       some,
                |       config
                |   ).project
                |}
            """.trimMargin(),
            rulesConfigList = rulesConfigListEmptyBlockExist
        )
    }

    @Test
    @Tag(WarningNames.EMPTY_BLOCK_STRUCTURE_ERROR)
    fun `should trigger on implementing anonymous SAM classes`() {
        lintMethod(
            """
                |interface A
                |
                |val some = object : A{}
            """.trimMargin(),
            DiktatError(3, 22, ruleId, "${EMPTY_BLOCK_STRUCTURE_ERROR.warnText()} different style for empty block", true),
            rulesConfigList = rulesConfigListEmptyBlockExist
        )
    }

    @Test
    @Tag(WarningNames.EMPTY_BLOCK_STRUCTURE_ERROR)
    fun `should not trigger on empty lambdas as a functions`() {
        lintMethod(
            """
                |fun foo(bar: () -> Unit = {})
                |
                |class Some {
                |   fun bar() {
                |       A({})
                |   }
                |}
            """.trimMargin(),
            rulesConfigList = rulesConfigListEmptyBlockExist
        )
    }

    @Test
    @Tag(WarningNames.EMPTY_BLOCK_STRUCTURE_ERROR)
    fun `should not trigger on empty lambdas as a functions #2`() {
        lintMethod(
            """
                |fun some() {
                |    val project = KotlinCoreEnvironment.createForProduction(
                |       { },
                |       compilerConfiguration,
                |       EnvironmentConfigFiles.JVM_CONFIG_FILES
                |    ).project
                |}
            """.trimMargin(),
            rulesConfigList = rulesConfigListEmptyBlockExist
        )
    }

    @Test
    @Tag(WarningNames.EMPTY_BLOCK_STRUCTURE_ERROR)
    fun `should not trigger on KotlinLogging logger`() {
        lintMethod(
            """
                |import io.github.oshai.kotlinlogging.KotlinLogging
                |
                |fun some() {
                |    val log = KotlinLogging.logger {}
                |    log.info { "test" }
                |}
            """.trimMargin(),
        )
    }
}
