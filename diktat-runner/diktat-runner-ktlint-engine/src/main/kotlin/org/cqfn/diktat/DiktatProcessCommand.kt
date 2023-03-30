package org.cqfn.diktat

import org.cqfn.diktat.api.DiktatCallback
import org.cqfn.diktat.ktlint.KtLintRuleSetProviderV2Wrapper.Companion.toKtLint
import org.cqfn.diktat.ktlint.unwrapForFormat
import org.cqfn.diktat.ktlint.unwrapForLint

import com.pinterest.ktlint.core.Code
import com.pinterest.ktlint.core.KtLintRuleEngine

import java.nio.file.Path

/**
 * Command to run `diktat`
 *
 * @property processor
 * @property file
 * @property callback
 */
class DiktatProcessCommand private constructor(
    processor: DiktatProcessor,
    file: Path,
    callback: DiktatCallback,
) : AbstractDiktatProcessCommand(
    processor,
    file,
    callback,
) {
    private val ktLintRuleEngine: KtLintRuleEngine by lazy {
        KtLintRuleEngine(
            ruleProviders = processor.diktatRuleSetProvider.toKtLint().getRuleProviders(),
        )
    }
    private val code: Code by lazy {
        Code.CodeFile(
            file = file.toFile()
        )
    }

    /**
     * Run `diktat fix` using parameters from current command
     *
     * @return result of `diktat fix`
     */
    override fun fix(): String = ktLintRuleEngine.format(code, callback.unwrapForFormat())

    /**
     * Run `diktat check` using parameters from current command
     */
    override fun check(): Unit = ktLintRuleEngine.lint(code, callback.unwrapForLint())

    /**
     * Builder for [DiktatProcessCommand]
     */
    class Builder internal constructor() : AbstractDiktatProcessCommand.Builder<DiktatProcessCommand>() {
        override fun doBuild(
            processor: DiktatProcessor,
            file: Path,
            callback: DiktatCallback,
        ): DiktatProcessCommand = DiktatProcessCommand(
            processor = processor,
            file = file,
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
