package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatCallback
import org.cqfn.diktat.api.DiktatLogLevel
import org.cqfn.diktat.ktlint.unwrap
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.api.EditorConfigOverride
import java.nio.file.Path
import kotlin.io.path.absolutePathString

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
    processor: DiktatProcessor,
    file: Path,
    fileContent: String,
    isScript: Boolean,
    callback: DiktatCallback,
) : AbstractDiktatProcessCommand(
    processor,
    file,
    fileContent,
    isScript,
    callback,
) {
    /**
     * Run `diktat fix` using parameters from current command
     *
     * @return result of `diktat fix`
     */
    override fun fix(): String = KtLint.format(ktLintParams())

    /**
     * Run `diktat check` using parameters from current command
     */
    override fun check(): Unit = KtLint.lint(ktLintParams())

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
     */
    class Builder internal constructor() : AbstractDiktatProcessCommand.Builder<DiktatProcessCommand>() {
        override fun doBuild(
            processor: DiktatProcessor,
            file: Path,
            fileContent: String,
            isScript: Boolean,
            callback: DiktatCallback,
        ): DiktatProcessCommand = DiktatProcessCommand(
            processor = processor,
            file = file,
            fileContent = fileContent,
            isScript = isScript,
            callback = callback,
        )
    }

    companion object {
        /**
         * @return a builder for [DiktatProcessCommand]
         */
        fun builder(): Builder = Builder()
    }
}
