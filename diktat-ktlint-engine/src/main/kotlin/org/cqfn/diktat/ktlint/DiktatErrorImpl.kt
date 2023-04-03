package org.cqfn.diktat.ktlint

import org.cqfn.diktat.api.DiktatError
import com.pinterest.ktlint.core.LintError

/**
 * Wrapper for KtLint error
 *
 * @property lintError
 */
data class DiktatErrorImpl(
    private val lintError: LintError
) : DiktatError {
    override fun getLine(): Int = lintError.line

    override fun getCol(): Int = lintError.col

    override fun getRuleId(): String = lintError.ruleId

    override fun getDetail(): String = lintError.detail

    override fun canBeAutoCorrected(): Boolean = lintError.canBeAutoCorrected

    companion object {
        /**
         * @return [DiktatError] from KtLint [LintError]
         */
        fun LintError.wrap(): DiktatError = DiktatErrorImpl(this)

        /**
         * @return KtLint [LintError] from [DiktatError] or exception
         */
        fun DiktatError.unwrap(): LintError = (this as? DiktatErrorImpl)?.lintError
            ?: error("Unsupported wrapper of ${DiktatError::class.java.simpleName}: ${this::class.java.canonicalName}")
    }
}

