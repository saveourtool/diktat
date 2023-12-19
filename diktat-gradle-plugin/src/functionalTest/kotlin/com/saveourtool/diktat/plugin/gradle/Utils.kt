package com.saveourtool.diktat.plugin.gradle

import org.gradle.buildinit.plugins.internal.modifiers.BuildInitDsl
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

internal val testsCounter = AtomicInteger(0)

internal fun createExampleProject(testProjectDir: TemporaryFolder,
                                  exampleProject: File,
                                  buildInitDsl: BuildInitDsl
) {
    exampleProject.copyRecursively(testProjectDir.root)
    val buildFileName = buildInitDsl.fileNameFor("build")
    File(testProjectDir.root, buildFileName).delete()
    testProjectDir.newFile(buildFileName).writeText(
        """
            plugins {
                id("com.saveourtool.diktat")
            }

            repositories {
                mavenLocal()
                mavenCentral()
            }
        """.trimIndent()
    )
}

/**
 * @param arguments additional arguments to pass to [GradleRunner]
 */
internal fun runDiktat(testProjectDir: TemporaryFolder,
                      shouldSucceed: Boolean = true,
                      arguments: List<String> = emptyList(),
                      configureRunner: GradleRunner.() -> GradleRunner = { this }
) = GradleRunner.create()
    .run(configureRunner)
    .withProjectDir(testProjectDir.root)
    .withArguments(arguments + DiktatGradlePlugin.DIKTAT_CHECK_TASK)
    .withPluginClasspath()
    .withJaCoCo(testsCounter.incrementAndGet())
    .forwardOutput()
    .runCatching {
        if (shouldSucceed) build() else buildAndFail()
    }
    .also {
        require(it.isSuccess) {
            val ex = it.exceptionOrNull()
            "Running gradle returned exception $ex, cause: ${ex?.cause}"
        }
    }
    .getOrNull()
    .let {
        requireNotNull(it) {
            "Failed to get build result from running diktat"
        }
    }

/**
 * This is support for jacoco reports in tests run with gradle TestKit
 */
private fun GradleRunner.withJaCoCo(number: Int) = apply {
    javaClass.classLoader
        .getResourceAsStream("testkit-gradle.properties")
        .also { it ?: error("properties file for testkit is not available, check build configuration") }
        ?.use { propertiesFileStream ->
            val text = propertiesFileStream.reader().readText()
            File(projectDir, "gradle.properties").createNewFile()
            File(projectDir, "gradle.properties").writer().use {
                it.write(text.replace("functionalTest.exec", "functionalTest-$number.exec"))
            }
        }
}

fun assertDiktatExecuted(
    result: BuildResult,
    taskOutcome: TaskOutcome = TaskOutcome.FAILED,
    errorMessage: () -> String? = { null }
) {
    val diktatCheckBuildResult = result.task(":${DiktatGradlePlugin.DIKTAT_CHECK_TASK}")
    requireNotNull(diktatCheckBuildResult)
    Assertions.assertEquals(taskOutcome, diktatCheckBuildResult.outcome, errorMessage)
    Assertions.assertTrue(
        result.output.contains("[FILE_NAME_MATCH_CLASS]")
    ) {
        "Task ${DiktatGradlePlugin.DIKTAT_CHECK_TASK} wasn't run"
    }
}
