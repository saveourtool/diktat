package com.saveourtool.diktat.plugin.gradle

import com.saveourtool.diktat.api.DiktatReporter
import com.saveourtool.diktat.ktlint.DiktatReporterImpl.Companion.unwrapToKtlint
import com.saveourtool.diktat.plugin.gradle.tasks.DiktatCheckTask
import com.saveourtool.diktat.util.DiktatProcessorListenerWrapper.Companion.unwrap
import com.pinterest.ktlint.cli.reporter.json.JsonReporter
import com.pinterest.ktlint.cli.reporter.plain.PlainReporter
import com.pinterest.ktlint.cli.reporter.sarif.SarifReporter

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class DiktatJavaExecTaskTest {
    private val projectBuilder = ProjectBuilder.builder()
    private lateinit var project: Project

    @BeforeEach
    fun setUp() {
        project = projectBuilder
            .withName("testProject")
            .build()
        // mock kotlin sources
        project.mkdir("src/main/kotlin")
        project.file("src/main/kotlin/Test.kt").createNewFile()
        sequenceOf(
            project.file("diktat-analysis.yml"),
            project.file("../diktat-analysis.yml")
        ).forEach {
            it.writeText("""
                # Common configuration
                - name: DIKTAT_COMMON
                  enabled: true
            """.trimIndent())
        }
    }

    @AfterEach
    fun tearDown() {
        project.layout.buildDirectory.get().asFile.deleteRecursively()
    }

    @Test
    fun `check command line for various inputs`() {
        assertFiles(
            listOf(
                combinePathParts("src", "main", "kotlin", "Test.kt")
            )
        ) {
            inputs { include("src/**/*.kt") }
        }

        val task = project.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatCheckTask
        assert(task.diktatRunner.diktatReporter.unwrapFirst() is PlainReporter)
    }

    @Test
    fun `check command line in debug mode`() {
        assertFiles(
            listOf(combinePathParts("src", "main", "kotlin", "Test.kt"))
        ) {
            inputs { include("src/**/*.kt") }
            debug = true
        }
    }

    @Test
    fun `check command line with excludes`() {
        project.file("src/main/kotlin/generated").mkdirs()
        project.file("src/main/kotlin/generated/Generated.kt").createNewFile()
        assertFiles(
            listOf(combinePathParts("src", "main", "kotlin", "Test.kt"))
        ) {
            inputs {
                include("src/**/*.kt")
                exclude("src/main/kotlin/generated")
            }
        }

        val task = project.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatCheckTask
        assert(task.diktatRunner.diktatReporter.unwrapFirst() is PlainReporter)
    }

    @Test
    fun `check command line with non-existent inputs`() {
        val task = project.registerDiktatTask {
            inputs { exclude("*") }
        }
        Assertions.assertFalse(task.shouldRun)
    }

    @Test
    fun `check system property with default config`() {
        val task = project.registerDiktatTask {
            inputs { exclude("*") }
        }
        Assertions.assertEquals(File(project.projectDir, "diktat-analysis.yml"), task.extension.diktatConfigFile)
    }

    @Test
    fun `check system property with custom config`() {
        val task = project.registerDiktatTask {
            inputs { exclude("*") }
            diktatConfigFile = project.file("../diktat-analysis.yml")
        }
        Assertions.assertEquals(File(project.projectDir.parentFile, "diktat-analysis.yml"), task.extension.diktatConfigFile)
    }

    @Test
    fun `check command line has reporter type and output`() {
        assertFiles(emptyList()) {
            inputs { exclude("*") }
            diktatConfigFile = project.file("../diktat-analysis.yml")
            reporters { reportersDsl ->
                reportersDsl.json { jsonDsl ->
                    jsonDsl.output.set(project.layout.buildDirectory.file("some.txt"))
                }
            }
        }
        val task = project.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatCheckTask
        assert(task.diktatRunner.diktatReporter.unwrapFirst() is JsonReporter)
    }

    @Test
    fun `check command line has reporter type without output`() {
        assertFiles(emptyList()) {
            inputs { exclude("*") }
            diktatConfigFile = project.file("../diktat-analysis.yml")
            reporters { reportersDsl ->
                reportersDsl.json()
            }
        }
        val task = project.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatCheckTask
        assert(task.diktatRunner.diktatReporter.unwrapFirst() is JsonReporter)
    }

    @Test
    fun `check command line in githubActions mode`() {
        assertFiles(emptyList()) {
            inputs { exclude("*") }
            diktatConfigFile = project.file("../diktat-analysis.yml")
            githubActions = true
        }
        val task = project.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatCheckTask
        assert(task.diktatRunner.diktatReporter.unwrapFirst() is SarifReporter)
        Assertions.assertEquals(
            project.rootDir.toString(),
            System.getProperty("user.home")
        )
    }

    @Test
    fun `githubActions mode should have higher precedence over explicit reporter`() {
        assertFiles(emptyList()) {
            inputs { exclude("*") }
            diktatConfigFile = project.file("../diktat-analysis.yml")
            githubActions = true
            reporters { reportersDsl ->
                reportersDsl.json { jsonDsl ->
                    jsonDsl.output.set(project.layout.buildDirectory.file("report.json"))
                }
            }
        }
        val task = project.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatCheckTask
        val (first, second) = task.diktatRunner.diktatReporter
            .unwrap<Iterable<DiktatReporter>>()
            .first()
            .unwrap<DiktatReporter>()
            .unwrap<Iterable<DiktatReporter>>()
            .toList()

        assert(first.unwrapToKtlint() is JsonReporter)
        assert(second.unwrapToKtlint() is SarifReporter)

        Assertions.assertEquals(
            project.rootDir.toString(),
            System.getProperty("user.home")
        )
    }

    @Test
    fun `should set system property with SARIF reporter`() {
        assertFiles(emptyList()) {
            inputs { exclude("*") }
            diktatConfigFile = project.file("../diktat-analysis.yml")
            reporters { reportersDsl ->
                reportersDsl.sarif()
            }
        }
        val task = project.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatCheckTask
        assert(task.diktatRunner.diktatReporter.unwrapFirst() is SarifReporter)
        Assertions.assertEquals(
            project.rootProject.projectDir.toPath().toString(),
            System.getProperty("user.home")
        )
    }

    @Test
    fun `check system property with multiproject build with default config`() {
        setupMultiProject()
        val subproject = project.subprojects.first()
        project.allprojects {
            it.registerDiktatTask {
                inputs { exclude("*") }
            }
        }
        val task = project.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatCheckTask
        val subprojectTask = subproject.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatCheckTask
        Assertions.assertEquals(File(project.projectDir, "diktat-analysis.yml"), task.extension.diktatConfigFile)
        Assertions.assertEquals(File(project.projectDir, "diktat-analysis.yml"), subprojectTask.extension.diktatConfigFile)
    }

    @Test
    fun `check system property with multiproject build with custom config`() {
        setupMultiProject()
        val subproject = project.subprojects.first()
        project.allprojects {
            it.registerDiktatTask {
                inputs { exclude("*") }
                diktatConfigFile = it.rootProject.file("diktat-analysis.yml")
            }
        }
        val task = project.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatCheckTask
        val subprojectTask = subproject.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatCheckTask
        Assertions.assertEquals(File(project.projectDir, "diktat-analysis.yml"), task.extension.diktatConfigFile)
        Assertions.assertEquals(File(project.projectDir, "diktat-analysis.yml"), subprojectTask.extension.diktatConfigFile)
    }

    @Test
    fun `check system property with multiproject build with custom config and two config files`() {
        setupMultiProject()
        val subproject = project.subprojects.single()
        subproject.file("diktat-analysis.yml").createNewFile()
        project.allprojects {
            it.registerDiktatTask {
                inputs { exclude("*") }
                diktatConfigFile = it.file("diktat-analysis.yml")
            }
        }
        val task = project.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatCheckTask
        val subprojectTask = subproject.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatCheckTask
        Assertions.assertEquals(File(project.projectDir, "diktat-analysis.yml"), task.extension.diktatConfigFile)
        Assertions.assertEquals(File(subproject.projectDir, "diktat-analysis.yml"), subprojectTask.extension.diktatConfigFile)
    }

    private fun Project.registerDiktatTask(extensionConfiguration: DiktatExtension.() -> Unit): DiktatCheckTask {
        pluginManager.apply(DiktatGradlePlugin::class.java)
        extensions.configure("diktat", extensionConfiguration)
        return tasks.getByName(DIKTAT_CHECK_TASK) as DiktatCheckTask
    }

    private fun assertFiles(expected: List<String>, extensionConfiguration: DiktatExtension.() -> Unit) {
        val task = project.registerDiktatTask(extensionConfiguration)
        val files = task.actualInputs.files
        Assertions.assertEquals(expected.size, files.size)
        Assertions.assertTrue {
            expected.zip(files)
                .map { (expectedStr, file) ->
                    file.path.endsWith(expectedStr)
                }
                .all { it }
        }
    }

    private fun setupMultiProject() {
        ProjectBuilder.builder()
            .withParent(project)
            .withName("testSubproject")
            .withProjectDir(File(project.projectDir, "testSubproject").also {
                Files.createDirectory(it.toPath())
            })
            .build()
        project.file("diktat-analysis.yml").createNewFile()
    }

    private fun combinePathParts(vararg parts: String) = parts.joinToString(File.separator)

    companion object {
        private const val DIKTAT_CHECK_TASK = "diktatCheck"

        private fun DiktatReporter.unwrapFirst() = this
            .unwrap<Iterable<DiktatReporter>>().first()
            .unwrap<DiktatReporter>()
            .unwrap<Iterable<DiktatReporter>>().first()
            .unwrapToKtlint()
    }
}
