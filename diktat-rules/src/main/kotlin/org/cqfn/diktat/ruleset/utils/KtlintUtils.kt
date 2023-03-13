@file:Suppress(
    "HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE",
)

package org.cqfn.diktat.ruleset.utils

import org.cqfn.diktat.common.utils.loggerWithKtlintConfig
import com.pinterest.ktlint.core.Code
import com.pinterest.ktlint.core.KtLintRuleEngine
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleSetProviderV2
import mu.KotlinLogging
import org.intellij.lang.annotations.Language

@Suppress("EMPTY_BLOCK_STRUCTURE_ERROR")
private val log = KotlinLogging.loggerWithKtlintConfig {}

@Suppress("TYPE_ALIAS")
val defaultCallback: (lintError: LintError, corrected: Boolean) -> Unit = { lintError, _ ->
    log.warn("Received linting error: $lintError")
}

typealias LintCallback = (LintError) -> Unit
typealias FormatCallback = (LintError, Boolean) -> Unit

/**
 * @param ruleSetProviderRef
 * @param text
 * @param fileName
 * @param cb callback to be called on unhandled [LintError]s
 * @return formatted code
 */
@Suppress("LAMBDA_IS_NOT_LAST_PARAMETER")
fun format(
    ruleSetProviderRef: () -> RuleSetProviderV2,
    @Language("kotlin") text: String,
    fileName: String,
    cb: FormatCallback = defaultCallback
): String {
    val ruleProviders = ruleSetProviderRef().getRuleProviders()
    val ktLintRuleEngine = KtLintRuleEngine(
        ruleProviders = ruleProviders
    )
    val code: Code = Code.CodeSnippet(
        content = text,
        script = fileName.removeSuffix("_copy").endsWith("kts")
    )
    return ktLintRuleEngine.format(code) { error: LintError, corrected: Boolean ->
        if (!corrected) {
            cb(error, false)
        }
    }
}
