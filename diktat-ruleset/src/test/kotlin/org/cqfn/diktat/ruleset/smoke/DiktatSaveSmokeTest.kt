package org.cqfn.diktat.ruleset.smoke

import org.cqfn.diktat.api.DiktatError
import org.cqfn.diktat.test.framework.processing.TestComparatorUnit
import org.cqfn.diktat.test.framework.util.checkForkedJavaHome
import org.cqfn.diktat.test.framework.util.deleteIfExistsRecursively
import org.cqfn.diktat.test.framework.util.deleteIfExistsSilently
import org.cqfn.diktat.test.framework.util.inheritJavaHome
import org.cqfn.diktat.test.framework.util.isWindows
import org.cqfn.diktat.test.framework.util.temporaryDirectory

import mu.KotlinLogging
import org.assertj.core.api.Assertions.fail
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS

import java.net.URL
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText

@DisabledOnOs(OS.MAC)
class DiktatSaveSmokeTest : DiktatSmokeTestBase() {
    override fun fixAndCompare(
        config: Path,
        expected: String,
        test: String,
        trimLastEmptyLine: Boolean,
    ) {
        saveSmokeTest(config, test)
    }

    // do nothing, we can't check unfixed lint errors here
    override fun doAssertUnfixedLintErrors(diktatErrorConsumer: (List<DiktatError>) -> Unit) = Unit

    /**
     * @param testPath path to file with code that will be transformed by formatter, relative to [TestComparatorUnit.resourceFilePath]
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

            try {
                configFilePath.copyTo(configFile, overwrite = true)

                val processBuilder = createProcessBuilderWithCmd(testPath).apply {
                    redirectErrorStream(true)
                    redirectOutput(ProcessBuilder.Redirect.appendTo(saveLog.toFile()))

                    /*
                     * Inherit JAVA_HOME for the child process.
                     */
                    inheritJavaHome()

                    /*
                     * On Windows, ktlint is often unable to relativize paths
                     * (see https://github.com/pinterest/ktlint/issues/1608).
                     *
                     * So let's force the temporary directory to be the
                     * sub-directory of the project root.
                     */
                    if (System.getProperty("os.name").isWindows()) {
                        temporaryDirectory(baseDirectoryPath / WINDOWS_TEMP_DIRECTORY)
                    }
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
            } finally {
                configFile.deleteIfExistsSilently()
                saveLog.deleteIfExistsSilently()
            }
        }
    }

    /**
     * @param testPath path to file with code that will be transformed by formatter, relative to [TestComparatorUnit.resourceFilePath]
     * @return ProcessBuilder
     */
    private fun createProcessBuilderWithCmd(testPath: String): ProcessBuilder {
        val savePath = "$BASE_DIRECTORY/${getSaveForCurrentOs()}"
        val saveArgs = arrayOf(
            "$BASE_DIRECTORY/src/main/kotlin",
            testPath,
            "--log",
            "all"
        )

        return when {
            System.getProperty("os.name").isWindows() -> arrayOf(savePath, *saveArgs)
            else -> arrayOf("sh", "-c", "chmod 777 $savePath ; ./$savePath ${saveArgs.joinToString(" ")}")
        }.let { args ->
            ProcessBuilder(*args)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val BASE_DIRECTORY = "src/test/resources/test/smoke"
        private const val SAVE_VERSION: String = "0.3.4"
        private const val WINDOWS_TEMP_DIRECTORY = ".save-cli"
        private val baseDirectoryPath = Path(BASE_DIRECTORY).absolute()

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

                /*
                 * The fat JAR should reside in the same directory as `ktlint` and
                 * `save*` and be named `diktat.jar`
                 * (see `diktat-rules/src/test/resources/test/smoke/save.toml`).
                 */
                val buildDirectory = Path(BUILD_DIRECTORY)
                softly.assertThat(buildDirectory)
                    .isDirectory
                val diktatFrom = buildDirectory
                    .takeIf(Path::exists)
                    ?.listDirectoryEntries(DIKTAT_FAT_JAR_GLOB)
                    ?.singleOrNull()
                softly.assertThat(diktatFrom)
                    .describedAs(diktatFrom?.toString() ?: "$BUILD_DIRECTORY/$DIKTAT_FAT_JAR_GLOB")
                    .isNotNull
                    .isRegularFile

                val diktat = baseDirectoryPath / DIKTAT_FAT_JAR
                val save = baseDirectoryPath / getSaveForCurrentOs()
                val ktlint = baseDirectoryPath / KTLINT_FAT_JAR

                downloadFile(URL("https://github.com/saveourtool/save-cli/releases/download/v$SAVE_VERSION/${getSaveForCurrentOs()}"), save)
                downloadFile(URL("https://github.com/pinterest/ktlint/releases/download/$KTLINT_VERSION/ktlint"), ktlint)

                diktatFrom?.copyTo(diktat, overwrite = true)
            }
        }

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            val diktat = baseDirectoryPath / DIKTAT_FAT_JAR
            val save = baseDirectoryPath / getSaveForCurrentOs()
            val ktlint = baseDirectoryPath / KTLINT_FAT_JAR

            diktat.deleteIfExistsSilently()
            ktlint.deleteIfExistsSilently()
            save.deleteIfExistsSilently()

            if (System.getProperty("os.name").isWindows()) {
                val tempDirectory = baseDirectoryPath / WINDOWS_TEMP_DIRECTORY
                tempDirectory.deleteIfExistsRecursively()
            }
        }
    }
}
