/**
 * Utility methods to work with file paths.
 */

package com.saveourtool.diktat.ruleset.utils

internal const val SRC_DIRECTORY_NAME = "src"

/**
 * Splits [this] string by file path separator.
 *
 * @return list of path parts
 */
fun String.splitPathToDirs(): List<String> =
    this.replace("\\", "/")
        .replace("//", "/")
        .split("/")

/**
 * Checks if [this] String is a name of a gradle kotlin script file by checking whether file extension equals 'gradle.kts'
 *
 * @return true if this is a gradle kotlin script file name, false otherwise
 */
fun String.isGradleScript() = endsWith("gradle.kts")
