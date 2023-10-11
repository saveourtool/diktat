package com.saveourtool.diktat.ktlint

import com.pinterest.ktlint.cli.reporter.core.api.KtlintCliError
import com.pinterest.ktlint.cli.reporter.core.api.ReporterV2

/**
 * Wrapper for [ReporterV2]
 *
 * @param reporterV2
 */
open class ReporterV2Wrapper(private val reporterV2: ReporterV2) : ReporterV2 {
    override fun beforeAll() = reporterV2.beforeAll()

    override fun before(file: String) = reporterV2.before(file)

    override fun onLintError(file: String, ktlintCliError: KtlintCliError) = reporterV2.onLintError(file, ktlintCliError)

    override fun after(file: String) = reporterV2.after(file)

    override fun afterAll() = reporterV2.afterAll()

    companion object {
        /**
         * @return unwrapped [ReporterV2Wrapper] if it's required
         */
        fun ReporterV2.unwrapIfNeeded(): ReporterV2 = if (this is ReporterV2Wrapper) {
            this.reporterV2
        } else {
            this
        }
    }
}
