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
 * @return KtLint [FormatCallback] from [DiktatCallback] or exception
 */
fun DiktatCallback.unwrap(): FormatCallback = { error, isCorrected ->
    this.accept(error.wrap(), isCorrected)
}

/**
 * @return KtLint [FormatCallback] from [DiktatCallback] or exception
 */
fun DiktatCallback.unwrapForLint(): LintCallback = { error ->
    this.accept(error.wrap(), false)
}
