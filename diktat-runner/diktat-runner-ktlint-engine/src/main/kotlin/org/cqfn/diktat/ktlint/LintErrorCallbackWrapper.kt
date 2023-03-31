/**
 * This file contains utility methods for LintErrorCallback
 */

package org.cqfn.diktat.ktlint

import org.cqfn.diktat.api.DiktatCallback
import org.cqfn.diktat.ktlint.DiktatErrorImpl.Companion.unwrap
import org.cqfn.diktat.ktlint.DiktatErrorImpl.Companion.wrap
import org.cqfn.diktat.ruleset.utils.LintErrorCallback

/**
 * @return [DiktatCallback] from KtLint [LintErrorCallback]
 */
fun LintErrorCallback.wrap(): DiktatCallback = DiktatCallback { error, isCorrected ->
    this(error.unwrap(), isCorrected)
}

/**
 * @return KtLint [LintErrorCallback] from [DiktatCallback] or exception
 */
fun DiktatCallback.unwrap(): LintErrorCallback = { error, isCorrected ->
    this(error.wrap(), isCorrected)
}
