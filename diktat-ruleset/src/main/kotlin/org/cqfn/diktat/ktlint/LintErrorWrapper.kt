package org.cqfn.diktat.ktlint

import org.cqfn.diktat.api.DiktatError
import com.pinterest.ktlint.core.LintError

/**
 * Wrapper for KtLint error
 *
 * @property lintError
 */
data class LintErrorWrapper(
    val lintError: LintError
) : DiktatError {
    override fun getLine(): Int = lintError.line

    override fun getCol(): Int = lintError.col

    override fun getRuleId(): String = lintError.ruleId

    override fun getDetail(): String = lintError.detail

    override fun canBeAutoCorrected(): Boolean = lintError.canBeAutoCorrected
}

/**
 * @return [DiktatError] from KtLint [LintError]
 */
fun LintError.wrap(): DiktatError = LintErrorWrapper(this)

/**
 * @return KtLint [LintError] from [DiktatError] or exception
 */
fun DiktatError.unwrap(): LintError = (this as? LintErrorWrapper)?.lintError ?: error("Unsupported wrapper for ${DiktatError::class.java.simpleName}")
