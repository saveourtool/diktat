package com.saveourtool.diktat.api

/**
 * The **file-specific** error emitter, initialized and used in [DiktatRule] implementations.
 *
 * Since the file is indirectly a part of the state of a `DiktatRule`, the same
 * `DiktatRule` instance should **never be re-used** to check more than a single
 * file, or confusing effects (incl. race conditions) will occur.
 *
 * @see DiktatRule
 */
fun interface DiktatErrorEmitter : Function3<Int, String, Boolean, Unit> {
    /**
     * @param offset
     * @param errorMessage
     * @param canBeAutoCorrected
     */
    override fun invoke(
        offset: Int,
        errorMessage: String,
        canBeAutoCorrected: Boolean
    )
}
