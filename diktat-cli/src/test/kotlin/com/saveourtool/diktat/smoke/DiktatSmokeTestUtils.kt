@file:Suppress("HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE")

package com.saveourtool.diktat.smoke

import com.saveourtool.diktat.test.framework.util.retry
import io.github.oshai.kotlinlogging.KotlinLogging
import org.assertj.core.api.Assertions.fail
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.outputStream
import kotlin.io.path.relativeToOrSelf
import kotlin.system.measureNanoTime

internal const val BUILD_DIRECTORY = "build/libs"
internal const val DIKTAT_FAT_JAR = "diktat.jar"
internal const val DIKTAT_FAT_JAR_GLOB = "diktat-*.jar"
internal const val KTLINT_FAT_JAR = "ktlint"

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
