package org.cqfn.diktat.plugin.gradle

import org.cqfn.diktat.plugin.gradle.DiktatGradlePlugin.Companion.DIKTAT_CHECK_TASK
import org.gradle.buildinit.plugins.internal.modifiers.BuildInitDsl
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class DiktatGradlePluginMultiprojectFunctionalTest {
    private val testProjectDir = TemporaryFolder()
    private lateinit var buildFile: File

    @BeforeEach
    fun setUp() {
        testProjectDir.create()
        val buildInitDsl = BuildInitDsl.KOTLIN
        File("../examples/gradle-kotlin-dsl-multiproject").copyRecursively(testProjectDir.root)
        buildFile = testProjectDir.root.resolve(buildInitDsl.fileNameFor("build"))
    }

    @AfterEach
    fun tearDown() {
        testProjectDir.delete()
    }

    @Test
    fun `should execute diktatCheck on default values in multiproject build`() {
        val result = runDiktat(testProjectDir, shouldSucceed = false)

        val diktatCheckRootResult = result.task(":$DIKTAT_CHECK_TASK")
        requireNotNull(diktatCheckRootResult)
        Assertions.assertEquals(TaskOutcome.NO_SOURCE, diktatCheckRootResult.outcome) {
            "Task for root project with empty sources should succeed"
        }

        val diktatCheckBackendResult = result.task(":backend:$DIKTAT_CHECK_TASK")
        requireNotNull(diktatCheckBackendResult)
        Assertions.assertEquals(TaskOutcome.FAILED, diktatCheckBackendResult.outcome)
        Assertions.assertTrue(
            result.output.contains("org.cqfn.diktat.example.gradle.multiproject")
        )
    }
}
