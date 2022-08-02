package org.cqfn.diktat.ruleset.chapter3.spaces

import org.cqfn.diktat.ruleset.junit.ExpectedLintError

/**
 * The expected indentation error (extracted from annotated code fragments).
 *
 * @property line the line number (1-based).
 * @property column the column number (1-based).
 * @property expectedIndent the expected indentation level (in space characters).
 * @property actualIndent the actual indentation level (in space characters).
 */
data class ExpectedIndentationError(
    override val line: Int,
    override val column: Int = 1,
    val expectedIndent: Int,
    val actualIndent: Int
) : ExpectedLintError
