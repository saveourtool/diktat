package com.saveourtool.diktat.ruleset.chapter6

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter6.classes.SingleInitRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class SingleInitRuleWarnTest : LintTestBase(::SingleInitRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${SingleInitRule.NAME_ID}"

    @Test
    @Tag(WarningNames.MULTIPLE_INIT_BLOCKS)
    fun `should allow single init block`() {
        lintMethod(
            """
                |class Example {
                |    init { println("Lorem ipsum") }
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.MULTIPLE_INIT_BLOCKS)
    fun `should forbid multiple init blocks`() {
        lintMethod(
            """
                |class Example {
                |    init { println("Lorem ipsum") }
                |
                |    val foo = 0
                |
                |    init { println("Dolor sit amet") }
                |}
            """.trimMargin(),
            DiktatError(1, 15, ruleId, "${Warnings.MULTIPLE_INIT_BLOCKS.warnText()} in class <Example> found 2 `init` blocks", true)
        )
    }

    @Test
    @Tag(WarningNames.MULTIPLE_INIT_BLOCKS)
    fun `should warn if properties are assigned in init block`() {
        lintMethod(
            """
                |class A(baseUrl: String, hardUrl: String) {
                |    private val customUrl: String
                |    init {
                |        customUrl = "${'$'}baseUrl/myUrl"
                |    }
                |}
            """.trimMargin(),
            DiktatError(3, 5, ruleId, "${Warnings.MULTIPLE_INIT_BLOCKS.warnText()} `init` block has assignments that can be moved to declarations", true)
        )
    }

    @Test
    @Tag(WarningNames.MULTIPLE_INIT_BLOCKS)
    fun `shouldn't warn if property are assigned on property in init block`() {
        lintMethod(
            """
                |class A {
                |    var a: String
                |    var c: String
                |
                |    init {
                |        val b: String = "a"
                |        a = b
                |
                |        val d: String = "c"
                |        c = foo(d)
                |    }
                |}
            """.trimMargin()
        )
    }
}
