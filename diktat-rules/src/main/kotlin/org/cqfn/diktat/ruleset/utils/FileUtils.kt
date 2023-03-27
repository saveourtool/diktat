/**
 * Utility methods to work with file paths.
 */

package org.cqfn.diktat.ruleset.utils

import java.nio.file.Path
import kotlin.io.path.extension

internal const val SRC_DIRECTORY_NAME = "src"
private const val KOTLIN_EXTENSION = "kt"
private const val KOTLIN_SCRIPT_EXTENSION = KOTLIN_EXTENSION + "s"

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
 * Checks if [this] [String] is a name of a kotlin script file by checking whether file extension equals 'kts'
 *
 * @return true if this is a kotlin script file name, false otherwise
 */
fun String.isKotlinScript() = endsWith(".$KOTLIN_SCRIPT_EXTENSION")

/**
 * Check if [this] [Path] is a kotlin script by checking whether an extension equals to 'kts'
 *
 * @return true if this is a kotlin script file name, false otherwise
 */
fun Path.isKotlinScript() = this.extension.lowercase() == KOTLIN_SCRIPT_EXTENSION

/**
 * Check if [this] [Path] is a kotlin code or script by checking whether an extension equals to `kt` or 'kts'
 *
 * @return true if this is a kotlin code or script file name, false otherwise
 */
fun Path.isKotlinCodeOrScript() = this.extension.lowercase() in setOf(KOTLIN_EXTENSION, KOTLIN_SCRIPT_EXTENSION)

/**
 * Checks if [this] String is a name of a gradle kotlin script file by checking whether file extension equals 'gradle.kts'
 *
 * @return true if this is a gradle kotlin script file name, false otherwise
 */
fun String.isGradleScript() = endsWith("gradle.kts")
