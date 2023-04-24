package org.cqfn.diktat.ktlint

import org.cqfn.diktat.DiktatProcessor
import org.cqfn.diktat.DiktatProcessorFactory
import org.cqfn.diktat.api.DiktatCallback
import org.cqfn.diktat.api.DiktatRuleSet
import org.cqfn.diktat.ktlint.KtLintRuleWrapper.Companion.toKtLint
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
            ): String = ktLintRuleEngine.format(file.toKtLint(), callback.toKtLintForFormat())
            override fun fix(
                code: String,
                isScript: Boolean,
                callback: DiktatCallback
            ): String = ktLintRuleEngine.format(code.toKtLint(isScript), callback.toKtLintForFormat())
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
