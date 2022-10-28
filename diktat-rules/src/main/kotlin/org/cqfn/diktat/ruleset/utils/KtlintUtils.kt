@file:Suppress("HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE")

package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.KtLint.ExperimentalParams
import com.pinterest.ktlint.core.LintError

/**
 * Enables ignoring autocorrected errors when in "fix" mode (i.e. when
 * [KtLint.format] is invoked).
 *
 * Before version 0.47, _Ktlint_ only reported non-corrected errors in "fix"
 * mode.
 * Now, this has changed.
 *
 * @receiver the instance of _Ktlint_ parameters.
 * @return the instance with the [callback][ExperimentalParams.cb] modified in
 *   such a way that it ignores corrected errors.
 * @see KtLint.format
 * @see ExperimentalParams.cb
 * @since 1.2.4
 */
fun ExperimentalParams.ignoreCorrectedErrors(): ExperimentalParams =
    copy(cb = { error: LintError, corrected: Boolean ->
        if (!corrected) {
            cb(error, false)
        }
    })
