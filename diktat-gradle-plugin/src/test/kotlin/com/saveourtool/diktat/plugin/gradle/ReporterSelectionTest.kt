package com.saveourtool.diktat.plugin.gradle

import org.gradle.api.Project
import org.gradle.api.tasks.util.PatternSet
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ReporterSelectionTest {
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
    fun `should fallback to plain reporter for unknown reporter types`() {
        val diktatExtension = DiktatExtension(
            PatternSet(),
        ).apply {
            reporter = "jsonx"
        }

        Assertions.assertEquals(
            "plain",
            project.getReporterType(diktatExtension)
        )
    }
}
