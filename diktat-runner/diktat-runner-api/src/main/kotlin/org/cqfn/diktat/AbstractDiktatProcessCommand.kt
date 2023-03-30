package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatCallback
import java.nio.file.Path

/**
 * An abstract implementation for command to run `diktat`
 *
 * @property processor
 * @property file
 * @property callback
 */
abstract class AbstractDiktatProcessCommand(
    protected val processor: DiktatProcessor,
    protected val file: Path,
    protected val callback: DiktatCallback,
) {
    /**
     * Run `diktat fix` using parameters from current command
     *
     * @return result of `diktat fix`
     */
    abstract fun fix(): String

    /**
     * Run `diktat check` using parameters from current command
     */
    abstract fun check()

    /**
     * Builder for [AbstractDiktatProcessCommand]
     */
    abstract class Builder<C : AbstractDiktatProcessCommand> {
        private var processor: DiktatProcessor? = null
        private var file: Path? = null
        private var callback: DiktatCallback? = null

        /**
         * @param processor
         * @return updated builder
         */
        fun processor(processor: DiktatProcessor) = apply { this.processor = processor }

        /**
         * @param file
         * @return updated builder
         */
        fun file(file: Path) = apply { this.file = file }

        /**
         * @param callback
         * @return updated builder
         */
        fun callback(callback: DiktatCallback) = apply { this.callback = callback }

        /**
         * @return built [C]
         */
        fun build(): C {
            val resolvedFile = requireNotNull(file) {
                "file is required"
            }
            return doBuild(
                processor = requireNotNull(processor) {
                    "processor is required"
                },
                file = resolvedFile,
                callback = requireNotNull(callback) {
                    "callback is required"
                },
            )
        }

        /**
         * @param processor
         * @param file
         * @param callback
         * @return [C] is built using values from builder
         */
        abstract fun doBuild(
            processor: DiktatProcessor,
            file: Path,
            callback: DiktatCallback,
        ): C
    }
}
