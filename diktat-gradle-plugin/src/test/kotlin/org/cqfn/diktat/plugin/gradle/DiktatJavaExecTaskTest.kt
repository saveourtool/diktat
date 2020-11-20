package org.cqfn.diktat.plugin.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class DiktatJavaExecTaskTest {
    private val projectBuilder = ProjectBuilder.builder()
    private lateinit var project: Project

    @BeforeEach
    fun setUp() {
        project = projectBuilder.build()
    }

    @Test
    fun `check command line for various inputs`() {
        val pwd = project.file(".")
        assertCommandLineEquals(
            listOf(null, "$pwd" + listOf("src", "**", "*.kt").joinToString(File.separator, prefix = File.separator)),
            DiktatExtension().apply {
                inputs = project.files("src/**/*.kt")
            }
        )
    }

    @Test
    fun `check command line in debug mode`() {
        val pwd = project.file(".")
        assertCommandLineEquals(
            listOf(null, "--debug", "$pwd" + listOf("src", "**", "*.kt").joinToString(File.separator, prefix = File.separator)),
            DiktatExtension().apply {
                inputs = project.files("src/**/*.kt")
                debug = true
            }
        )
    }

    private fun registerDiktatTask(extension: DiktatExtension) = project.tasks.register(
        "test", DiktatJavaExecTaskBase::class.java,
        "6.7", extension, project.configurations.create("diktat")
    )

    private fun assertCommandLineEquals(expected: List<String?>, extension: DiktatExtension) {
        val task = registerDiktatTask(extension).get()
        Assertions.assertIterableEquals(expected, task.commandLine)
    }
}