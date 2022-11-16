package org.cqfn.diktat.api

import java.util.function.BiConsumer

/**
 * Callback for diktat process
 */
@FunctionalInterface
fun interface DiktatCallback : BiConsumer<DiktatError, Boolean> {
    /**
     * Performs this callback on the given [error] taking into account [isCorrected] flag.
     *
     * @param error the error found by diktat
     * @param isCorrected true if the error fixed by diktat
     */
    override fun accept(error: DiktatError, isCorrected: Boolean)
}
