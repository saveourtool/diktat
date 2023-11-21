package com.saveourtool.diktat.plugin.gradle

import com.saveourtool.diktat.plugin.gradle.tasks.DiktatCheckTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DiktatGradlePluginTest {
    private val projectBuilder = ProjectBuilder.builder()
    private lateinit var project: Project

    @BeforeEach
    fun setUp() {
        project = projectBuilder.build()
        // mock kotlin sources
        project.mkdir("src/main/kotlin")
        project.file("src/main/kotlin/Test.kt").createNewFile()
        project.pluginManager.apply(DiktatGradlePlugin::class.java)
    }

    @Test
    fun `check that tasks are registered`() {
        Assertions.assertTrue(project.tasks.findByName("diktatCheck") != null)
        Assertions.assertTrue(project.tasks.findByName("diktatFix") != null)
    }

    @Test
    fun `check default extension properties`() {
        val diktatExtension = project.extensions.getByName("diktat") as DiktatExtension
        val actualInputs = project.tasks
            .named("diktatCheck", DiktatCheckTask::class.java)
            .get()
            .actualInputs
        Assertions.assertFalse(diktatExtension.debug)
        Assertions.assertIterableEquals(project.fileTree("src").files, actualInputs.files)
        Assertions.assertTrue(actualInputs.files.isNotEmpty())
    }
}
