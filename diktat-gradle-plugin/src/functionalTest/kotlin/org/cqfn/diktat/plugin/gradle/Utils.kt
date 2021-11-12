package org.cqfn.diktat.plugin.gradle

import org.gradle.buildinit.plugins.internal.modifiers.BuildInitDsl
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
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
    testProjectDir.newFile(buildFileName).apply {
        writeText(
            """
                plugins {
                    id("org.cqfn.diktat.diktat-gradle-plugin")
                }
                
                repositories {
                    mavenLocal()
                    mavenCentral()
                }
            """.trimIndent()
        )
    }
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
    .getOrNull()!!

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
