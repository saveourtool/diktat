package com.saveourtool.diktat.api

import com.saveourtool.diktat.util.DiktatProcessorListenerWrapper
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

private typealias DiktatProcessorListenerIterable = Iterable<DiktatProcessorListener>

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
        fun union(listeners: DiktatProcessorListenerIterable): DiktatProcessorListener = object : DiktatProcessorListenerWrapper<DiktatProcessorListenerIterable>(listeners) {
            override fun doBeforeAll(wrappedValue: DiktatProcessorListenerIterable, files: Collection<Path>) = wrappedValue.forEach { it.beforeAll(files) }
            override fun doBefore(wrappedValue: DiktatProcessorListenerIterable, file: Path) = wrappedValue.forEach { it.before(file) }
            override fun doOnError(
                wrappedValue: DiktatProcessorListenerIterable,
                file: Path,
                error: DiktatError,
                isCorrected: Boolean
            ) = wrappedValue.forEach { it.onError(file, error, isCorrected) }
            override fun doAfter(wrappedValue: DiktatProcessorListenerIterable, file: Path) = wrappedValue.forEach { it.after(file) }
            override fun doAfterAll(wrappedValue: DiktatProcessorListenerIterable) = wrappedValue.forEach(DiktatProcessorListener::afterAll)
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
                if (!isCorrected) {
                    incrementAndGet()
                }
            }
        }
    }
}
