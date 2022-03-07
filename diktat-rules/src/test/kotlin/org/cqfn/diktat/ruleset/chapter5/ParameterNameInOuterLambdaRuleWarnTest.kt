package org.cqfn.diktat.ruleset.chapter5

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter5.ParameterNameInOuterLambdaRule
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class ParameterNameInOuterLambdaRuleWarnTest : LintTestBase(::ParameterNameInOuterLambdaRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:parameter-name-in-outer-lambda"
    private val rulesConfigList: List<RulesConfig> = listOf(
            RulesConfig(Warnings.PARAMETER_NAME_IN_OUTER_LAMBDA.name, true)
    )

    @Test
    @Tag(WarningNames.PARAMETER_NAME_IN_OUTER_LAMBDA)
    fun `lambda without parameters`() {
        lintMethod(
                """
                    |fun foo() {
                    |   val x = 10
                    |   val list = listOf(1, 2, 3, 4, 5)
                    |       .map { it + x }
                    |}
                """.trimMargin(),
                rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.PARAMETER_NAME_IN_OUTER_LAMBDA)
    fun `lambda without parameters in lazy initialization`() {
        lintMethod(
                """
                    |fun foo() {
                    |   private val allTestsFromResources: List<String> by lazy {
                    |       val fileUrl: URL? = javaClass.getResource("123")
                    |       val resource = fileUrl
                    |              ?.let { File(it.file) }
                    |       resource?.readLines() ?: Collections.emptyList()
                    |   }
                    |}
                """.trimMargin(),
                rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.PARAMETER_NAME_IN_OUTER_LAMBDA)
    fun `lambda doesn't expect parameters`() {
        lintMethod(
                """
                    |fun foo() {
                    |   private val allTestsFromResources: List<String> by lazy {
                    |     val fileUrl: URL? = javaClass.getResource("123")
                    |     listOf("1", "2", "3", "4", "5").removeAt(1)
                    |   }
                    |}
                """.trimMargin(),
                rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.PARAMETER_NAME_IN_OUTER_LAMBDA)
    fun `lambda in lambda (right case)`() {
        lintMethod(
                """
                    |fun foo() {
                    |   val list = listOf(listOf(1,2,3), listOf(4,5,6))
                    |       .map {l -> l.map {
                    |           val x = 0
                    |           val y = x + 1
                    |           val z = y + 1
                    |           println(it)
                    |           }
                    |       }
                    |   }
                """.trimMargin(),
                rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.PARAMETER_NAME_IN_OUTER_LAMBDA)
    fun `lambda in lambda (invalid case)`() {
        lintMethod(
                """
                    |fun foo() {
                    |   val list = listOf(listOf(1,2,3), listOf(4,5,6)).map {
                    |       it.map { k ->
                    |           val x = 0
                    |           val y = x + 1
                    |           val z = y + 1
                    |           println(k)
                    |       }
                    |   }
                    |}
                """.trimMargin(),
                LintError(2, 56, ruleId, "${Warnings.PARAMETER_NAME_IN_OUTER_LAMBDA.warnText()} lambda without arguments has inner lambda", false),
                rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.PARAMETER_NAME_IN_OUTER_LAMBDA)
    fun `lambda in lambda (invalid case #2)`() {
        lintMethod(
                """
                    |fun foo() {
                    |   val list = listOf(listOf("1","2","3"), listOf("4","5","6")).map { l ->
                    |       l.map { k ->
                    |           val x = 0
                    |           val y = x + 1
                    |           val z = y + 1
                    |           println(k)
                    |       }
                    |       l.map {
                    |           it.map { c ->
                    |               println(c)
                    |           }
                    |       }
                    |   }
                    |}
                """.trimMargin(),
                LintError(9, 14, ruleId, "${Warnings.PARAMETER_NAME_IN_OUTER_LAMBDA.warnText()} lambda without arguments has inner lambda", false),
                rulesConfigList = rulesConfigList
        )
    }
}