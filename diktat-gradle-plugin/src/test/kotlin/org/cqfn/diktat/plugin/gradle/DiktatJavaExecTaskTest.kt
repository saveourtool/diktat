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
            listOf(null, combinePathParts("src", "**", "*.kt"))
        ) {
            inputs = project.files("src/**/*.kt")
        }
    }

    @Test
    fun `check command line in debug mode`() {
        assertCommandLineEquals(
            listOf(null, "--debug", combinePathParts("src", "**", "*.kt"))
        ) {
            inputs = project.files("src/**/*.kt")
            debug = true
        }
    }

    @Test
    fun `check command line with excludes`() {
        assertCommandLineEquals(
            listOf(null, combinePathParts("src", "**", "*.kt"),
                "!${combinePathParts("src", "main", "kotlin", "generated")}"
            )
        ) {
            inputs = project.files("src/**/*.kt")
            excludes = project.files("src/main/kotlin/generated")
        }
    }

    @Test
    fun `check command line with non-existent inputs`() {
        val task = registerDiktatTask {
            inputs = project.files()
        }
        Assertions.assertFalse(task.shouldRun)
    }

    private fun registerDiktatTask(extensionConfiguration: DiktatExtension.() -> Unit): DiktatJavaExecTaskBase {
        DiktatGradlePlugin().apply(project)
        project.extensions.configure("diktat", extensionConfiguration)
        return project.tasks.getByName("diktatCheck") as DiktatJavaExecTaskBase
    }

    private fun assertCommandLineEquals(expected: List<String?>, extensionConfiguration: DiktatExtension.() -> Unit) {
        val task = registerDiktatTask(extensionConfiguration)
        Assertions.assertIterableEquals(expected, task.commandLine)
    }

    private fun combinePathParts(vararg parts: String) = parts.joinToString(File.separator)
}
