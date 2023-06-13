/**
 * Utility methods to work with file paths.
 */

package com.saveourtool.diktat.util

import java.nio.file.Path
import kotlin.io.path.extension

private const val KOTLIN_EXTENSION = "kt"
private const val KOTLIN_SCRIPT_EXTENSION = KOTLIN_EXTENSION + "s"

/**
 * Checks if [this] [String] is a name of a kotlin script file by checking whether file extension equals 'kts'
 *
 * @return true if this is a kotlin script file name, false otherwise
 */
fun String.isKotlinScript() = endsWith(".$KOTLIN_SCRIPT_EXTENSION", true)

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
