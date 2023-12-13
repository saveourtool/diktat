/**
 * This class contains util methods to operate with java.nio.file.Path for CLI
 */

package com.saveourtool.diktat.util

import java.io.File
import java.nio.file.FileSystems
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.walk

private const val NEGATIVE_PREFIX_PATTERN = "!"
private const val PARENT_DIRECTORY_PREFIX = 3
private const val PARENT_DIRECTORY_UNIX = "../"
private const val PARENT_DIRECTORY_WINDOWS = "..\\"

// all roots
private val roots: Set<String> = FileSystems.getDefault()
    .rootDirectories
    .asSequence()
    .map { it.absolutePathString() }
    .toSet()

/**
 * Lists all files in [this] directory based on [patterns]
 *
 * @param patterns a path to a file or a directory (all files from this directory will be returned) or an [Ant-style path pattern](https://ant.apache.org/manual/dirtasks.html#patterns)
 * @return [Sequence] of files as [Path] matched to provided [patterns]
 */
fun Path.listFiles(
    vararg patterns: String,
): Sequence<Path> {
    val (includePatterns, excludePatterns) = patterns.partition { !it.startsWith(NEGATIVE_PREFIX_PATTERN) }
    val exclude by lazy {
        doListFiles(excludePatterns.map { it.removePrefix(NEGATIVE_PREFIX_PATTERN) })
            .toSet()
    }
    return doListFiles(includePatterns).filterNot { exclude.contains(it) }
}

@OptIn(ExperimentalPathApi::class)
private fun Path.doListFiles(patterns: List<String>): Sequence<Path> = patterns
    .asSequence()
    .flatMap { pattern ->
        tryToResolveIfExists(pattern, this)?.walk() ?: walkByGlob(pattern)
    }
    .map { it.normalize() }
    .map { it.toAbsolutePath() }
    .distinct()

/**
 * Create a matcher and return a filter that uses it.
 *
 * @param glob glob pattern to filter files
 * @return a sequence of files which matches to [glob]
 */
@OptIn(ExperimentalPathApi::class)
private fun Path.walkByGlob(glob: String): Sequence<Path> = if (glob.startsWith(PARENT_DIRECTORY_UNIX) || glob.startsWith(PARENT_DIRECTORY_WINDOWS)) {
    parent?.walkByGlob(glob.substring(PARENT_DIRECTORY_PREFIX)) ?: emptySequence()
} else {
    getAbsoluteGlobAndRoot(glob, this)
        .let { (absoluteGlob, root) ->
            absoluteGlob
                .replace("([^\\\\])\\\\([^\\\\])".toRegex(), "$1\\\\\\\\$2")  // encode Windows separators
                .let { root.fileSystem.getPathMatcher("glob:$it") }
                .let { matcher ->
                    root.walk().filter { matcher.matches(it) }
                }
        }
}

private fun String.findRoot(): Path = substring(0, indexOf('*'))
    .let { withoutAsterisks ->
        withoutAsterisks.substring(0, withoutAsterisks.lastIndexOfAny(charArrayOf('\\', '/')))
    }
    .let { Path(it) }

/**
 * @param candidate
 * @param currentDirectory
 * @return path or null if path is invalid or doesn't exist
 */
private fun tryToResolveIfExists(candidate: String, currentDirectory: Path): Path? = try {
    Paths.get(candidate).takeIf { it.exists() }
        ?: currentDirectory.resolve(candidate).takeIf { it.exists() }
} catch (e: InvalidPathException) {
    null
}

private fun getAbsoluteGlobAndRoot(glob: String, currentFolder: Path): Pair<String, Path> = when {
    glob.startsWith("**") -> glob to currentFolder
    roots.any { glob.startsWith(it, true) } -> glob to glob.findRoot()
    else -> "${currentFolder.absolutePathString()}${File.separatorChar}$glob" to currentFolder
}
