package org.cqfn.diktat.ruleset.chapter5

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter5.ParameterNameInOuterLambdaRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.ruleset.rules.chapter6.classes.AbstractClassesRule
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class ParameterNameInOuterLambdaRuleWarnTest : LintTestBase(::ParameterNameInOuterLambdaRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${ParameterNameInOuterLambdaRule.NAME_ID}"
    private val rulesConfigList: List<RulesConfig> = listOf(
        RulesConfig(Warnings.PARAMETER_NAME_IN_OUTER_LAMBDA.name, true)
    )

    @Test
    @Tag(WarningNames.PARAMETER_NAME_IN_OUTER_LAMBDA)
    fun `lambda has specific parameter`() {
        lintMethod(
            """
                    |fun foo(lambda: (s: String) -> Unit) {
                    |   lambda("foo")
                    |}
                    |
                    |fun test() {
                    |   foo { s ->
                    |       println(s)
                    |   }
                    |}
                """.trimMargin(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.PARAMETER_NAME_IN_OUTER_LAMBDA)
    fun `lambda has implicit parameter`() {
        lintMethod(
            """
                    |fun foo(lambda: (s: String) -> Unit) {
                    |   lambda("foo")
                    |}
                    |
                    |fun test() {
                    |   foo {
                    |       println(it)
                    |   }
                    |}
                """.trimMargin(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.PARAMETER_NAME_IN_OUTER_LAMBDA)
    fun `outer lambda and inner lambda have specific parameter`() {
        lintMethod(
            """
                    |fun bar(lambda: (s: String) -> Unit) {
                    |   lambda("bar")
                    |}
                    |
                    |fun foo(lambda: (s: String) -> Unit) {
                    |   lambda("foo")
                    |}
                    |
                    |fun test() {
                    |   foo { f ->
                    |       bar { b ->
                    |           println(f + " -> " + b)
                    |       }
                    |   }
                    |}
                """.trimMargin(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.PARAMETER_NAME_IN_OUTER_LAMBDA)
    fun `outer lambda has specific parameter but inner lambda has implicit parameter`() {
        lintMethod(
            """
                    |fun bar(lambda: (s: String) -> Unit) {
                    |   lambda("bar")
                    |}
                    |
                    |fun foo(lambda: (s: String) -> Unit) {
                    |   lambda("foo")
                    |}
                    |
                    |fun test() {
                    |   foo { f ->
                    |       bar {
                    |           println(f + " -> " + it)
                    |       }
                    |   }
                    |}
                """.trimMargin(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.PARAMETER_NAME_IN_OUTER_LAMBDA)
    fun `outer lambda has implicit parameter but inner lambda has specific parameter`() {
        lintMethod(
            """
                    |fun bar(lambda: (s: String) -> Unit) {
                    |   lambda("bar")
                    |}
                    |
                    |fun foo(lambda: (s: String) -> Unit) {
                    |   lambda("foo")
                    |}
                    |
                    |fun test() {
                    |   foo {
                    |       bar { b ->
                    |           println(it + " -> " + b)
                    |       }
                    |   }
                    |}
                """.trimMargin(),
            LintError(10, 8, ruleId, "${Warnings.PARAMETER_NAME_IN_OUTER_LAMBDA.warnText()} lambda without arguments has inner lambda", false),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.PARAMETER_NAME_IN_OUTER_LAMBDA)
    fun `outer lambda has implicit parameter but inner lambda has no parameter`() {
        lintMethod(
            """
                    |fun bar(lambda: () -> Unit) {
                    |   lambda()
                    |}
                    |
                    |fun foo(lambda: (s: String) -> Unit) {
                    |   lambda("foo")
                    |}
                    |
                    |fun test() {
                    |   foo {
                    |       bar {
                    |           println(it + " -> bar")
                    |       }
                    |   }
                    |}
                """.trimMargin(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.PARAMETER_NAME_IN_OUTER_LAMBDA)
    fun `outer lambda has no parameter but inner lambda has implicit parameter`() {
        lintMethod(
            """
                    |fun bar(lambda: (s: String) -> Unit) {
                    |   lambda("bar")
                    |}
                    |
                    |fun foo(lambda: () -> Unit) {
                    |   lambda()
                    |}
                    |
                    |fun test() {
                    |   foo {
                    |       bar {
                    |           println("foo -> " + it)
                    |       }
                    |   }
                    |}
                """.trimMargin(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.PARAMETER_NAME_IN_OUTER_LAMBDA)
    fun `shouldn't warn if nested lambda has explicit it`() {
        lintMethod(
            """
                |fun test() {
                |    run {
                |        l.map { it ->
                |            println(it)
                |        }
                |    }
                |}
            """.trimMargin(),
            rulesConfigList = rulesConfigList
        )
    }
}
