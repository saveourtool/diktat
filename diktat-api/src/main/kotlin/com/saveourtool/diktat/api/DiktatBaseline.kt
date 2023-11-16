package com.saveourtool.diktat.api

import com.saveourtool.diktat.util.DiktatProcessorListenerWrapper
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
        fun DiktatProcessorListener.skipKnownErrors(baseline: DiktatBaseline): DiktatProcessorListener = object : DiktatProcessorListenerWrapper<DiktatProcessorListener>(
            this@skipKnownErrors
        ) {
            override fun doOnError(
                wrappedValue: DiktatProcessorListener,
                file: Path,
                error: DiktatError,
                isCorrected: Boolean
            ) {
                if (!baseline.errorsByFile(file).contains(error)) {
                    wrappedValue.onError(file, error, isCorrected)
                }
            }

            override fun doBeforeAll(wrappedValue: DiktatProcessorListener, files: Collection<Path>) = wrappedValue.beforeAll(files)
            override fun doBefore(wrappedValue: DiktatProcessorListener, file: Path) = wrappedValue.before(file)
            override fun doAfter(wrappedValue: DiktatProcessorListener, file: Path) = wrappedValue.after(file)
            override fun doAfterAll(wrappedValue: DiktatProcessorListener) = wrappedValue.afterAll()
        }
    }
}
