package org.cqfn.diktat.ruleset.chapter5

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.TOO_LONG_FUNCTION
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.FunctionLength
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class FunctionLengthWarnTest : LintTestBase(::FunctionLength) {

    private val ruleId = "$DIKTAT_RULE_SET_ID:function-length"

    private val rulesConfigList: List<RulesConfig> = listOf(
            RulesConfig(TOO_LONG_FUNCTION.name, true,
                    mapOf("maxFunctionLength" to "5"))
    )

    private val shortrulesConfigList: List<RulesConfig> = listOf(
            RulesConfig(TOO_LONG_FUNCTION.name, true,
                    mapOf("maxFunctionLength" to "2"))
    )

    @Test
    @Tag(WarningNames.TOO_LONG_FUNCTION)
    fun `check with all comment`() {
        lintMethod(
                """
                    |fun foo() {
                    |
                    |   //dkfgvdf
                    |   
                    |   /*
                    |    * jkgh
                    |    */
                    |    
                    |    /**
                    |     * dfkjvbhdfkjb
                    |     */
                    |
                    |   val x = 10
                    |   val y = 100
                    |   println(x)
                    |   val z = x + y
                    |   println(x)
                    |
                    |}
                """.trimMargin(),
                LintError(1, 1, ruleId, "${TOO_LONG_FUNCTION.warnText()} max length is 5, but you have 7", false),
                rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.TOO_LONG_FUNCTION)
    fun `less than max`() {
        lintMethod(
                """
                    |fun foo() {
                    |
                    |
                    |
                    |
                    |
                    |
                    |   val x = 10
                    |   val y = 100
                    |   println(x)
                    |
                    |}
                """.trimMargin(),
                rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.TOO_LONG_FUNCTION)
    fun `one line function`() {
        lintMethod(
                """
                    |fun foo(list: List<ASTNode>) = list.forEach {
                    |        if (it.element == "dfscv")
                    |           println()
                    |}
                """.trimMargin(),
                LintError(1, 1, ruleId, "${TOO_LONG_FUNCTION.warnText()} max length is 2, but you have 4", false),
                rulesConfigList = shortrulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.TOO_LONG_FUNCTION)
    fun `only empty lines`() {
        lintMethod(
                """
                    |fun foo(list: List<ASTNode>) {
                    |               
                    |           
                    |                           
                    |       
                    |}
                """.trimMargin(),
                rulesConfigList = shortrulesConfigList
        )
    }
}
