package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatCallback
import org.cqfn.diktat.api.DiktatLogLevel
import org.cqfn.diktat.ktlint.unwrap
import org.cqfn.diktat.ruleset.utils.isKotlinScript
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.api.EditorConfigOverride
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.readText

/**
 * Command to run `diktat`
 *
 * @property processor
 * @property file
 * @property fileContent
 * @property isScript
 * @property callback
 */
class DiktatProcessCommand private constructor(
    private val processor: DiktatProcessor,
    private val file: Path,
    private val fileContent: String,
    private val isScript: Boolean,
    private val callback: DiktatCallback,
) {
    /**
     * Run `diktat fix` using parameters from current command
     *
     * @return result of `diktat fix`
     */
    fun fix(): String = KtLint.format(ktLintParams())

    /**
     * Run `diktat check` using parameters from current command
     */
    fun check(): Unit = KtLint.lint(ktLintParams())

    @Suppress("DEPRECATION")
    private fun ktLintParams(): KtLint.ExperimentalParams = KtLint.ExperimentalParams(
        fileName = file.absolutePathString(),
        text = fileContent,
        ruleSets = setOf(processor.diktatRuleSetProvider.get()),
        userData = emptyMap(),
        cb = callback.unwrap(),
        script = isScript,
        editorConfigPath = null,
        debug = processor.logLevel == DiktatLogLevel.DEBUG,
        editorConfigOverride = EditorConfigOverride.emptyEditorConfigOverride,
        isInvokedFromCli = false
    )

    /**
     * Builder for [DiktatProcessCommand]
     *
     * @property processor
     * @property file
     * @property fileContent
     * @property isScript
     * @property callback
     */
    class Builder internal constructor(
        private var processor: DiktatProcessor? = null,
        private var file: Path? = null,
        private var fileContent: String? = null,
        private var isScript: Boolean? = null,
        private var callback: DiktatCallback? = null,
    ) {
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
         * @return built [DiktatProcessCommand]
         */
        fun build(): DiktatProcessCommand {
            val resolvedFile = requireNotNull(file) {
                "file is required"
            }
            return DiktatProcessCommand(
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
    }

    companion object {
        /**
         * @return a builder for [DiktatProcessCommand]
         */
        fun builder(): Builder = Builder()
    }
}
