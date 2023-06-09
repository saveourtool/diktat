package com.saveourtool.diktat.plugin.gradle

import org.gradle.buildinit.plugins.internal.modifiers.BuildInitDsl
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
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
        assertDiktatExecuted(result, TaskOutcome.NO_SOURCE) {
            "Task for root project with empty sources should succeed"
        }
    }
}
