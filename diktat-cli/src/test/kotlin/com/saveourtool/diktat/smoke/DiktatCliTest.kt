package com.saveourtool.diktat.smoke

import com.saveourtool.diktat.test.framework.util.checkForkedJavaHome
import com.saveourtool.diktat.test.framework.util.deleteIfExistsSilently
import com.saveourtool.diktat.test.framework.util.inheritJavaHome
import com.saveourtool.diktat.test.framework.util.isWindows
import com.saveourtool.diktat.test.framework.util.temporaryDirectory
import io.github.oshai.kotlinlogging.KotlinLogging
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.PathWalkOption
import kotlin.io.path.absolutePathString
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.isDirectory
import kotlin.io.path.readText
import kotlin.io.path.walk

class DiktatCliTest {

    @Test
    fun `Run diKTat from cli`() {
        cliTest("examples/maven/src/main/kotlin/Test.kt")
    }

    @Test
    fun `Run diKTat from cli (absolute paths)`() {
        cliTest(tempDir.resolve("examples/maven/src/main/kotlin/Test.kt").absolutePathString())
    }

    @Test
    fun `Run diKTat from cli (glob paths, 1 of 4)`() {
        cliTest("examples/maven/src/main/kotlin/*.kt")
    }

    @Test
    fun `Run diKTat from cli (glob paths, 2 of 4)`() {
        cliTest("examples/**/main/kotlin/*.kt")
    }

    @Test
    fun `Run diKTat from cli (glob paths, 3 of 4)`() {
        cliTest("examples/**/*.kt")
    }

    @Test
    fun `Run diKTat from cli (glob paths, 4 of 4)`() {
        cliTest("**/*.kt")
    }

    @Suppress("TOO_LONG_FUNCTION")
    private fun cliTest(
        vararg cliArgs: String,
    ) {
        assertSoftly { softly ->
            val diktatCliLog = (tempDir / "log.txt").apply {
                parent.createDirectories()
                deleteIfExistsSilently()
            }

            val processBuilder = createProcessBuilderWithCmd(*cliArgs).apply {
                redirectErrorStream(true)
                redirectOutput(ProcessBuilder.Redirect.appendTo(diktatCliLog.toFile()))

                /*
                 * Inherit JAVA_HOME for the child process.
                 */
                inheritJavaHome()

                temporaryDirectory(tempDir / ".tmp")
            }

            val diktatCliProcess = processBuilder.start()
            val exitCode = diktatCliProcess.waitFor()
            softly.assertThat(exitCode).describedAs("The exit code of Diktat CLI").isOne

            softly.assertThat(diktatCliLog).isRegularFile

            val diktatCliOutput = diktatCliLog.readText()

            val commandLine = processBuilder.command().joinToString(separator = " ")
            softly.assertThat(diktatCliOutput)
                .describedAs("The output of \"$commandLine\"")
                .isNotEmpty
                .contains("[VARIABLE_NAME_INCORRECT_FORMAT]")
                .doesNotContain("WARNING:")
        }
    }

    private fun createProcessBuilderWithCmd(vararg cliArgs: String): ProcessBuilder {
        return when {
            System.getProperty("os.name").isWindows() -> arrayOf(*javaArgs, DIKTAT_CLI_JAR, *defaultArgs, *cliArgs)
            else -> arrayOf("sh", "-c", arrayOf(*javaArgs, DIKTAT_CLI_JAR, *defaultArgs, *cliArgs).joinToString(" "))
        }.let { args -> ProcessBuilder(*args).directory(tempDir.toFile()) }
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        private val javaArgs = arrayOf("java", "-showversion", "-jar")
        private val defaultArgs = arrayOf("--log-level", "debug")

        @JvmStatic
        @TempDir
        internal var tempDir: Path = Paths.get("/invalid")

        @BeforeAll
        @JvmStatic
        @OptIn(ExperimentalPathApi::class)
        internal fun beforeAll() {
            assertSoftly { softly ->
                checkForkedJavaHome()

                logger.info {
                    "The temp directory for the test is $tempDir."
                }
                val sourceDirectory = Paths.get("../examples")
                val targetDirectory = (tempDir / "examples").also {
                    it.createDirectories()
                }
                sourceDirectory.walk(PathWalkOption.INCLUDE_DIRECTORIES).forEach { file ->
                    if (file.isDirectory()) {
                        targetDirectory.resolve(sourceDirectory.relativize(file)).createDirectories()
                    } else {
                        val dest = targetDirectory.resolve(sourceDirectory.relativize(file))
                        file.copyTo(dest)
                    }
                }
                copyDiktatCli(softly, tempDir / DIKTAT_CLI_JAR)

                val defaultConfigFile = Paths.get("../diktat-analysis.yml")
                softly.assertThat(defaultConfigFile)
                    .describedAs("Default config file for diktat")
                    .isRegularFile
                defaultConfigFile.copyTo(tempDir / "diktat-analysis.yml", overwrite = true)
            }
        }
    }
}
