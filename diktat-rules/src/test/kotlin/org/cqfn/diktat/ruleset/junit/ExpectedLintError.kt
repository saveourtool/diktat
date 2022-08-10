package org.cqfn.diktat.ruleset.junit

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
}
