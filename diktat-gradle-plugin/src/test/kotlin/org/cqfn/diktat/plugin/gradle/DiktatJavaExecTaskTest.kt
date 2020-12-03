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
        assertCommandLineEquals(
            listOf(null, "\"${listOf("src", "**", "*.kt").joinToString(File.separator)}\""),
            DiktatExtension().apply {
                inputs = project.files("src/**/*.kt")
            }
        )
    }

    @Test
    fun `check command line in debug mode`() {
        assertCommandLineEquals(
            listOf(null, "--debug", "\"${listOf("src", "**", "*.kt").joinToString(File.separator)}\""),
            DiktatExtension().apply {
                inputs = project.files("src/**/*.kt")
                debug = true
            }
        )
    }

    @Test
    fun `check command line with excludes`() {
        assertCommandLineEquals(
            listOf(null, "\"${listOf("src", "**", "*.kt").joinToString(File.separator)}\"",
                "\"!${listOf("src", "main", "kotlin", "generated").joinToString(File.separator)}\""
            ),
            DiktatExtension().apply {
                inputs = project.files("src/**/*.kt")
                excludes = project.files("src/main/kotlin/generated")
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
