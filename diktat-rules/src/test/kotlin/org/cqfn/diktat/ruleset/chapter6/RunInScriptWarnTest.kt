package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.ruleset.constants.Warnings.RUN_IN_SCRIPT
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter6.RunInScript
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class RunInScriptWarnTest : LintTestBase(::RunInScript) {
    private val ruleId: String = "$DIKTAT_RULE_SET_ID:${RunInScript.nameId}"

    @Test
    @Tag(WarningNames.RUN_IN_SCRIPT)
    fun `check simple example`() {
        lintMethod(
            """
                class A {}
                
                fun foo() {
                }
                
                diktat {}
                
                diktat({})

                foo/*df*/()

                foo( //dfdg
                    10
                )
                println("hello")
                
                w.map { it -> it }
                
                tasks.register("a") {
                    dependsOn("b")
                    doFirst {
                        generateCodeStyle(file("rootDir/guide"), file("rootDir/../wp"))
                    }
                }
                
            """.trimMargin(),
            LintError(10, 17, ruleId, "${RUN_IN_SCRIPT.warnText()} foo/*df*/()", true),
            LintError(12, 17, ruleId, "${RUN_IN_SCRIPT.warnText()} foo( //dfdg...", true),
            LintError(15, 17, ruleId, "${RUN_IN_SCRIPT.warnText()} println(\"hello\")", true),
            LintError(17, 17, ruleId, "${RUN_IN_SCRIPT.warnText()} w.map { it -> it }", true),
            LintError(19, 17, ruleId, "${RUN_IN_SCRIPT.warnText()} tasks.register(\"a\") {...", true),
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
                
                val task = tasks.register("a") {
                }
                
            """.trimMargin(),
            fileName = "src/main/kotlin/org/cqfn/diktat/Example.kts"
        )
    }

    @Test
    @Tag(WarningNames.RUN_IN_SCRIPT)
    fun `check correct with custom wrapper`() {
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
            fileName = "src/main/kotlin/org/cqfn/diktat/Example.kts"
        )
    }

    @Test
    @Tag(WarningNames.RUN_IN_SCRIPT)
    fun `check gradle file`() {
        lintMethod(
            """
                class A {}
                
                fun foo() {
                }
                
                if(true) {
                    goo()
                }
                
                diktat {}
                
                diktat({})

                foo/*df*/()

                foo( //dfdg
                    10
                )
                println("hello")
                
                w.map { it -> it }
                
                (tasks.register("a") {
                    dependsOn("b")
                    doFirst {
                        generateCodeStyle(file("rootDir/guide"), file("rootDir/../wp"))
                    }
                })
                
            """.trimMargin(),
            LintError(6, 17, ruleId, "${RUN_IN_SCRIPT.warnText()} if(true) {...", true),
            fileName = "src/main/kotlin/org/cqfn/diktat/builds.gradle.kts"
        )
    }

    @Test
    @Tag(WarningNames.RUN_IN_SCRIPT)
    fun `check gradle script with eq expression`() {
        lintMethod(
            """
                version = "0.1.0-SNAPSHOT"
                
                diktat {}
                
                diktat({})

                foo/*df*/()
                
                foo().goo()
            """.trimMargin(),
            fileName = "src/main/kotlin/org/cqfn/diktat/builds.gradle.kts"
        )
    }

    @Test
    @Tag(WarningNames.RUN_IN_SCRIPT)
    fun `check kts script with eq expression`() {
        lintMethod(
            """
                version = "0.1.0-SNAPSHOT"
                
                diktat {}
                
                diktat({})

                foo/*df*/()
            """.trimMargin(),
            LintError(1, 17, ruleId, "${RUN_IN_SCRIPT.warnText()} version = \"0.1.0-SNAPSHOT\"", true),
            LintError(7, 17, ruleId, "${RUN_IN_SCRIPT.warnText()} foo/*df*/()", true),
            fileName = "src/main/kotlin/org/cqfn/diktat/Example.kts"
        )
    }
}
