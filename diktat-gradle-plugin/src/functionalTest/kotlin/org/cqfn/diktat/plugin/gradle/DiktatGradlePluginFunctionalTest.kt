package org.cqfn.diktat.plugin.gradle

import org.cqfn.diktat.plugin.gradle.DiktatGradlePlugin.Companion.DIKTAT_CHECK_TASK
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
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
        File("../examples/gradle-kotlin-dsl").copyRecursively(testProjectDir.root)
        File(testProjectDir.root, "build.gradle.kts").delete()
        buildFile = testProjectDir.newFile("build.gradle.kts").apply {
            writeText(
                """
                plugins {
                    id("org.cqfn.diktat.diktat-gradle-plugin")
                }
                
                repositories {
                    mavenLocal()
                    mavenCentral()
                }
            """.trimIndent()
            )
        }
    }

    @AfterEach
    fun tearDown() {
        testProjectDir.delete()
    }

    @Test
    fun `should execute diktatCheck on default values`() {
        val result = runDiktat()

        val diktatCheckBuildResult = result.task(":$DIKTAT_CHECK_TASK")
        requireNotNull(diktatCheckBuildResult)
        Assertions.assertEquals(TaskOutcome.FAILED, diktatCheckBuildResult.outcome)
        Assertions.assertTrue(
            result.output.contains("[HEADER_MISSING_OR_WRONG_COPYRIGHT]")
        )
    }

    @Test
    fun `should execute diktatCheck with explicit inputs`() {
        buildFile.appendText(
            """${System.lineSeparator()}
                diktat {
                    inputs = files("src/**/*.kt")
                }
            """.trimIndent()
        )
        val result = runDiktat()

        val diktatCheckBuildResult = result.task(":$DIKTAT_CHECK_TASK")
        requireNotNull(diktatCheckBuildResult)
        Assertions.assertEquals(TaskOutcome.FAILED, diktatCheckBuildResult.outcome)
        Assertions.assertTrue(
            result.output.contains("[HEADER_MISSING_OR_WRONG_COPYRIGHT]")
        )
    }

    private fun runDiktat() = GradleRunner.create()
        .withProjectDir(testProjectDir.root)
        .withArguments(DIKTAT_CHECK_TASK)
        .withPluginClasspath()
        .forwardOutput()
        .runCatching {
            buildAndFail()
        }
        .also {
            require(it.isSuccess) { "Running gradle returned exception ${it.exceptionOrNull()}" }
        }
        .getOrNull()!!
}
