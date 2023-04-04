package org.cqfn.diktat.api

/**
 * Callback for diktat process
 */
@FunctionalInterface
fun interface DiktatCallback : Function2<DiktatError, Boolean, Unit> {
    /**
     * Performs this callback on the given [error] taking into account [isCorrected] flag.
     *
     * @param error the error found by diktat
     * @param isCorrected true if the error fixed by diktat
     */
    override fun invoke(error: DiktatError, isCorrected: Boolean)
}
