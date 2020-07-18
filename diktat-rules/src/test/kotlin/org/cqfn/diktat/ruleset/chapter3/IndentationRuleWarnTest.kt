package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.files.IndentationRule
import org.cqfn.diktat.ruleset.utils.lintMethod
import org.junit.Test

class IndentationRuleWarnTest {
    private val ruleId = "$DIKTAT_RULE_SET_ID:indentation"
    private val rulesConfigList = listOf(
            RulesConfig(WRONG_INDENTATION.name, true,
                    mapOf(
                            "extendedIndentOfParameters" to "true",
                            "alignedParameters" to "true"
                    )
            )
    )

    @Test
    fun `should warn if tabs are used in indentation`() {
        lintMethod(IndentationRule(),
                """
                    |class Example {
                    |${"\t"}val zero = 0
                    |}
                    |
                """.trimMargin(),
                LintError(2, 1, ruleId, "${WRONG_INDENTATION.warnText()} tabs are not allowed for indentation", true)
        )
    }

    @Test
    fun `should warn if indent size is not 4 spaces`() {
        lintMethod(IndentationRule(),
                """
                    |class Example {
                    |   val zero = 0
                    |}
                    |
                """.trimMargin(),
                LintError(2, 1, ruleId, warnText(4, 3), true)
        )
    }

    @Test
    fun `valid indentation - example 1`() {
        lintMethod(IndentationRule(),
                """
                    |class Example {
                    |    private val foo = 0
                    |    
                    |    fun bar() {
                    |        if (foo > 0) {
                    |            baz()
                    |        } else {
                    |            bazz()
                    |        }
                    |        return foo
                    |    }
                    |}
                    |
                """.trimMargin()
        )
    }

    @Test
    fun `parameters can be indented by 8 spaces`() {
        lintMethod(IndentationRule(),
                """
                    |class Example(
                    |        val field1: Type1,
                    |        val field2: Type2,
                    |        val field3: Type3
                    |) {
                    |}
                    |
                """.trimMargin(),
                rulesConfigList = rulesConfigList
        )
    }

    @Test
    fun `parameters can be aligned`() {
        lintMethod(IndentationRule(),
                """
                    |class Example(val field1: Type1,
                    |              val field2: Type2,
                    |              val field3: Type3) {
                    |}
                    |
                """.trimMargin(),
                rulesConfigList = rulesConfigList
        )

        lintMethod(IndentationRule(),
                """
                    |class Example(
                    |              val field1: Type1,
                    |              val field2: Type2,
                    |              val field3: Type3) {
                    |}
                    |
                """.trimMargin(),
                LintError(2, 1, ruleId, warnText(8, 14), true),
                LintError(3, 1, ruleId, warnText(8, 14), true),
                LintError(4, 1, ruleId, warnText(8, 14), true),
                rulesConfigList = rulesConfigList
        )
    }

    @Test
    fun `lines split by operator can be indented by 8 spaces`() {
        lintMethod(IndentationRule(),
                """
                    |fun foo(a: Int, b: Int) {
                    |    return 2 * a +
                    |            b
                    |}
                    |
                """.trimMargin(),
                rulesConfigList = rulesConfigList
        )
    }

    @Test
    fun `should check indentation in KDocs - positive example`() {
        lintMethod(IndentationRule(),
                """
                    |/**
                    | * Lorem ipsum
                    | */
                    |class Example {
                    |}
                    |
                """.trimMargin()
        )
    }

    @Test
    fun `should check indentation in KDocs`() {
        lintMethod(IndentationRule(),
                """
                    |/**
                    |* Lorem ipsum
                    |*/
                    |class Example {
                    |}
                    |
                """.trimMargin(),
                LintError(2, 1, ruleId, warnText(1, 0), true),
                LintError(3, 1, ruleId, warnText(1, 0), true)
        )
    }

    private fun warnText(expected: Int, actual: Int) = "${WRONG_INDENTATION.warnText()} expected $expected but was $actual"
}
