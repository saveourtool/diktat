package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatCallback
import org.cqfn.diktat.common.config.rules.DIKTAT_ANALYSIS_CONF
import org.cqfn.diktat.ktlint.unwrap
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetFactory
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProviderV2
import org.cqfn.diktat.ruleset.utils.isKotlinScript
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.api.EditorConfigDefaults
import com.pinterest.ktlint.core.api.EditorConfigOverride
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.readText

/**
 * Command to run `diktat`
 *
 * @property file
 */
class DiktatProcessCommand private constructor(
    val file: Path,
    private val diktatRuleSetFactory: DiktatRuleSetFactory,
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
        ruleProviders = DiktatRuleSetProviderV2(diktatRuleSetFactory).getRuleProviders(),
        userData = emptyMap(),
        cb = callback.unwrap(),
        script = file.isKotlinScript(),
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
     * @property diktatRuleSetFactory
     * @property config
     * @property callback
     */
    data class Builder(
        var file: Path? = null,
        var diktatRuleSetFactory: DiktatRuleSetFactory? = null,
        var config: String? = null,
        var callback: DiktatCallback? = null,
    ) {
        /**
         * @param file
         * @return updated builder
         */
        fun file(file: Path) = apply { this.file = file }

        /**
         * @param diktatRuleSetFactory
         * @return updated builder
         */
        fun diktatRuleSetFactory(diktatRuleSetFactory: DiktatRuleSetFactory) = require(config == null) {
            "diktatRuleSetFactory is set already via config"
        }.let {
            apply { this.diktatRuleSetFactory = diktatRuleSetFactory }
        }

        /**
         * @param config
         * @return updated builder
         */
        fun config(config: String) = require(diktatRuleSetFactory == null) {
            "diktatRuleSetFactory is already provided directly"
        }.let {
            apply { this.config = config }
        }

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
            diktatRuleSetFactory ?: DiktatRuleSetFactory(config ?: DIKTAT_ANALYSIS_CONF),
            requireNotNull(callback) {
                "callback is required"
            },
        )
    }
}
