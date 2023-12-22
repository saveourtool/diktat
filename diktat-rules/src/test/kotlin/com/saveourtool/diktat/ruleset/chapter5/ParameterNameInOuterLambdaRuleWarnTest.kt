package com.saveourtool.diktat.ruleset.chapter5

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter5.ParameterNameInOuterLambdaRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class ParameterNameInOuterLambdaRuleWarnTest : LintTestBase(::ParameterNameInOuterLambdaRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${ParameterNameInOuterLambdaRule.NAME_ID}"
    private val rulesConfigList: List<RulesConfig> = listOf(
        RulesConfig(Warnings.PARAMETER_NAME_IN_OUTER_LAMBDA.name, true)
    )
    private val rulesConfigParameterNameInOuterLambda = listOf(
        RulesConfig(
            Warnings.PARAMETER_NAME_IN_OUTER_LAMBDA.name, true, mapOf(
            "strictMode" to "false"
        ))
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
            DiktatError(10, 8, ruleId, "${Warnings.PARAMETER_NAME_IN_OUTER_LAMBDA.warnText()} lambda without arguments has inner lambda", false),
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

    @Test
    @Tag(WarningNames.PARAMETER_NAME_IN_OUTER_LAMBDA)
    fun `lambdas at the same level`() {
        lintMethod(
            """
                |fun testA() {
                |   val overrideFunctions: List <Int> = emptyList()
                |   overrideFunctions.forEach { functionNameMap.compute(it.getIdentifierName()!!.text) { _, oldValue -> (oldValue ?: 0) + 1 } }
                |}
                """.trimMargin(),
            rulesConfigList = this.rulesConfigParameterNameInOuterLambda
        )
    }

    @Test
    @Tag(WarningNames.PARAMETER_NAME_IN_OUTER_LAMBDA)
    fun `lambdas at the same level 2`() {
        lintMethod(
            """
                |private fun isCheckNeeded(whiteSpace: PsiWhiteSpace) =
                |    whiteSpace.parent
                |        .node
                |        .elementType
                |        .let { it == VALUE_PARAMETER_LIST || it == VALUE_ARGUMENT_LIST } &&
                |            whiteSpace.siblings(forward = false, withItself = false).none { it is PsiWhiteSpace && it.textContains('\n') } &&
                |            whiteSpace.siblings(forward = true, withItself = false).any {
                |                it.node.elementType.run { this == VALUE_ARGUMENT || this == VALUE_PARAMETER }
                |            }
                """.trimMargin(),
            rulesConfigList = this.rulesConfigParameterNameInOuterLambda
        )
    }

    @Test
    @Tag(WarningNames.PARAMETER_NAME_IN_OUTER_LAMBDA)
    fun `lambdas at the same level 21`() {
        lintMethod(
            """
                |private fun isCheckNeeded(w: PsiWhiteSpace, h: PsiWhiteSpace) =
                |    w.let { it == VALUE_PARAMETER_LIST || it == VALUE_ARGUMENT_LIST } &&
                |            h.none { it is PsiWhiteSpace && it.textContains('\n') } &&
                |            h.any {
                |                it.node.elementType.run { this == VALUE_ARGUMENT || this == VALUE_PARAMETER }
                |            }
                """.trimMargin(),
            rulesConfigList = this.rulesConfigParameterNameInOuterLambda
        )
    }

    @Test
    @Tag(WarningNames.PARAMETER_NAME_IN_OUTER_LAMBDA)
    fun `lambdas at the same level 3`() {
        lintMethod(
            """
                |private fun checkBlankLineAfterKdoc(node: ASTNode) {
                |    commentType.forEach {
                |        val kdoc = node.getFirstChildWithType(it)
                |        kdoc?.treeNext?.let { nodeAfterKdoc ->
                |            if (nodeAfterKdoc.elementType == WHITE_SPACE && nodeAfterKdoc.numNewLines() > 1) {
                |                WRONG_NEWLINES_AROUND_KDOC.warnAndFix(configRules, emitWarn, isFixMode, "redundant blank line after ${'$'}{kdoc.text}", nodeAfterKdoc.startOffset, nodeAfterKdoc) {
                |                    nodeAfterKdoc.leaveOnlyOneNewLine()
                |                }
                |            }
                |        }
                |    }
                |}
                """.trimMargin(),
            rulesConfigList = this.rulesConfigParameterNameInOuterLambda
        )
    }

    @Test
    @Tag(WarningNames.PARAMETER_NAME_IN_OUTER_LAMBDA)
    fun `lambdas at the same level 4`() {
        lintMethod(
            """
                |private fun KSAnnotation.getArgumentValue(argumentName: String): String = arguments
                |    .singleOrNull { it.name?.asString() == argumentName }
                |    .let {
                |        requireNotNull(it) {
                |            "Not found ${'$'}argumentName in ${'$'}this"
                |        }
                |    }
                |    .value
                |    ?.let { it as? String }
                |    .let {
                |        requireNotNull(it) {
                |            "Not found a value for ${'$'}argumentName in ${'$'}this"
                |        }
                |    }
                """.trimMargin(),
            rulesConfigList = this.rulesConfigParameterNameInOuterLambda
        )
    }
}
