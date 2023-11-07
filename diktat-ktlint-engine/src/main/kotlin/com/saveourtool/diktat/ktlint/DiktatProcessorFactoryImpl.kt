package com.saveourtool.diktat.ktlint

import com.saveourtool.diktat.DiktatProcessor
import com.saveourtool.diktat.DiktatProcessorFactory
import com.saveourtool.diktat.api.DiktatCallback
import com.saveourtool.diktat.api.DiktatRuleSet
import com.saveourtool.diktat.ktlint.KtLintRuleWrapper.Companion.toKtLint
import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.api.LintError
import java.nio.file.Path

private typealias FormatCallback = (LintError, Boolean) -> Unit
private typealias LintCallback = (LintError) -> Unit

/**
 * A factory to create [DiktatProcessor] using [DiktatProcessorFactory] and `KtLint` as engine
 */
class DiktatProcessorFactoryImpl : DiktatProcessorFactory {
    override fun invoke(diktatRuleSet: DiktatRuleSet): DiktatProcessor {
        val ktLintRuleEngine = diktatRuleSet.toKtLintEngine()
        return object : DiktatProcessor {
            override fun fix(
                file: Path,
                callback: DiktatCallback,
            ): String {
                val code = file.toKtLint()
                val fixedCode = ktLintRuleEngine.format(code, DiktatCallback.empty.toKtLintForFormat())
                val ktlintFixedCode = Code(
                    content = fixedCode,
                    fileName = code.fileName,
                    filePath = code.filePath,
                    script = code.script,
                    isStdIn = code.isStdIn,
                )
                ktLintRuleEngine.lint(ktlintFixedCode, callback.toKtLintForLint())
                return fixedCode
            }
            override fun fix(
                code: String,
                isScript: Boolean,
                callback: DiktatCallback
            ): String {
                val fixedCode = ktLintRuleEngine.format(code.toKtLint(isScript), DiktatCallback.empty.toKtLintForFormat())
                ktLintRuleEngine.lint(fixedCode.toKtLint(isScript), callback.toKtLintForLint())
                return fixedCode
            }
            override fun check(
                file: Path,
                callback: DiktatCallback,
            ) = ktLintRuleEngine.lint(file.toKtLint(), callback.toKtLintForLint())
            override fun check(
                code: String,
                isScript: Boolean,
                callback: DiktatCallback
            ) = ktLintRuleEngine.lint(code.toKtLint(isScript), callback.toKtLintForLint())
        }
    }

    companion object {
        private fun DiktatRuleSet.toKtLintEngine(): KtLintRuleEngine = KtLintRuleEngine(ruleProviders = toKtLint())

        private fun Path.toKtLint(): Code = Code.fromFile(this.toFile())

        private fun String.toKtLint(isScript: Boolean): Code = Code.fromSnippet(this, isScript)

        private fun DiktatCallback.toKtLintForFormat(): FormatCallback = { error, isCorrected ->
            this(error.wrap(), isCorrected)
        }

        private fun DiktatCallback.toKtLintForLint(): LintCallback = { error ->
            this(error.wrap(), false)
        }
    }
}
