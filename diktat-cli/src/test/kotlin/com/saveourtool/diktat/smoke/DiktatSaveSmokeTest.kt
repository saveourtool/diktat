package com.saveourtool.diktat.smoke

import com.saveourtool.diktat.api.DiktatError
import com.saveourtool.diktat.test.framework.processing.TestComparatorUnit
import com.saveourtool.diktat.test.framework.util.checkForkedJavaHome
import com.saveourtool.diktat.test.framework.util.deleteIfExistsSilently
import com.saveourtool.diktat.test.framework.util.inheritJavaHome
import com.saveourtool.diktat.test.framework.util.isWindows
import com.saveourtool.diktat.test.framework.util.temporaryDirectory

import io.github.oshai.kotlinlogging.KotlinLogging
import org.assertj.core.api.Assertions.fail
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS

import java.net.URL
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.readText

@DisabledOnOs(OS.MAC)
class DiktatSaveSmokeTest : DiktatSmokeTestBase() {
    override fun fixAndCompare(
        config: Path,
        expected: String,
        test: String,
    ) {
        saveSmokeTest(config, test)
    }

    // do nothing, we can't check unfixed lint errors here
    override fun assertUnfixedLintErrors(diktatErrorConsumer: (List<DiktatError>) -> Unit) = Unit

    /**
     * @param testPath path to file with code that will be transformed by formatter, loaded by [TestComparatorUnit.resourceReader]
     * @param configFilePath path of diktat-analysis file
     */
    @Suppress("TOO_LONG_FUNCTION")
    private fun saveSmokeTest(
        configFilePath: Path,
        testPath: String
    ) {
        assertSoftly { softly ->
            softly.assertThat(configFilePath).isRegularFile

            val configFile = (baseDirectoryPath / "diktat-analysis.yml").apply {
                parent.createDirectories()
            }
            val saveLog = (baseDirectoryPath / "tmpSave.txt").apply {
                parent.createDirectories()
                deleteIfExistsSilently()
            }

            configFilePath.copyTo(configFile, overwrite = true)

            val processBuilder = createProcessBuilderWithCmd(testPath).apply {
                redirectErrorStream(true)
                redirectOutput(ProcessBuilder.Redirect.appendTo(saveLog.toFile()))

                /*
                 * Inherit JAVA_HOME for the child process.
                 */
                inheritJavaHome()

                temporaryDirectory(baseDirectoryPath / TEMP_DIRECTORY)
            }

            val saveProcess = processBuilder.start()
            val saveExitCode = saveProcess.waitFor()
            softly.assertThat(saveExitCode).describedAs("The exit code of SAVE").isZero

            softly.assertThat(saveLog).isRegularFile

            val saveOutput = saveLog.readText()

            val saveCommandLine = processBuilder.command().joinToString(separator = " ")
            softly.assertThat(saveOutput)
                .describedAs("The output of \"$saveCommandLine\"")
                .isNotEmpty
                .contains("SUCCESS")
        }
    }

    /**
     * @param testPath path to file with code that will be transformed by formatter, loaded by [TestComparatorUnit.resourceReader]
     * @return ProcessBuilder
     */
    private fun createProcessBuilderWithCmd(testPath: String): ProcessBuilder {
        val savePath = baseDirectoryPath.resolve(getSaveForCurrentOs()).toString()
        val saveArgs = arrayOf(
            baseDirectoryPath.resolve("src/main/kotlin").toString(),
            testPath,
            "--log",
            "all"
        )

        return when {
            System.getProperty("os.name").isWindows() -> arrayOf(savePath, *saveArgs)
            else -> arrayOf("sh", "-c", "chmod 777 $savePath ; $savePath ${saveArgs.joinToString(" ")}")
        }.let { args ->
            ProcessBuilder(*args)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val SAVE_VERSION: String = "0.3.4"
        private const val TEMP_DIRECTORY = ".save-cli"
        private val baseDirectoryPath by lazy { tempDir.absolute() }

        private fun getSaveForCurrentOs(): String {
            val osName = System.getProperty("os.name")

            return when {
                osName.startsWith("Linux", ignoreCase = true) -> "save-$SAVE_VERSION-linuxX64.kexe"
                osName.startsWith("Mac", ignoreCase = true) -> "save-$SAVE_VERSION-macosX64.kexe"
                osName.isWindows() -> "save-$SAVE_VERSION-mingwX64.exe"
                else -> fail("SAVE doesn't support $osName (version ${System.getProperty("os.version")})")
            }
        }

        private fun downloadFile(from: URL, to: Path) = downloadFile(from, to, baseDirectoryPath)

        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
            assertSoftly { softly ->
                checkForkedJavaHome()

                logger.info {
                    "The base directory for the smoke test is $baseDirectoryPath."
                }

                val diktat = baseDirectoryPath / DIKTAT_CLI_JAR
                copyDiktatCli(softly, diktat)
                val save = baseDirectoryPath / getSaveForCurrentOs()
                downloadFile(URL("https://github.com/saveourtool/save-cli/releases/download/v$SAVE_VERSION/${getSaveForCurrentOs()}"), save)
            }
        }
    }
}
