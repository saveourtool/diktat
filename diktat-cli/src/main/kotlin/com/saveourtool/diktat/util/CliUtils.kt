/**
 * This class contains util methods to operate with java.nio.file.Path for CLI
 */

package com.saveourtool.diktat.util

import java.io.File
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.PathWalkOption
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.walk

// all roots
private val roots: Set<String> = FileSystems.getDefault()
    .rootDirectories
    .asSequence()
    .map { it.absolutePathString() }
    .toSet()

private const val parentDirectoryPrefix = 3
private const val parentDirectoryUnix = "../"
private const val parentDirectoryWindows = "..\\"

/**
 * Create a matcher and return a filter that uses it.
 *
 * @param glob glob pattern to filter files
 * @return a sequence of files which matches to [glob]
 */
@OptIn(ExperimentalPathApi::class)
fun Path.walkByGlob(glob: String): Sequence<Path> = if (glob.startsWith(parentDirectoryUnix) || glob.startsWith(parentDirectoryWindows)) {
    parent?.walkByGlob(glob.substring(parentDirectoryPrefix)) ?: emptySequence()
} else {
    fileSystem.globMatcher(glob)
        .let { matcher ->
            walk(PathWalkOption.INCLUDE_DIRECTORIES).filter { matcher.matches(it) }
        }
}

private fun FileSystem.globMatcher(glob: String): PathMatcher = if (isAbsoluteGlob(glob)) {
    getPathMatcher("glob:$glob")
} else {
    getPathMatcher("glob:**${File.separatorChar}$glob")
}

private fun isAbsoluteGlob(glob: String): Boolean = glob.startsWith("**") || roots.any { glob.startsWith(it, true) }

/**
 * @return path or null if path is invalid or doesn't exist
 */
fun String.tryToPathIfExists(): Path? = try {
    Paths.get(this).takeIf { it.exists() }
} catch (e: InvalidPathException) {
    null
}
