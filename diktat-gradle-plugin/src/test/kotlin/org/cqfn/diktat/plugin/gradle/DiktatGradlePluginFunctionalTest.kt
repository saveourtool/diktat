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

    @BeforeEach
    fun setUp() {
        testProjectDir.create()
        File("../examples/gradle-kotlin-dsl").copyRecursively(testProjectDir.root)
    }

    @AfterEach
    fun tearDown() {
        testProjectDir.delete()
    }

    @Test
    fun `should execute diktatCheck on default values`() {
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.root)
            .withArguments(DIKTAT_CHECK_TASK)
            .forwardOutput()
            .runCatching {
                buildAndFail()
            }

        Assertions.assertTrue(result.isSuccess) { "Running gradle returned exception ${result.exceptionOrNull()}" }

        val buildResult = result.getOrNull()!!
        val diktatCheckBuildResult = buildResult.task(":$DIKTAT_CHECK_TASK")
        requireNotNull(diktatCheckBuildResult)
        Assertions.assertEquals(TaskOutcome.FAILED, diktatCheckBuildResult.outcome)
        Assertions.assertTrue(
            buildResult.output.contains("[HEADER_MISSING_OR_WRONG_COPYRIGHT]")
        )
    }
}
