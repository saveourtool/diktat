package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.StringWarnings
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.files.IndentationRule
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

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
    @Tag(StringWarnings.WRONG_INDENTATION)
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
    @Tag(StringWarnings.WRONG_INDENTATION)
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
    @Tag(StringWarnings.WRONG_INDENTATION)
    fun `valid indentation - example 1`() {
        lintMethod(IndentationRule(),
                """
                    |class Example {
                    |    private val foo = 0
                    |    private val fuu =
                    |        0
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
    @Tag(StringWarnings.WRONG_INDENTATION)
    fun `parameters can be indented by 8 spaces - positive example`() {
        lintMethod(IndentationRule(),
                """
                    |class Example(
                    |        val field1: Type1,
                    |        val field2: Type2,
                    |        val field3: Type3
                    |) {
                    |    val e1 = Example(
                    |            t1,
                    |            t2,
                    |            t3
                    |    )
                    |    
                    |    val e2 = Example(t1, t2,
                    |            t3
                    |    )
                    |}
                    |
                """.trimMargin(),
                rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(StringWarnings.WRONG_INDENTATION)
    fun `parameters can be aligned - positive example`() {
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
    }

    @Test
    @Tag(StringWarnings.WRONG_INDENTATION)
    fun `parameters can be aligned`() {
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
    @Tag(StringWarnings.WRONG_INDENTATION)
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
    @Tag(StringWarnings.WRONG_INDENTATION)
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
    @Tag(StringWarnings.WRONG_INDENTATION)
    fun `assignment increases indentation if followed by newline`() {
        lintMethod(IndentationRule(),
                """
                    |fun <T> foo(list: List<T>) {
                    |    val a = list.filter { 
                    |        predicate(it)
                    |    }
                    |    
                    |    val b =
                    |        list.filter { 
                    |            predicate(it)
                    |        }
                    |}
                    |
                """.trimMargin()
        )
    }

    @Test
    @Tag(StringWarnings.WRONG_INDENTATION)
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

    @Test
    @Tag(StringWarnings.WRONG_INDENTATION)
    fun `dot call increases indentation`() {
        lintMethod(IndentationRule(),
                """
                    |fun foo() {
                    |    Integer
                    |        .valueOf(2).also {
                    |            println(it)
                    |        }
                    |        ?.also {
                    |            println("Also with safe access")
                    |        }
                    |}
                    |
                """.trimMargin()
        )
    }

    @Test
    @Tag(StringWarnings.WRONG_INDENTATION)
    @Disabled("todo")
    fun `opening braces should not increase indent when placed on the same line`() {
        lintMethod(IndentationRule(),
                """
                    |fun foo() {
                    |    consume(Example(
                    |            t1, t2, t3)
                    |    )
                    |}
                    |
                """.trimMargin()
        )
    }

    private fun warnText(expected: Int, actual: Int) = "${WRONG_INDENTATION.warnText()} expected $expected but was $actual"
}
