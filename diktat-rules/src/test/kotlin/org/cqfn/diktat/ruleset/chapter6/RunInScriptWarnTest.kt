package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.ruleset.constants.Warnings.RUN_IN_SCRIPT
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter6.RunInScript
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import org.junit.jupiter.api.Test

class RunInScriptWarnTest : LintTestBase(::RunInScript) {
    private val ruleId: String = "$DIKTAT_RULE_SET_ID:run-script"

    @Test
    fun `check simple example`() {
        lintMethod(
            """
                class A {}
                
                fun foo() {
                }
                
                println("hello")
                
                tasks.register("a") {
                    dependsOn("b")
                    doFirst {
                        generateCodeStyle(file("rootDir/guide"), file("rootDir/../wp"))
                    }
                }
                
            """.trimMargin(),
            LintError(6, 17, ruleId, "${RUN_IN_SCRIPT.warnText()} println(\"hello\")", true),
            fileName = "src/main/kotlin/org/cqfn/diktat/Example.kts"
        )
    }

    @Test
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
}
