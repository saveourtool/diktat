@file:Suppress(
    "HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE",
    "Deprecation",
)

package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.KtLint.ExperimentalParams
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSetProvider
import mu.KotlinLogging
import org.intellij.lang.annotations.Language

private val log = KotlinLogging.logger {}

@Suppress("TYPE_ALIAS")
val defaultCallback: (lintError: LintError, corrected: Boolean) -> Unit = { lintError, _ ->
    log.warn("Received linting error: $lintError")
}

typealias LintErrorCallback = (LintError, Boolean) -> Unit

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

/**
 * @param ruleSetProviderRef
 * @param text
 * @param fileName
 * @param cb callback to be called on unhandled [LintError]s
 * @return formatted code
 */
@Suppress("LAMBDA_IS_NOT_LAST_PARAMETER")
fun format(
    ruleSetProviderRef: () -> RuleSetProvider,
    @Language("kotlin") text: String,
    fileName: String,
    cb: LintErrorCallback = defaultCallback
): String {
    val ruleSets = listOf(ruleSetProviderRef().get())
    return KtLint.format(
        ExperimentalParams(
            text = text,
            ruleSets = ruleSets,
            fileName = fileName.removeSuffix("_copy"),
            script = fileName.removeSuffix("_copy").endsWith("kts"),
            cb = cb,
            debug = true,
        ).ignoreCorrectedErrors()
    )
}
