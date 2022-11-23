package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatCallback
import org.cqfn.diktat.common.config.rules.DIKTAT_ANALYSIS_CONF
import org.cqfn.diktat.ktlint.unwrap
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProviderV2
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.api.EditorConfigDefaults
import com.pinterest.ktlint.core.api.EditorConfigOverride
import org.intellij.lang.annotations.Language
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.extension
import kotlin.io.path.readText

/**
 * Command to run `diktat`
 *
 * @property file
 */
class DiktatProcessCommand private constructor(
    val file: Path,
    private val config: String,
    private val callback: DiktatCallback,
) {
    private val isDebug: Boolean by lazy {
        LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME).isDebugEnabled
    }

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

    private fun ktLintParams(): KtLint.ExperimentalParams = KtLint.ExperimentalParams(
        fileName = file.absolutePathString(),
        text = file.readText(Charsets.UTF_8),
        ruleSets = emptySet(),
        ruleProviders = DiktatRuleSetProviderV2(config).getRuleProviders(),
        userData = emptyMap(),
        cb = callback.unwrap(),
        script = file.extension.endsWith("kts", ignoreCase = true),
        editorConfigPath = null,
        debug = isDebug,
        editorConfigDefaults = EditorConfigDefaults.emptyEditorConfigDefaults,
        editorConfigOverride = EditorConfigOverride.emptyEditorConfigOverride,
        isInvokedFromCli = false
    )

    /**
     * Builder for [DiktatProcessCommand]
     *
     * @property file
     * @property config
     * @property callback
     * @property fileContent
     * @property isScript
     * @property logLevel
     */
    data class Builder(
        var file: Path? = null,
        @Language("kotlin") var fileContent: String? = null,
        var config: String = DIKTAT_ANALYSIS_CONF,
        var callback: DiktatCallback? = null,
        var isScript: Boolean? = null,
        var logLevel: Level = Level.INFO,
    ) {
        /**
         * @param file
         * @return updated builder
         */
        fun file(file: Path) = apply { this.file = file }

        /**
         * @param config
         * @return updated builder
         */
        fun config(config: String) = apply { this.config = config }

        /**
         * @param callback
         * @return updated builder
         */
        fun callback(callback: DiktatCallback) = apply { this.callback = callback }

        /**
         * @return built [DiktatProcessCommand]
         */
        fun build() = DiktatProcessCommand(
            requireNotNull(file) {
                "file is required"
            },
            config,
            requireNotNull(callback) {
                "callback is required"
            },
        )
    }
}
