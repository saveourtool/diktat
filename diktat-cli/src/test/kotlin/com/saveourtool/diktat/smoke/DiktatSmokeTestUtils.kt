@file:Suppress("HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE")

package com.saveourtool.diktat.smoke

import com.saveourtool.diktat.test.framework.util.retry
import io.github.oshai.kotlinlogging.KotlinLogging
import org.assertj.core.api.Assertions.fail
import org.assertj.core.api.SoftAssertions
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.copyTo
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.outputStream
import kotlin.io.path.relativeToOrSelf
import kotlin.system.measureNanoTime

internal const val BUILD_DIRECTORY = "build/libs"
internal const val DIKTAT_CLI_JAR = "diktat.jar"
internal const val DIKTAT_CLI_JAR_GLOB = "diktat-cli-*.jar"

private val logger = KotlinLogging.logger {}

/**
 * Downloads the file from a remote URL, retrying if necessary.
 *
 * @param from the remote URL to download from.
 * @param to the target path.
 * @param baseDirectory the directory against which [to] should be relativized
 *   if it's absolute.
 */
@Suppress("FLOAT_IN_ACCURATE_CALCULATIONS")
internal fun downloadFile(
    from: URL,
    to: Path,
    baseDirectory: Path,
) {
    logger.info {
        "Downloading $from to ${to.relativeToOrSelf(baseDirectory)}..."
    }

    @Suppress("MAGIC_NUMBER")
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

/**
 * Copies the diktat-cli.jar with assertions
 *
 * @param softAssertions
 * @param to the target path.
 */
internal fun copyDiktatCli(
    softAssertions: SoftAssertions,
    to: Path
) {
    /*
     * The fat JAR should reside in the same directory as `save*` and
     * be named `diktat.jar`
     * (see `diktat-cli/src/test/resources/test/smoke/save.toml`).
     */
    val buildDirectory = Path(BUILD_DIRECTORY)
    softAssertions.assertThat(buildDirectory)
        .isDirectory
    val diktatFrom = buildDirectory
        .takeIf(Path::exists)
        ?.listDirectoryEntries(DIKTAT_CLI_JAR_GLOB)
        ?.singleOrNull()
    softAssertions.assertThat(diktatFrom)
        .describedAs(diktatFrom?.toString() ?: "$BUILD_DIRECTORY/$DIKTAT_CLI_JAR_GLOB")
        .isNotNull
        .isRegularFile

    diktatFrom?.copyTo(to, overwrite = true)
}
