package com.saveourtool.diktat.ruleset.junit

import com.saveourtool.diktat.api.DiktatError

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
     * Converts this instance to a [DiktatError].
     *
     * @return the [DiktatError] which corresponds to this instance.
     */
    fun asLintError(): DiktatError
}
