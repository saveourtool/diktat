package com.saveourtool.diktat.plugin.gradle

import com.saveourtool.diktat.plugin.gradle.DiktatGradlePlugin.Companion.DIKTAT_CHECK_TASK
import org.gradle.buildinit.plugins.internal.modifiers.BuildInitDsl
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class DiktatGradlePluginFunctionalTest {
    private val testProjectDir = TemporaryFolder()
    private lateinit var buildFile: File

    @BeforeEach
    fun setUp() {
        testProjectDir.create()
        val buildInitDsl = BuildInitDsl.KOTLIN
        createExampleProject(testProjectDir, File("../examples/gradle-kotlin-dsl"), buildInitDsl)
        buildFile = testProjectDir.root.resolve(buildInitDsl.fileNameFor("build"))
    }

    @AfterEach
    fun tearDown() {
        testProjectDir.delete()
    }

    @Test
    fun `should execute diktatCheck on default values`() {
        val result = runDiktat(testProjectDir, shouldSucceed = false)

        val diktatCheckBuildResult = result.task(":$DIKTAT_CHECK_TASK")
        requireNotNull(diktatCheckBuildResult)
        Assertions.assertEquals(TaskOutcome.FAILED, diktatCheckBuildResult.outcome)
        Assertions.assertTrue(
            result.output.contains("[FILE_NAME_MATCH_CLASS]")
        )
    }

    @Test
    fun `should have json reporter files`() {
        buildFile.appendText(
            """${System.lineSeparator()}
                diktat {
                    inputs { include("src/**/*.kt") }
                    reporter = "json"
                    output = "test.txt"
                }
            """.trimIndent()
        )
        val result = runDiktat(testProjectDir, shouldSucceed = false)

        val diktatCheckBuildResult = result.task(":$DIKTAT_CHECK_TASK")
        requireNotNull(diktatCheckBuildResult)
        Assertions.assertEquals(TaskOutcome.FAILED, diktatCheckBuildResult.outcome)
        val file = testProjectDir.root.walkTopDown().filter { it.name == "test.txt" }.first()
        Assertions.assertNotNull(file)
        Assertions.assertTrue(
                file.readLines().any { it.contains("[FILE_NAME_MATCH_CLASS]") }
        )
    }

    @Test
    fun `should execute diktatCheck with explicit inputs`() {
        buildFile.appendText(
            """${System.lineSeparator()}
                diktat {
                    inputs { include("src/**/*.kt") }
                }
            """.trimIndent()
        )
        val result = runDiktat(testProjectDir, shouldSucceed = false)

        val diktatCheckBuildResult = result.task(":$DIKTAT_CHECK_TASK")
        requireNotNull(diktatCheckBuildResult)
        Assertions.assertEquals(TaskOutcome.FAILED, diktatCheckBuildResult.outcome)
        Assertions.assertTrue(
            result.output.contains("[FILE_NAME_MATCH_CLASS]")
        )
    }

    @Test
    fun `should execute diktatCheck with excludes`() {
        buildFile.appendText(
            """${System.lineSeparator()}
                diktat {
                    inputs {
                        include("src/**/*.kt")
                        exclude("src/**/Test.kt")
                    }
                }
            """.trimIndent()
        )
        val result = runDiktat(testProjectDir, shouldSucceed = false)

        val diktatCheckBuildResult = result.task(":$DIKTAT_CHECK_TASK")
        requireNotNull(diktatCheckBuildResult)
        Assertions.assertEquals(TaskOutcome.FAILED, diktatCheckBuildResult.outcome)
    }

    @Test
    fun `should not run diktat with ktlint's default includes when no files match include patterns`() {
        buildFile.appendText(
            """${System.lineSeparator()}
                diktat {
                    inputs { include ("nonexistent-directory/src/**/*.kt") }
                }
            """.trimIndent()
        )
        val result = runDiktat(testProjectDir, arguments = listOf("--info"))

        val diktatCheckBuildResult = result.task(":$DIKTAT_CHECK_TASK")
        requireNotNull(diktatCheckBuildResult)
        Assertions.assertEquals(TaskOutcome.NO_SOURCE, diktatCheckBuildResult.outcome)
        Assertions.assertFalse(
            result.output.contains("Skipping diktat execution")
        )
    }

    @Test
    fun `should execute diktatCheck with gradle older than 6_4`() {
        val result = runDiktat(testProjectDir, shouldSucceed = false, arguments = listOf("--info")) {
            withGradleVersion("5.3")
        }

        val diktatCheckBuildResult = result.task(":$DIKTAT_CHECK_TASK")
        requireNotNull(diktatCheckBuildResult)
        Assertions.assertEquals(TaskOutcome.FAILED, diktatCheckBuildResult.outcome)
        Assertions.assertTrue(
            result.output.contains("[FILE_NAME_MATCH_CLASS]")
        )
    }

    @Test
    fun `should respect ignoreFailures setting`() {
        buildFile.appendText(
            """${System.lineSeparator()}
                diktat {
                    ignoreFailures = true
                }
            """.trimIndent()
        )
        val result = runDiktat(testProjectDir, shouldSucceed = true, arguments = listOf("--info"))

        val diktatCheckBuildResult = result.task(":$DIKTAT_CHECK_TASK")
        requireNotNull(diktatCheckBuildResult)
        Assertions.assertEquals(TaskOutcome.SUCCESS, diktatCheckBuildResult.outcome)
        Assertions.assertTrue(
            result.output.contains("[FILE_NAME_MATCH_CLASS]")
        )
    }
}
