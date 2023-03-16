package org.cqfn.diktat.ruleset.smoke

import org.cqfn.diktat.common.ktlint.ktlintDisabledRulesArgument
import org.cqfn.diktat.test.framework.util.checkForkedJavaHome
import org.cqfn.diktat.test.framework.util.inheritJavaHome
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.assertj.core.api.WithAssumptions
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.io.TempDir
import java.lang.ProcessBuilder.Redirect
import java.lang.ProcessBuilder.Redirect.INHERIT
import java.lang.management.ManagementFactory
import java.net.URL
import java.nio.file.Path
import java.util.concurrent.TimeUnit.MINUTES
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract
import kotlin.io.path.Path
import kotlin.io.path.copyTo
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.pathString
import kotlin.io.path.writeText
import kotlin.math.min
import kotlin.text.Charsets.UTF_8

/**
 * A smoke test which uses a forked JVM.
 */
class DiktatForkedJvmSmokeTest : WithAssumptions {
    @Test
    @Timeout(TEST_TIMEOUT_MINUTES, unit = MINUTES)
    fun `run-time ktlint version should be the same as expected`(@TempDir projectDir: Path) {
        assertSoftly { softly ->
            val diktatLog = projectDir / "diktat.log"

            val diktatCommand = diktatProcessBuilder("-V") {
                redirectInput(INHERIT)
                redirectOutput(Redirect.to(diktatLog.toFile()))
                redirectError(INHERIT)
                inheritJavaHome()
                directory(projectDir.toFile())
            }

            val diktatExitCode = diktatCommand.start().waitFor()
            softly.assertThat(diktatExitCode)
                .describedAs("Diktat exit code")
                .isZero

            softly.assertThat(diktatLog)
                .isRegularFile
                .content(UTF_8)
                .isEqualToIgnoringNewLines(KTLINT_VERSION)
        }
    }

    @Test
    @Timeout(TEST_TIMEOUT_MINUTES, unit = MINUTES)
    fun parallel(@TempDir projectDir: Path) {
        /*
         * We need to run Diktat in parallel, where N threads are processing
         * N files, so that, for each rule id, multiple rule instances get
         * created.
         */
        assumeThat(availableProcessors)
            .describedAs("Available processors")
            .isGreaterThan(1)

        /*
         * Use at most 26 classes.
         */
        val maxClasses = 'Z' - 'A' + 1

        assertSoftly { softly ->
            for (i in 0 until min(availableProcessors, maxClasses)) {
                val className = ('A' + i).toString()

                /*
                 * A single class declaration w/o any extra code will result in
                 * plenty of errors, even with the default `diktat-analysis.yml`.
                 */
                @Language("kotlin")
                val code = "class $className"

                val file = projectDir / "$className.kt"

                file.writeText(code)
            }

            val diktatCommand = diktatProcessBuilder("--relative", "--verbose", "*.kt") {
                inheritIO()
                inheritJavaHome()
                directory(projectDir.toFile())
            }

            val diktatExitCode = diktatCommand.start().waitFor()
            softly.assertThat(diktatExitCode)
                .describedAs("Diktat exit code")
                .isEqualTo(1)
        }
    }

    companion object {
        private const val TEST_TIMEOUT_MINUTES = 5L

        /**
         * The directory where `diktat.jar` and `ktlint` far JARs are stored.
         */
        @TempDir
        private lateinit var diktatLibDir: Path

        private val diktatJar: Path
            get() =
                diktatLibDir / DIKTAT_FAT_JAR

        private val ktlintJar: Path
            get() =
                diktatLibDir / KTLINT_FAT_JAR

        /**
         * Returns `true` if the "parent" (JUnit) JVM is running under debugger.
         */
        private val isJvmDebugMode: Boolean
            get() =
                ManagementFactory.getRuntimeMXBean().inputArguments.any { inputArgument ->
                    inputArgument.startsWith("-agentlib:jdwp=")
                }

        /**
         * Returns the necessary debug flags for the "child" (Diktat) JVM.
         *
         * Note that `suspend` is set to `y`.
         */
        private val jvmDebugFlags: Array<out String> by lazy {
            when {
                isJvmDebugMode -> arrayOf("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y")
                else -> emptyArray()
            }
        }

        private val diktatCommandLinePrefix: Array<out String> by lazy {
            arrayOf(
                "java",
                "-Dfile.encoding=$UTF_8",
                *jvmDebugFlags,
                "-jar",
                ktlintJar.pathString,
                "-R",
                diktatJar.pathString,
                ktlintDisabledRulesArgument,
            )
        }

        private val availableProcessors: Int =
            Runtime.getRuntime().availableProcessors()

        @OptIn(ExperimentalContracts::class)
        private fun diktatProcessBuilder(vararg args: String, init: ProcessBuilder.() -> Unit = {}): ProcessBuilder {
            contract {
                callsInPlace(init, EXACTLY_ONCE)
            }

            return ProcessBuilder(*diktatCommandLinePrefix, *args).apply {
                init()
            }
        }

        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
            assertSoftly { softly ->
                checkForkedJavaHome()

                /*
                 * The fat JAR should reside in the same directory as `ktlint` and
                 * `save*` and be named `diktat.jar`
                 * (see `diktat-rules/src/test/resources/test/smoke/save.toml`).
                 */
                val buildDirectory = Path(BUILD_DIRECTORY)
                softly.assertThat(buildDirectory)
                    .isDirectory
                val diktatJarSource = buildDirectory
                    .takeIf(Path::exists)
                    ?.listDirectoryEntries(DIKTAT_FAT_JAR_GLOB)
                    ?.singleOrNull()
                softly.assertThat(diktatJarSource)
                    .describedAs(diktatJarSource?.toString() ?: "$BUILD_DIRECTORY/$DIKTAT_FAT_JAR_GLOB")
                    .isNotNull
                    .isRegularFile

                val ktlintJarSource = buildDirectory / KTLINT_FAT_JAR
                if (!ktlintJarSource.isRegularFile()) {
                    downloadFile(
                        from = URL("https://github.com/pinterest/ktlint/releases/download/$KTLINT_VERSION/ktlint"),
                        to = ktlintJarSource,
                        baseDirectory = buildDirectory
                    )
                }

                diktatJarSource?.copyTo(diktatJar)
                ktlintJarSource.copyTo(ktlintJar)
            }
        }
    }
}
