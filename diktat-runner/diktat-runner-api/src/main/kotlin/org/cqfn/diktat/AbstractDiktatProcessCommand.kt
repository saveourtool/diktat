package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatCallback
import org.cqfn.diktat.ruleset.utils.isKotlinScript
import java.nio.file.Path
import kotlin.io.path.readText

/**
 * An abstract implementation for command to run `diktat`
 *
 * @property processor
 * @property file
 * @property fileContent
 * @property isScript
 * @property callback
 */
abstract class AbstractDiktatProcessCommand(
    protected val processor: DiktatProcessor,
    protected val file: Path,
    protected val fileContent: String,
    protected val isScript: Boolean,
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
     *
     * @property processor
     * @property file
     * @property fileContent
     * @property isScript
     * @property callback
     */
    abstract class Builder<C : AbstractDiktatProcessCommand> {
        private var processor: DiktatProcessor? = null
        private var file: Path? = null
        private var fileContent: String? = null
        private var isScript: Boolean? = null
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
         * @param fileContent
         * @return updated builder
         */
        fun fileContent(fileContent: String) = apply { this.fileContent = fileContent }

        /**
         * @param isScript
         * @return updated builder
         */
        fun isScript(isScript: Boolean) = apply { this.isScript = isScript }

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
                fileContent = fileContent ?: resolvedFile.readText(Charsets.UTF_8),
                isScript = isScript ?: resolvedFile.isKotlinScript(),
                callback = requireNotNull(callback) {
                    "callback is required"
                },
            )
        }

        /**
         * @return [C] is built using values from builder
         *
         * @param processor
         * @param file
         * @param fileContent
         * @param isScript
         * @param callback
         */
        abstract fun doBuild(
            processor: DiktatProcessor,
            file: Path,
            fileContent: String,
            isScript: Boolean,
            callback: DiktatCallback,
        ): C
    }
}
