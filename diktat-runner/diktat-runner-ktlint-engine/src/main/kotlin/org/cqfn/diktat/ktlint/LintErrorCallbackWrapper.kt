/**
 * This file contains utility methods for LintErrorCallback
 */

package org.cqfn.diktat.ktlint

import org.cqfn.diktat.api.DiktatCallback
import org.cqfn.diktat.ruleset.utils.FormatCallback
import org.cqfn.diktat.ruleset.utils.LintCallback

/**
 * @return [DiktatCallback] from KtLint [FormatCallback]
 */
fun FormatCallback.wrap(): DiktatCallback = DiktatCallback { error, isCorrected ->
    this(error.unwrap(), isCorrected)
}

/**
 * @return [DiktatCallback] from KtLint [LintCallback]
 */
fun LintCallback.wrap(): DiktatCallback = DiktatCallback { error, _ ->
    this(error.unwrap())
}

/**
 * @return KtLint [FormatCallback] from [DiktatCallback] or exception
 */
fun DiktatCallback.unwrapForFormat(): FormatCallback = { error, isCorrected ->
    this(error.wrap(), isCorrected)
}

/**
 * @return KtLint [LintCallback] from [DiktatCallback] or exception
 */
fun DiktatCallback.unwrapForLint(): LintCallback = { error ->
    this(error.wrap(), false)
}
