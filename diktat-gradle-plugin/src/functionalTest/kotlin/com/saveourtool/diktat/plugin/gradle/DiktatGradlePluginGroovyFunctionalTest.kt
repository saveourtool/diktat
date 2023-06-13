package com.saveourtool.diktat.plugin.gradle

import org.gradle.buildinit.plugins.internal.modifiers.BuildInitDsl
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class DiktatGradlePluginGroovyFunctionalTest {
    private val testProjectDir = TemporaryFolder()
    private lateinit var buildFile: File

    @BeforeEach
    fun setUp() {
        testProjectDir.create()
        val buildInitDsl = BuildInitDsl.GROOVY
        createExampleProject(testProjectDir, File("../examples/gradle-groovy-dsl"), buildInitDsl)
        buildFile = testProjectDir.root.resolve(buildInitDsl.fileNameFor("build"))
    }

    @AfterEach
    fun tearDown() {
        testProjectDir.delete()
    }

    @Test
    fun `should execute diktatCheck with default values`() {
        val result = runDiktat(testProjectDir, shouldSucceed = false)

        assertDiktatExecuted(result)
    }

    @Test
    fun `should execute diktatCheck with explicit configuration`() {
        buildFile.appendText(
            """${System.lineSeparator()}
                diktat {
                    inputs { it.include("src/**/*.kt") }
                    reporter = "plain"
                    diktatConfigFile = file(rootDir.path + "/diktat-analysis.yml")
                }
            """.trimIndent()
        )

        val result = runDiktat(testProjectDir, shouldSucceed = false)

        assertDiktatExecuted(result)
    }
}
