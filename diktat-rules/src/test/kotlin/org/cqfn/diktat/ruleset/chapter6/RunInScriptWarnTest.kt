package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.RUN_IN_SCRIPT
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter6.RunInScript
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class RunInScriptWarnTest : LintTestBase(::RunInScript) {
    private val ruleId: String = "$DIKTAT_RULE_SET_ID:run-script"
    private val rulesConfigList: List<RulesConfig> = listOf(
        RulesConfig(
            RUN_IN_SCRIPT.name, true,
            mapOf("possibleWrapper" to "custom, oneMore"))
    )

    @Test
    @Tag(WarningNames.RUN_IN_SCRIPT)
    fun `check simple example`() {
        lintMethod(
            """
                class A {}
                
                fun foo() {
                }
                
                diktat {
                }
                
                w.map { it -> it }
                
                println("hello")
                
                tasks.register("a") {
                    dependsOn("b")
                    doFirst {
                        generateCodeStyle(file("rootDir/guide"), file("rootDir/../wp"))
                    }
                }
                
            """.trimMargin(),
            LintError(9, 17, ruleId, "${RUN_IN_SCRIPT.warnText()} w.map { it -> it }", true),
            LintError(11, 17, ruleId, "${RUN_IN_SCRIPT.warnText()} println(\"hello\")", true),
            fileName = "src/main/kotlin/org/cqfn/diktat/Example.kts"
        )
    }

    @Test
    @Tag(WarningNames.RUN_IN_SCRIPT)
    fun `check correct examples`() {
        lintMethod(
            """
                run {
                    println("hello")
                }
                
                run{println("hello")}
                
                tasks.register("a") {
                }
                
            """.trimMargin(),
            fileName = "src/main/kotlin/org/cqfn/diktat/Example.kts"
        )
    }

    @Test
    @Tag(WarningNames.RUN_IN_SCRIPT)
    fun `check correct examples witrh config`() {
        lintMethod(
            """
                custom {
                    println("hello")
                }
                
                oneMore{println("hello")}
                
                another {
                    println("hello")
                }
            """.trimMargin(),
            LintError(7, 17, ruleId, "${RUN_IN_SCRIPT.warnText()} another {...", true),
            rulesConfigList = rulesConfigList,
            fileName = "src/main/kotlin/org/cqfn/diktat/Example.kts"
        )
    }
}
