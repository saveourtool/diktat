/**
 * Utility classes and methods for tests
 */

package org.cqfn.diktat.util

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.utils.loggerWithKtlintConfig
import org.cqfn.diktat.ruleset.constants.EmitType

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import com.pinterest.ktlint.core.api.FeatureInAlphaState
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

import java.io.File
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
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
internal fun Path.deleteIfExistsSilently() {
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
internal fun <T> retry(
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
