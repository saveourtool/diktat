package org.cqfn.diktat.test.framework.util

import org.cqfn.diktat.common.utils.loggerWithKtlintConfig
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSetProvider
import mu.KotlinLogging
import org.intellij.lang.annotations.Language

@Suppress("EMPTY_BLOCK_STRUCTURE_ERROR")
private val log = KotlinLogging.loggerWithKtlintConfig {}

@Suppress("TYPE_ALIAS")
val defaultCallback: (lintError: LintError, corrected: Boolean) -> Unit = { lintError, _ ->
    log.warn("Received linting error: $lintError")
}

typealias LintErrorCallback = (LintError, Boolean) -> Unit

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
        KtLint.ExperimentalParams(
            text = text,
            ruleSets = ruleSets,
            fileName = fileName.removeSuffix("_copy"),
            script = fileName.removeSuffix("_copy").endsWith("kts"),
            cb = cb,
            debug = true,
        )
    )
}
