package com.saveourtool.diktat.ruleset.chapter6

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings.RUN_IN_SCRIPT
import com.saveourtool.diktat.ruleset.rules.chapter6.RunInScript
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class RunInScriptWarnTest : LintTestBase(::RunInScript) {
    private val ruleId: String = "$DIKTAT_RULE_SET_ID:${RunInScript.NAME_ID}"

    @Test
    @Tag(WarningNames.RUN_IN_SCRIPT)
    fun `check simple example`(@TempDir tempDir: Path) {
        lintMethodWithFile(
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
            tempDir = tempDir,
            fileName = "src/main/kotlin/com/saveourtool/diktat/Example.kts",
            DiktatError(10, 17, ruleId, "${RUN_IN_SCRIPT.warnText()} foo/*df*/()", true),
            DiktatError(12, 17, ruleId, "${RUN_IN_SCRIPT.warnText()} foo( //dfdg...", true),
            DiktatError(15, 17, ruleId, "${RUN_IN_SCRIPT.warnText()} println(\"hello\")", true),
            DiktatError(17, 17, ruleId, "${RUN_IN_SCRIPT.warnText()} w.map { it -> it }", true),
            DiktatError(19, 17, ruleId, "${RUN_IN_SCRIPT.warnText()} tasks.register(\"a\") {...", true)
        )
    }

    @Test
    @Tag(WarningNames.RUN_IN_SCRIPT)
    fun `check correct examples`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                run {
                    println("hello")
                }

                run{println("hello")}

                val task = tasks.register("a") {
                }

            """.trimMargin(),
            tempDir = tempDir,
            fileName = "src/main/kotlin/com/saveourtool/diktat/Example.kts"
        )
    }

    @Test
    @Tag(WarningNames.RUN_IN_SCRIPT)
    fun `check correct with custom wrapper`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                custom {
                    println("hello")
                }

                oneMore{println("hello")}

                another {
                    println("hello")
                }
            """.trimMargin(),
            tempDir = tempDir,
            fileName = "src/main/kotlin/com/saveourtool/diktat/Example.kts"
        )
    }

    @Test
    @Tag(WarningNames.RUN_IN_SCRIPT)
    fun `check gradle file`(@TempDir tempDir: Path) {
        lintMethodWithFile(
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
            tempDir = tempDir,
            fileName = "src/main/kotlin/com/saveourtool/diktat/builds.gradle.kts",
            DiktatError(6, 17, ruleId, "${RUN_IN_SCRIPT.warnText()} if(true) {...", true)
        )
    }

    @Test
    @Tag(WarningNames.RUN_IN_SCRIPT)
    fun `check gradle script with eq expression`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                version = "0.1.0-SNAPSHOT"

                diktat {}

                diktat({})

                foo/*df*/()

                foo().goo()
            """.trimMargin(),
            tempDir = tempDir,
            fileName = "src/main/kotlin/com/saveourtool/diktat/builds.gradle.kts"
        )
    }

    @Test
    @Tag(WarningNames.RUN_IN_SCRIPT)
    fun `check kts script with eq expression`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                version = "0.1.0-SNAPSHOT"

                diktat {}

                diktat({})

                foo/*df*/()
            """.trimMargin(),
            tempDir = tempDir,
            fileName = "src/main/kotlin/com/saveourtool/diktat/Example.kts",
            DiktatError(1, 17, ruleId, "${RUN_IN_SCRIPT.warnText()} version = \"0.1.0-SNAPSHOT\"", true),
            DiktatError(7, 17, ruleId, "${RUN_IN_SCRIPT.warnText()} foo/*df*/()", true)
        )
    }
}
