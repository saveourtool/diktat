package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatCallback
import org.cqfn.diktat.ktlint.wrap
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.api.EditorConfigDefaults
import com.pinterest.ktlint.core.api.EditorConfigOverride
import java.io.File

/**
 * Command to run `diktat`
 */
class DiktatProcessCommand private constructor(
    val file: File,
    val fileContent: String,
    private val callback: DiktatCallback,
    private val isScript: Boolean,
) {
    /**
     * Run `diktat fix` using parameters from current command
     *
     * @return result of `diktat fix`
     */
    fun fix(): String {
        return KtLint.format(ktLintParams())
    }

    /**
     * Run `diktat check` using parameters from current command
     */
    fun check() {
        KtLint.lint(ktLintParams())
    }

    @Suppress("DEPRECATION")
    private fun ktLintParams(): KtLint.ExperimentalParams = KtLint.ExperimentalParams(
        fileName = file.absolutePath,
        text = fileContent,
        ruleSets = setOf(DiktatRuleSetProvider().get()),
        ruleProviders = emptySet(),
        userData = emptyMap(),
        cb = { e, corrected ->
            callback.accept(e.wrap(), corrected)
        },
        script = isScript,
        editorConfigPath = null,
        debug = false,
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
     */
    data class Builder(
        var file: File? = null,
        var fileContent: String? = null,
        var callback: DiktatCallback? = null,
        var isScript: Boolean? = null,
    ) {
        /**
         * @param file
         * @return updated builder
         */
        fun file(file: File) = apply { this.file = file }

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
         * @return built [DiktatProcessCommand]
         */
        fun build() = DiktatProcessCommand(
            requireNotNull(file),
            requireNotNull(fileContent),
            requireNotNull(callback),
            requireNotNull(isScript),
        )
    }
}
