package org.cqfn.diktat.api

import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

/**
 * A listener for [org.cqfn.diktat.DiktatProcessor]
 */
interface DiktatProcessorListener {
    /**
     * Called once, before [org.cqfn.diktat.DiktatProcessor] starts process a bunch of files.
     */
    fun beforeAll(): Unit = Unit

    /**
     * Called before each file when [org.cqfn.diktat.DiktatProcessor] starts to process it.
     *
     * @param file
     */
    fun before(file: Path): Unit = Unit

    /**
     * Called on each error when [org.cqfn.diktat.DiktatProcessor] detects such one.
     *
     * @param file
     * @param error
     * @param isCorrected
     */
    fun onError(file: Path, error: DiktatError, isCorrected: Boolean)

    /**
     * Called after each file when [org.cqfn.diktat.DiktatProcessor] finished to process it.
     *
     * @param file
     */
    fun after(file: Path): Unit = Unit

    /**
     * Called once, after the processing of [org.cqfn.diktat.DiktatProcessor] finished.
     */
    fun afterAll(): Unit = Unit

    companion object {
        /**
         * An empty implementation of [DiktatProcessorListener]
         */
        open class Empty : DiktatProcessorListener {
            override fun onError(file: Path, error: DiktatError, isCorrected: Boolean) = Unit
        }

        /**
         * An instance of [DiktatProcessorListener.Empty]
         */
        val empty = Empty()

        /**
         * @param listeners
         * @return a single [DiktatProcessorListener] which uses all provided [listeners]
         */
        operator fun invoke(vararg listeners: DiktatProcessorListener): DiktatProcessorListener = object : DiktatProcessorListener {
            override fun beforeAll() = listeners.forEach(DiktatProcessorListener::beforeAll)
            override fun before(file: Path) = listeners.forEach { it.before(file) }
            override fun onError(file: Path, error: DiktatError, isCorrected: Boolean) = listeners.forEach { it.onError(file, error, isCorrected) }
            override fun after(file: Path) = listeners.forEach { it.after(file) }
            override fun afterAll() = listeners.forEach(DiktatProcessorListener::afterAll)
        }

        /**
         * An implementation of [DiktatProcessorListener] which counts [DiktatError]s
         */
        fun AtomicInteger.countErrorsAsProcessorListener(): DiktatProcessorListener = object : DiktatProcessorListener {
            override fun onError(file: Path, error: DiktatError, isCorrected: Boolean) {
                incrementAndGet()
            }
        }

        /**
         * An implementation of [DiktatProcessorListener] which closes [AutoCloseable] at the end
         */
        fun AutoCloseable.closeAfterAllAsProcessorListener(): DiktatProcessorListener = object : Empty() {
            override fun afterAll() {
                this@closeAfterAllAsProcessorListener.close()
            }
        }
    }
}
