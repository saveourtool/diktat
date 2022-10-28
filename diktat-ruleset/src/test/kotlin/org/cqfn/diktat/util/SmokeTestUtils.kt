/**
 * Utility classes and methods for tests
 */

package org.cqfn.diktat.util

import org.cqfn.diktat.common.utils.loggerWithKtlintConfig

import com.pinterest.ktlint.core.LintError
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly

import java.io.File
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.util.function.Consumer
import kotlin.io.path.absolute
import kotlin.io.path.deleteIfExists
import kotlin.io.path.isDirectory
import kotlin.io.path.isSameFileAs

internal const val TEST_FILE_NAME = "TestFileName.kt"

@Suppress("EMPTY_BLOCK_STRUCTURE_ERROR")
private val log = KotlinLogging.loggerWithKtlintConfig {}

@Suppress("TYPE_ALIAS")
internal val defaultCallback: (lintError: LintError, corrected: Boolean) -> Unit = { lintError, _ ->
    log.warn("Received linting error: $lintError")
}

typealias LintErrorCallback = (LintError, Boolean) -> Unit

/**
 * Compare [LintError]s from [this] with [expectedLintErrors]
 *
 * @param expectedLintErrors expected [LintError]s
 */
internal fun List<LintError>.assertEquals(vararg expectedLintErrors: LintError) {
    if (size == expectedLintErrors.size) {
        assertThat(this)
            .allSatisfy(Consumer { actual ->
                val expected = expectedLintErrors[this@assertEquals.indexOf(actual)]
                assertSoftly { sa ->
                    sa
                        .assertThat(actual.line)
                        .`as`("Line")
                        .isEqualTo(expected.line)
                    sa
                        .assertThat(actual.col)
                        .`as`("Column")
                        .isEqualTo(expected.col)
                    sa
                        .assertThat(actual.ruleId)
                        .`as`("Rule id")
                        .isEqualTo(expected.ruleId)
                    sa
                        .assertThat(actual.detail)
                        .`as`("Detailed message")
                        .isEqualTo(expected.detail)
                    sa
                        .assertThat(actual.canBeAutoCorrected)
                        .`as`("Can be autocorrected")
                        .isEqualTo(expected.canBeAutoCorrected)
                }
            })
    } else {
        assertThat(this).containsExactly(*expectedLintErrors)
    }
}

/**
 * @receiver the 1st operand.
 * @param other the 2nd operand.
 * @return `true` if, and only if, the two paths locate the same `JAVA_HOME`.
 */
internal fun Path.isSameJavaHomeAs(other: Path): Boolean =
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
internal fun Path.isSameFileAsSafe(other: Path): Boolean =
    try {
        isSameFileAs(other)
    } catch (_: NoSuchFileException) {
        false
    }

/**
 * Prepends the `PATH` of this process builder with [pathEntry].
 *
 * @param pathEntry the entry to be prepended to the `PATH`.
 */
internal fun ProcessBuilder.prependPath(pathEntry: Path) {
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

