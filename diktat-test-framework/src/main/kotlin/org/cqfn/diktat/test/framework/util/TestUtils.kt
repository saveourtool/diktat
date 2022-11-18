/**
 * Utility classes and methods for tests
 */

package org.cqfn.diktat.test.framework.util

import org.cqfn.diktat.common.utils.loggerWithKtlintConfig

import mu.KotlinLogging

import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.Files
import java.nio.file.Files.walkFileTree
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

import kotlin.io.path.absolute
import kotlin.io.path.bufferedReader
import kotlin.io.path.deleteExisting
import kotlin.io.path.deleteIfExists
import kotlin.io.path.isDirectory
import kotlin.io.path.isSameFileAs

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
 * @receiver the 1st operand.
 * @param other the 2nd operand.
 * @return `true` if, and only if, the two paths locate the same `JAVA_HOME`.
 */
fun Path.isSameJavaHomeAs(other: Path): Boolean =
    isDirectory() &&
            (isSameFileAsSafe(other) ||
                    resolve("jre").isSameFileAsSafe(other) ||
                    other.resolve("jre").isSameFileAsSafe(this))

/**
 * The same as [Path.isSameFileAs], but doesn't throw any [NoSuchFileException]
 * if either of the operands doesn't exist.
 *
 * @receiver the 1st operand.
 * @param other the 2nd operand.
 * @return `true` if, and only if, the two paths locate the same file.
 * @see Path.isSameFileAs
 */
fun Path.isSameFileAsSafe(other: Path): Boolean =
    try {
        isSameFileAs(other)
    } catch (_: NoSuchFileException) {
        false
    }

/**
 * Requests that this file or directory be deleted when the JVM terminates.
 *
 * Does nothing if this [Path] is not associated with the default provider.
 *
 * @receiver a regular file or a directory.
 * @return this [Path].
 */
fun Path.tryToDeleteOnExit(): Path {
    try {
        toFile().deleteOnExit()
    } catch (_: UnsupportedOperationException) {
        /*
         * Ignore.
         */
    }

    return this
}

/**
 * Resets any permissions which might otherwise prevent from reading or writing
 * this file or directory, or traversing this directory.
 *
 * @receiver a regular file or a directory.
 */
fun Path.resetPermissions() {
    toFile().apply {
        setReadable(true)
        setWritable(true)

        if (isDirectory) {
            setExecutable(true)
        }
    }
}

/**
 * Returns a sequence containing only files whose content (the first
 * [linesToRead] lines) matches [lineRegex].
 *
 * The operation is _intermediate_ and _stateless_.
 *
 * @receiver a sequence of regular files.
 * @param linesToRead the number of lines to read (at most).
 * @param lineRegex the regular expression to be applied to each line until a
 *   match is found (i.e. the line is found which _contains_ [lineRegex]).
 * @return the filtered sequence.
 */
fun Sequence<Path>.filterContentMatches(linesToRead: Int, lineRegex: Regex): Sequence<Path> =
    filter { file ->
        file.bufferedReader().useLines { lines ->
            lines.take(linesToRead).any { line ->
                line.contains(lineRegex)
            }
        }
    }

/**
 * Prepends the `PATH` of this process builder with [pathEntry].
 *
 * @param pathEntry the entry to be prepended to the `PATH`.
 */
fun ProcessBuilder.prependPath(pathEntry: Path) {
    require(pathEntry.isDirectory()) {
        "$pathEntry is not a directory"
    }

    val environment = environment()

    val defaultPathKey = "PATH"
    val defaultWindowsPathKey = "Path"

    val pathKey = when {
        /*-
         * Keys of the Windows environment are case-insensitive ("PATH" == "Path").
         * Keys of the Java interface to the environment are not ("PATH" != "Path").
         * This is an attempt to work around the inconsistency.
         */
        System.getProperty("os.name").startsWith("Windows") -> environment.keys.firstOrNull { key ->
            key.equals(defaultPathKey, ignoreCase = true)
        } ?: defaultWindowsPathKey

        else -> defaultPathKey
    }

    val pathSeparator = File.pathSeparatorChar
    val oldPath = environment[pathKey]

    val newPath = when {
        oldPath.isNullOrEmpty() -> pathEntry.toString()
        else -> "$pathEntry$pathSeparator$oldPath"
    }

    environment[pathKey] = newPath
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
