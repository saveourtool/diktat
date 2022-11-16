package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatCallback
import org.cqfn.diktat.api.DiktatLogLevel
import org.cqfn.diktat.ktlint.unwrap
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.api.EditorConfigDefaults
import com.pinterest.ktlint.core.api.EditorConfigOverride
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * Command to run `diktat`
 *
 * @property file
 * @property fileContent
 */
class DiktatProcessCommand private constructor(
    val file: Path,
    val fileContent: String,
    private val callback: DiktatCallback,
    private val isScript: Boolean,
    private val logLevel: DiktatLogLevel,
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
    fun check() {
        KtLint.lint(ktLintParams())
    }

    @Suppress("DEPRECATION")
    private fun ktLintParams(): KtLint.ExperimentalParams = KtLint.ExperimentalParams(
        fileName = file.absolutePathString(),
        text = fileContent,
        ruleSets = setOf(DiktatRuleSetProvider().get()),
        ruleProviders = emptySet(),
        userData = emptyMap(),
        cb = callback.unwrap(),
        script = isScript,
        editorConfigPath = null,
        debug = logLevel == DiktatLogLevel.DEBUG,
        editorConfigDefaults = EditorConfigDefaults.emptyEditorConfigDefaults,
        editorConfigOverride = EditorConfigOverride.emptyEditorConfigOverride,
        isInvokedFromCli = false
    )

    /**
     * Builder for [DiktatProcessCommand]
     *
     * @property file
     * @property fileContent
     * @property callback
     * @property isScript
     * @property logLevel
     */
    data class Builder(
        var file: Path? = null,
        var fileContent: String? = null,
        var callback: DiktatCallback? = null,
        var isScript: Boolean? = null,
        var logLevel: DiktatLogLevel = DiktatLogLevel.INFO,
    ) {
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
         * @param callback
         * @return updated builder
         */
        fun callback(callback: DiktatCallback) = apply { this.callback = callback }

        /**
         * @param isScript
         * @return updated builder
         */
        fun isScript(isScript: Boolean) = apply { this.isScript = isScript }

        /**
         * @param logLevel
         * @return updated builder
         */
        fun logLevel(logLevel: DiktatLogLevel) = apply { this.logLevel = logLevel }

        /**
         * @return built [DiktatProcessCommand]
         */
        fun build() = DiktatProcessCommand(
            requireNotNull(file) {
                "file is required"
            },
            requireNotNull(fileContent) {
                "fileContent is required"
            },
            requireNotNull(callback) {
                "callback is required"
            },
            requireNotNull(isScript) {
                "isScript is required"
            },
            logLevel,
        )
    }
}
