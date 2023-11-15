package com.saveourtool.diktat.api

import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

/**
 * A listener for [com.saveourtool.diktat.DiktatProcessor]
 */
interface DiktatProcessorListener {
    /**
     * Called once, before [com.saveourtool.diktat.DiktatProcessor] starts process a bunch of files.
     *
     * @param files
     */
    fun beforeAll(files: Collection<Path>): Unit = Unit

    /**
     * Called before each file when [com.saveourtool.diktat.DiktatProcessor] starts to process it.
     *
     * @param file
     */
    fun before(file: Path): Unit = Unit

    /**
     * Called on each error when [com.saveourtool.diktat.DiktatProcessor] detects such one.
     *
     * @param file
     * @param error
     * @param isCorrected
     */
    fun onError(
        file: Path,
        error: DiktatError,
        isCorrected: Boolean
    ): Unit = Unit

    /**
     * Called after each file when [com.saveourtool.diktat.DiktatProcessor] finished to process it.
     *
     * @param file
     */
    fun after(file: Path): Unit = Unit

    /**
     * Called once, after the processing of [com.saveourtool.diktat.DiktatProcessor] finished.
     */
    fun afterAll(): Unit = Unit

    companion object {
        /**
         * An instance of [DiktatProcessorListener] that does nothing
         */
        @Suppress("EMPTY_BLOCK_STRUCTURE_ERROR")
        val empty = object : DiktatProcessorListener {}

        /**
         * @param listeners
         * @return a single [DiktatProcessorListener] which uses all provided [listeners]
         */
        fun union(listeners: Iterable<DiktatProcessorListener>): DiktatProcessorListener = object : DiktatProcessorListener {
            override fun beforeAll(files: Collection<Path>) = listeners.forEach { it.beforeAll(files) }
            override fun before(file: Path) = listeners.forEach { it.before(file) }
            override fun onError(
                file: Path,
                error: DiktatError,
                isCorrected: Boolean
            ) = listeners.forEach { it.onError(file, error, isCorrected) }
            override fun after(file: Path) = listeners.forEach { it.after(file) }
            override fun afterAll() = listeners.forEach(DiktatProcessorListener::afterAll)
        }

        /**
         * @param listeners
         * @return a single [DiktatProcessorListener] which uses all provided [listeners]
         */
        operator fun invoke(vararg listeners: DiktatProcessorListener): DiktatProcessorListener = union(listeners.asIterable())

        /**
         * @return An implementation of [DiktatProcessorListener] which counts [DiktatError]s
         */
        fun AtomicInteger.countErrorsAsProcessorListener(): DiktatProcessorListener = object : DiktatProcessorListener {
            override fun onError(
                file: Path,
                error: DiktatError,
                isCorrected: Boolean
            ) {
                incrementAndGet()
            }
        }
    }
}
