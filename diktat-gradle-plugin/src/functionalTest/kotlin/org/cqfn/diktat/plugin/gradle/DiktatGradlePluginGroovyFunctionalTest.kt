package org.cqfn.diktat.plugin.gradle

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
        buildFile.appendText(
            """${System.lineSeparator()}
            repositories {
                mavenCentral()
                maven {
                    url "https://oss.sonatype.org/content/repositories/snapshots"
                    content {
                        includeGroup("com.pinterest")
                        includeGroup("com.pinterest.ktlint")
                    }
                }
            }
        """.trimIndent()
        )
    }

    @AfterEach
    fun tearDown() {
        testProjectDir.delete()
    }

    @Test
    fun `should execute diktatCheck on default values`() {
        val result = runDiktat(testProjectDir, shouldSucceed = false)

        val diktatCheckBuildResult = result.task(":${DiktatGradlePlugin.DIKTAT_CHECK_TASK}")
        requireNotNull(diktatCheckBuildResult)
        Assertions.assertEquals(TaskOutcome.FAILED, diktatCheckBuildResult.outcome)
        Assertions.assertTrue(
            result.output.contains("[FILE_NAME_MATCH_CLASS]")
        )
    }
}
