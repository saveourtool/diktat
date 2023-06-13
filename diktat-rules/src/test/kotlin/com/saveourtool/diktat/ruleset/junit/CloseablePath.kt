package com.saveourtool.diktat.ruleset.junit

import com.saveourtool.diktat.test.framework.util.resetPermissions
import com.saveourtool.diktat.test.framework.util.tryToDeleteOnExit
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource
import java.io.IOException
import java.nio.file.DirectoryNotEmptyException
import java.nio.file.FileVisitResult
import java.nio.file.FileVisitResult.CONTINUE
import java.nio.file.Files.walkFileTree
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.SortedMap
import kotlin.io.path.absolute
import kotlin.io.path.deleteExisting
import kotlin.io.path.isDirectory
import kotlin.io.path.notExists
import kotlin.io.path.relativeToOrSelf

/**
 * @property directory the temporary directory (will be recursively deleted once
 *   the test completes).
 */
data class CloseablePath(val directory: Path) : CloseableResource {
    @Throws(IOException::class)
    override fun close() {
        val failures = deleteAllFilesAndDirectories()

        if (failures.isNotEmpty()) {
            throw failures.toIoException()
        }
    }

    @Suppress("TOO_LONG_FUNCTION")
    @Throws(IOException::class)
    private fun deleteAllFilesAndDirectories(): SortedMap<Path, IOException> {
        if (directory.notExists()) {
            return emptyMap<Path, IOException>().toSortedMap()
        }

        val failures: SortedMap<Path, IOException> = sortedMapOf()
        directory.resetPermissions()
        walkFileTree(directory, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (dir != directory) {
                    dir.resetPermissions()
                }
                return CONTINUE
            }

            override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
                /*
                 * `IOException` includes `AccessDeniedException` thrown by
                 * non-readable or non-executable flags.
                 */
                resetPermissionsAndTryToDeleteAgain(file, exc)
                return CONTINUE
            }

            override fun visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult =
                file.deleteAndContinue()

            override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult =
                dir.deleteAndContinue()

            private fun Path.deleteAndContinue(): FileVisitResult {
                try {
                    deleteExisting()
                } catch (_: NoSuchFileException) {
                    /*
                     * Ignore.
                     */
                } catch (dnee: DirectoryNotEmptyException) {
                    failures[this] = dnee
                } catch (ioe: IOException) {
                    /*
                     * `IOException` includes `AccessDeniedException` thrown by
                     * non-readable or non-executable flags.
                     */
                    resetPermissionsAndTryToDeleteAgain(this, ioe)
                }
                return CONTINUE
            }

            private fun resetPermissionsAndTryToDeleteAgain(path: Path, ioe: IOException) {
                try {
                    path.resetPermissions()
                    if (path.isDirectory()) {
                        walkFileTree(path, this)
                    } else {
                        path.deleteExisting()
                    }
                } catch (suppressed: Exception) {
                    ioe.addSuppressed(suppressed)
                    failures[path] = ioe
                }
            }
        })
        return failures
    }

    private fun SortedMap<Path, IOException>.toIoException(): IOException {
        @Suppress("WRONG_NEWLINES")  // False positives, see #1495.
        val joinedPaths = keys
            .asSequence()
            .map(Path::tryToDeleteOnExit)
            .map { path ->
                path.relativeToOrSelf(directory)
            }
            .map(Any::toString)
            .joinToString()

        return IOException("Failed to delete temp directory ${directory.absolute()}. " +
                "The following paths could not be deleted (see suppressed exceptions for details): $joinedPaths").apply {
            values.forEach(this::addSuppressed)
        }
    }
}
