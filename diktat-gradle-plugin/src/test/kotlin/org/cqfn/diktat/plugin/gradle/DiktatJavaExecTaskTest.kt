package org.cqfn.diktat.plugin.gradle

import org.cqfn.diktat.common.config.rules.DIKTAT_CONF_PROPERTY
import org.cqfn.diktat.common.ktlint.ktlintDisabledRulesArgument

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
    }

    @AfterEach
    fun tearDown() {
        project.buildDir.deleteRecursively()
    }

    @Test
    fun `check command line for various inputs`() {
        assertCommandLineEquals(
            listOf(
                null,
                ktlintDisabledRulesArgument,
                combinePathParts("src", "main", "kotlin", "Test.kt"),
                "--reporter=plain"
            )
        ) {
            inputs { include("src/**/*.kt") }
        }
    }

    @Test
    fun `check command line in debug mode`() {
        assertCommandLineEquals(
            listOf(
                null,
                ktlintDisabledRulesArgument,
                "--debug",
                combinePathParts("src", "main", "kotlin", "Test.kt"),
                "--reporter=plain"
            )
        ) {
            inputs { include("src/**/*.kt") }
            debug = true
        }
    }

    @Test
    fun `check command line with excludes`() {
        project.file("src/main/kotlin/generated").mkdirs()
        project.file("src/main/kotlin/generated/Generated.kt").createNewFile()
        assertCommandLineEquals(
            listOf(
                null,
                ktlintDisabledRulesArgument,
                combinePathParts("src", "main", "kotlin", "Test.kt"),
                "--reporter=plain"
            )
        ) {
            inputs {
                include("src/**/*.kt")
                exclude("src/main/kotlin/generated")
            }
        }
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
        Assertions.assertEquals(File(project.projectDir, "diktat-analysis.yml").absolutePath, task.systemProperties[DIKTAT_CONF_PROPERTY])
    }

    @Test
    fun `check system property with custom config`() {
        val task = project.registerDiktatTask {
            inputs { exclude("*") }
            diktatConfigFile = project.file("../diktat-analysis.yml")
        }
        Assertions.assertEquals(File(project.projectDir.parentFile, "diktat-analysis.yml").absolutePath, task.systemProperties[DIKTAT_CONF_PROPERTY])
    }

    @Test
    fun `check command line has reporter type and output`() {
        assertCommandLineEquals(
            listOf(
                null,
                ktlintDisabledRulesArgument,
                "--reporter=json,output=${project.projectDir.resolve("some.txt")}"
            )
        ) {
            inputs { exclude("*") }
            diktatConfigFile = project.file("../diktat-analysis.yml")
            reporter = "json"
            output = "some.txt"
        }
    }

    @Test
    fun `check command line has reporter type without output`() {
        assertCommandLineEquals(
            listOf(
                null,
                ktlintDisabledRulesArgument,
                "--reporter=json"
            )
        ) {
            inputs { exclude("*") }
            diktatConfigFile = project.file("../diktat-analysis.yml")
            reporter = "json"
        }
    }

    @Test
    fun `check command line in githubActions mode`() {
        val path = project.file("${project.buildDir}/reports/diktat/diktat.sarif")
        assertCommandLineEquals(
            listOf(
                null,
                ktlintDisabledRulesArgument,
                "--reporter=sarif,output=$path"
            )
        ) {
            inputs { exclude("*") }
            diktatConfigFile = project.file("../diktat-analysis.yml")
            githubActions = true
        }
        val task = project.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatJavaExecTaskBase
        Assertions.assertEquals(
            project.rootDir.toString(),
            task.systemProperties["user.home"]
        )
    }

    @Test
    fun `githubActions mode should have higher precedence over explicit reporter`() {
        val path = project.file("${project.buildDir}/reports/diktat/diktat.sarif")
        assertCommandLineEquals(
            listOf(
                null,
                ktlintDisabledRulesArgument,
                "--reporter=sarif,output=$path"
            )
        ) {
            inputs { exclude("*") }
            diktatConfigFile = project.file("../diktat-analysis.yml")
            githubActions = true
            reporter = "json"
            output = "report.json"
        }
    }

    @Test
    fun `should set system property with SARIF reporter`() {
        assertCommandLineEquals(
            listOf(
                null,
                ktlintDisabledRulesArgument,
                "--reporter=sarif"
            )
        ) {
            inputs { exclude("*") }
            diktatConfigFile = project.file("../diktat-analysis.yml")
            reporter = "sarif"
        }
        val task = project.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatJavaExecTaskBase
        Assertions.assertEquals(
            project.rootDir.toString(),
            task.systemProperties["user.home"]
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
        val task = project.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatJavaExecTaskBase
        val subprojectTask = subproject.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatJavaExecTaskBase
        Assertions.assertEquals(File(project.projectDir, "diktat-analysis.yml").absolutePath, task.systemProperties[DIKTAT_CONF_PROPERTY])
        Assertions.assertEquals(File(project.projectDir, "diktat-analysis.yml").absolutePath, subprojectTask.systemProperties[DIKTAT_CONF_PROPERTY])
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
        val task = project.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatJavaExecTaskBase
        val subprojectTask = subproject.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatJavaExecTaskBase
        Assertions.assertEquals(File(project.projectDir, "diktat-analysis.yml").absolutePath, task.systemProperties[DIKTAT_CONF_PROPERTY])
        Assertions.assertEquals(File(project.projectDir, "diktat-analysis.yml").absolutePath, subprojectTask.systemProperties[DIKTAT_CONF_PROPERTY])
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
        val task = project.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatJavaExecTaskBase
        val subprojectTask = subproject.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatJavaExecTaskBase
        Assertions.assertEquals(File(project.projectDir, "diktat-analysis.yml").absolutePath, task.systemProperties[DIKTAT_CONF_PROPERTY])
        Assertions.assertEquals(File(subproject.projectDir, "diktat-analysis.yml").absolutePath, subprojectTask.systemProperties[DIKTAT_CONF_PROPERTY])
    }

    private fun Project.registerDiktatTask(extensionConfiguration: DiktatExtension.() -> Unit): DiktatJavaExecTaskBase {
        pluginManager.apply(DiktatGradlePlugin::class.java)
        extensions.configure("diktat", extensionConfiguration)
        return tasks.getByName(DIKTAT_CHECK_TASK) as DiktatJavaExecTaskBase
    }

    private fun assertCommandLineEquals(expected: List<String?>, extensionConfiguration: DiktatExtension.() -> Unit) {
        val task = project.registerDiktatTask(extensionConfiguration)
        Assertions.assertIterableEquals(expected, task.commandLine)
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
    }
}
