package org.cqfn.diktat.api

import java.nio.file.Path

/**
 * A base interface for Baseline
 */
fun interface DiktatBaseline {
    /**
     * @param file
     * @return a set of [DiktatError] found in baseline by [file]
     */
    fun errorsByFile(file: Path): Set<DiktatError>

    companion object {
        /**
         * Empty [DiktatBaseline]
         */
        val empty: DiktatBaseline = DiktatBaseline { _ -> emptySet() }

        /**
         * @param baseline
         * @return wrapped [DiktatProcessorListener] which skips known errors based on [baseline]
         */
        fun DiktatProcessorListener.skipKnownErrors(baseline: DiktatBaseline): DiktatProcessorListener = object : DiktatProcessorListener {
            override fun onError(
                file: Path,
                error: DiktatError,
                isCorrected: Boolean
            ) {
                if (!baseline.errorsByFile(file).contains(error)) {
                    this@skipKnownErrors.onError(file, error, isCorrected)
                }
            }

            override fun beforeAll() = this@skipKnownErrors.beforeAll()
            override fun before(file: Path) = this@skipKnownErrors.before(file)
            override fun after(file: Path) = this@skipKnownErrors.after(file)
            override fun afterAll() = this@skipKnownErrors.afterAll()
        }
    }
}
