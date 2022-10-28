/**
 * Utility classes and methods for tests
 */

package org.cqfn.diktat.test.framework.util

import mu.KotlinLogging
import org.cqfn.diktat.common.utils.loggerWithKtlintConfig

import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.Files.walkFileTree
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.NoSuchFileException
import java.nio.file.attribute.BasicFileAttributes

import kotlin.io.path.absolute
import kotlin.io.path.deleteExisting
import kotlin.io.path.deleteIfExists

@Suppress("EMPTY_BLOCK_STRUCTURE_ERROR")
private val log = KotlinLogging.loggerWithKtlintConfig {}

/**
 * Deletes the file if it exists, retrying as necessary if the file is
 * blocked by another process (on Windows).
 *
 * @receiver the file or empty directory.
 * @see Path.deleteIfExists
 */
@Suppress(
    "EMPTY_BLOCK_STRUCTURE_ERROR",
    "MAGIC_NUMBER",
)
fun Path.deleteIfExistsSilently() {
    val attempts = 10

    val deleted = retry(attempts, delayMillis = 100L, lazyDefault = { false }) {
        deleteIfExists()

        /*
         * Ignore the return code of `deleteIfExists()` (will be `false`
         * if the file doesn't exist).
         */
        true
    }

    if (!deleted) {
        log.warn {
            "File \"${absolute()}\" not deleted after $attempts attempt(s)."
        }
    }
}

/**
 * Deletes this directory recursively.
 *
 * @see Path.deleteIfExistsRecursively
 */
fun Path.deleteRecursively() {
    walkFileTree(this, object : SimpleFileVisitor<Path>() {
        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            file.deleteIfExistsSilently()
            return CONTINUE
        }

        override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
            dir.deleteExisting()
            return CONTINUE
        }
    })
}

/**
 * Deletes this directory recursively if it exists.
 *
 * @return `true` if the existing directory was successfully deleted, `false` if
 *   the directory doesn't exist.
 * @see Files.deleteIfExists
 * @see Path.deleteRecursively
 */
@Suppress("FUNCTION_BOOLEAN_PREFIX")
fun Path.deleteIfExistsRecursively(): Boolean =
    try {
        deleteRecursively()
        true
    } catch (_: NoSuchFileException) {
        false
    }

/**
 * Retries the execution of the [block].
 *
 * @param attempts the number of attempts (must be positive).
 * @param delayMillis the timeout (in milliseconds) between the consecutive
 *   attempts. The default is 0. Ignored if [attempts] is 1.
 * @param lazyDefault allows to override the return value if none of the
 *   attempts succeeds. By default, the last exception is thrown.
 * @param block the block to execute.
 * @return the result of the execution of the [block], or whatever [lazyDefault]
 *   evaluates to if none of the attempts is successful.
 */
fun <T> retry(
    attempts: Int,
    delayMillis: Long = 0L,
    lazyDefault: (Throwable) -> T = { error -> throw error },
    block: () -> T
): T {
    require(attempts > 0) {
        "The number of attempts should be positive: $attempts"
    }

    var lastError: Throwable? = null

    for (i in 1..attempts) {
        try {
            return block()
        } catch (error: Throwable) {
            lastError = error
        }

        if (delayMillis > 0L) {
            Thread.sleep(delayMillis)
        }
    }

    return lazyDefault(lastError ?: Exception("The block was never executed"))
}
