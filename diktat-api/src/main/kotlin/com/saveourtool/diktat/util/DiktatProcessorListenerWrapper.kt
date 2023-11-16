package com.saveourtool.diktat.util

import com.saveourtool.diktat.api.DiktatError
import com.saveourtool.diktat.api.DiktatProcessorListener
import java.nio.file.Path

/**
 * A common wrapper for [DiktatProcessorListener]
 *
 * @property wrappedValue
 */
open class DiktatProcessorListenerWrapper<T : Any>(
    val wrappedValue: T,
) : DiktatProcessorListener {
    override fun beforeAll(files: Collection<Path>): Unit = doBeforeAll(wrappedValue, files)

    /**
     * Called once, before [com.saveourtool.diktat.DiktatProcessor] starts process a bunch of files.
     *
     * @param wrappedValue
     * @param files
     */
    protected open fun doBeforeAll(wrappedValue: T, files: Collection<Path>): Unit = Unit

    override fun before(file: Path): Unit = doBefore(wrappedValue, file)

    /**
     * Called before each file when [com.saveourtool.diktat.DiktatProcessor] starts to process it.
     *
     * @param wrappedValue
     * @param file
     */
    protected open fun doBefore(wrappedValue: T, file: Path): Unit = Unit

    override fun onError(
        file: Path,
        error: DiktatError,
        isCorrected: Boolean
    ): Unit = doOnError(wrappedValue, file, error, isCorrected)

    /**
     * Called on each error when [com.saveourtool.diktat.DiktatProcessor] detects such one.
     *
     * @param wrappedValue
     * @param file
     * @param error
     * @param isCorrected
     */
    protected open fun doOnError(
        wrappedValue: T,
        file: Path,
        error: DiktatError,
        isCorrected: Boolean
    ): Unit = Unit

    override fun after(file: Path): Unit = doAfter(wrappedValue, file)

    /**
     * Called after each file when [com.saveourtool.diktat.DiktatProcessor] finished to process it.
     *
     * @param wrappedValue
     * @param file
     */
    protected open fun doAfter(wrappedValue: T, file: Path): Unit = Unit

    override fun afterAll(): Unit = doAfterAll(wrappedValue)

    /**
     * Called once, after the processing of [com.saveourtool.diktat.DiktatProcessor] finished.
     *
     * @param wrappedValue
     */
    protected open fun doAfterAll(wrappedValue: T): Unit = Unit

    companion object {
        /**
         * @return wrapped value [T] if it's possible
         */
        inline fun <reified T : Any> DiktatProcessorListener.tryUnwrap(): T? = (this as? DiktatProcessorListenerWrapper<*>)
            ?.wrappedValue
            ?.let { it as? T }

        /**
         * @return wrapped value [T] or an error
         */
        inline fun <reified T : Any> DiktatProcessorListener.unwrap(): T = tryUnwrap<T>()
            ?: error("Unsupported wrapper of ${DiktatProcessorListener::class.java.simpleName} to ${T::class.simpleName}: ${this.javaClass.name}")
    }
}
