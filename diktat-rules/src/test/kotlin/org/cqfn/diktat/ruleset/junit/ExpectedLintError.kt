package org.cqfn.diktat.ruleset.junit

import com.pinterest.ktlint.core.LintError

/**
 * The common super-interface for expected lint errors (extracted from the
 * annotated code).
 */
interface ExpectedLintError {
    /**
     * The line number (1-based).
     */
    val line: Int

    /**
     * The column number (1-based).
     */
    val column: Int

    /**
     * Converts this instance to a [LintError].
     *
     * @return the [LintError] which corresponds to this instance.
     */
    fun asLintError(): LintError
}
