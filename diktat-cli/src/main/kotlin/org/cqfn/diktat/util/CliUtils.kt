/**
 * This class contains util methods to operate with java.nio.file.Path for CLI
 */

package org.cqfn.diktat.util

import java.nio.file.FileSystem
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.PathWalkOption
import kotlin.io.path.exists
import kotlin.io.path.walk

/**
 * Create a matcher and return a filter that uses it.
 *
 * @param glob glob pattern to filter files
 * @return a sequence of files which matches to [glob]
 */
@OptIn(ExperimentalPathApi::class)
fun Path.walkByGlob(glob: String): Sequence<Path> = run {
    fileSystem.globMatcher(glob) to fileSystem.relativeGlobMatcher(glob)
}.let { (matcher, relativeMatcher) ->
    this.walk(PathWalkOption.INCLUDE_DIRECTORIES)
        .filter { matcher.matches(it) || relativeMatcher.matches(it) }
}

private fun FileSystem.globMatcher(glob: String) = getPathMatcher("glob:${glob.replace(java.io.File.separatorChar, '/')}")

private fun FileSystem.relativeGlobMatcher(glob: String) = globMatcher("**/$glob")

/**
 * @return path or null if path is invalid or doesn't exist
 */
fun String.tryToPathIfExists(): Path? = try {
    Paths.get(this).takeIf { it.exists() }
} catch (e: InvalidPathException) {
    null
}
