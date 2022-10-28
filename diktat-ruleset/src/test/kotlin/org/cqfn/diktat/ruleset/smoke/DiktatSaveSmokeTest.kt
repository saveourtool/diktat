package org.cqfn.diktat.ruleset.smoke

import org.cqfn.diktat.common.utils.loggerWithKtlintConfig
import org.cqfn.diktat.util.deleteIfExistsSilently
import org.cqfn.diktat.util.isSameJavaHomeAs
import org.cqfn.diktat.util.prependPath
import org.cqfn.diktat.util.retry
import com.pinterest.ktlint.core.LintError

import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
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
import kotlin.io.path.outputStream
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.system.measureNanoTime

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

    override fun assertUnfixedLintErrors(lintErrorsConsumer: (List<LintError>) -> Unit) {
        // do nothing, we can't check unfixed lint errors here
    }

    /**
     * @param testPath path to file with code that will be transformed by formatter, relative to [resourceFilePath]
     * @param configFilePath path of diktat-analysis file
     */
    @Suppress("TOO_LONG_FUNCTION")
    private fun saveSmokeTest(
        configFilePath: Path,
        testPath: String
    ) {
        assertSoftly { softly ->
            softly.assertThat(configFilePath).isRegularFile

            val configFile = (baseDirectory / "diktat-analysis.yml").apply {
                parent.createDirectories()
            }
            val saveLog = (baseDirectory / "tmpSave.txt").apply {
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
                    val javaHome = System.getProperty("java.home")
                    environment()["JAVA_HOME"] = javaHome
                    prependPath(Path(javaHome) / "bin")
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
     * @param testPath path to file with code that will be transformed by formatter, relative to [resourceFilePath]
     * @return ProcessBuilder
     */
    private fun createProcessBuilderWithCmd(testPath: String): ProcessBuilder {
        val filesDir = "src/test/resources/test/smoke"
        val savePath = "$filesDir/${getSaveForCurrentOs()}"

        val systemName = System.getProperty("os.name")
        val result = when {
            systemName.startsWith("Linux", ignoreCase = true) || systemName.startsWith("Mac", ignoreCase = true) ->
                ProcessBuilder("sh", "-c", "chmod 777 $savePath ; ./$savePath $filesDir/src/main/kotlin $testPath --log all")
            else -> ProcessBuilder(savePath, "--log", "all", "$filesDir/src/main/kotlin", testPath)
        }
        return result
    }

    companion object {
        @Suppress("EMPTY_BLOCK_STRUCTURE_ERROR")
        private val logger = KotlinLogging.loggerWithKtlintConfig { }
        private const val BUILD_DIRECTORY = "target"
        private const val FAT_JAR_GLOB = "diktat-*.jar"
        private const val KTLINT_VERSION = "0.46.1"
        private const val SAVE_VERSION: String = "0.3.2"
        private val baseDirectory = Path("src/test/resources/test/smoke").absolute()

        private fun getSaveForCurrentOs(): String {
            val osName = System.getProperty("os.name")

            return when {
                osName.startsWith("Linux", ignoreCase = true) -> "save-$SAVE_VERSION-linuxX64.kexe"
                osName.startsWith("Mac", ignoreCase = true) -> "save-$SAVE_VERSION-macosX64.kexe"
                osName.startsWith("Windows", ignoreCase = true) -> "save-$SAVE_VERSION-mingwX64.exe"
                else -> fail("SAVE doesn't support $osName (version ${System.getProperty("os.version")})")
            }
        }

        @Suppress("FLOAT_IN_ACCURATE_CALCULATIONS")
        private fun downloadFile(from: URL, to: Path) {
            logger.info {
                "Downloading $from to ${to.relativeTo(baseDirectory)}..."
            }

            val attempts = 5

            val lazyDefault: (Throwable) -> Unit = { error ->
                fail("Failure downloading $from after $attempts attempt(s)", error)
            }

            retry(attempts, lazyDefault = lazyDefault) {
                from.openStream().use { source ->
                    to.outputStream().use { target ->
                        val bytesCopied: Long
                        val timeNanos = measureNanoTime {
                            bytesCopied = source.copyTo(target)
                        }
                        logger.info {
                            "$bytesCopied byte(s) copied in ${timeNanos / 1000 / 1e3} ms."
                        }
                    }
                }
            }
        }

        @BeforeAll
        @JvmStatic
        @Suppress("AVOID_NULL_CHECKS")
        internal fun beforeAll() {
            val forkedJavaHome = System.getenv("JAVA_HOME")
            if (forkedJavaHome != null) {
                val javaHome = System.getProperty("java.home")
                if (javaHome != null && !Path(javaHome).isSameJavaHomeAs(Path(forkedJavaHome))) {
                    logger.warn {
                        "Current JDK home is $javaHome. Forked tests may use a different JDK at $forkedJavaHome."
                    }
                }
                logger.warn {
                    "Make sure JAVA_HOME ($forkedJavaHome) points to a Java 8 or Java 11 home. Java 17 is not yet supported."
                }
            }

            logger.info {
                "The base directory for the smoke test is $baseDirectory."
            }

            /*
             * The fat JAR should reside in the same directory as `ktlint` and
             * `save*` and be named `diktat.jar`
             * (see `diktat-rules/src/test/resources/test/smoke/save.toml`).
             */
            val diktatFrom = Path(BUILD_DIRECTORY)
                .takeIf(Path::exists)
                ?.listDirectoryEntries(FAT_JAR_GLOB)
                ?.singleOrNull()
            assertThat(diktatFrom)
                .describedAs(diktatFrom?.toString() ?: "$BUILD_DIRECTORY/$FAT_JAR_GLOB")
                .isNotNull
                .isRegularFile

            val diktat = baseDirectory / "diktat.jar"
            val save = baseDirectory / getSaveForCurrentOs()
            val ktlint = baseDirectory / "ktlint"

            downloadFile(URL("https://github.com/saveourtool/save-cli/releases/download/v$SAVE_VERSION/${getSaveForCurrentOs()}"), save)
            downloadFile(URL("https://github.com/pinterest/ktlint/releases/download/$KTLINT_VERSION/ktlint"), ktlint)

            diktatFrom?.copyTo(diktat, overwrite = true)
        }

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            val diktat = baseDirectory / "diktat.jar"
            val save = baseDirectory / getSaveForCurrentOs()
            val ktlint = baseDirectory / "ktlint"

            diktat.deleteIfExistsSilently()
            ktlint.deleteIfExistsSilently()
            save.deleteIfExistsSilently()
        }
    }
}
