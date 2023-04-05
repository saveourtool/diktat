package org.cqfn.diktat.ktlint

import org.cqfn.diktat.DiktatProcessor
import org.cqfn.diktat.DiktatProcessorFactory
import org.cqfn.diktat.api.DiktatCallback
import org.cqfn.diktat.api.DiktatRuleSet
import org.cqfn.diktat.ktlint.DiktatErrorImpl.Companion.wrap
import org.cqfn.diktat.ktlint.KtLintRuleWrapper.Companion.toKtLint
import com.pinterest.ktlint.core.Code
import com.pinterest.ktlint.core.KtLintRuleEngine
import com.pinterest.ktlint.core.LintError
import java.nio.file.Path

private typealias FormatCallback = (LintError, Boolean) -> Unit
private typealias LintCallback = (LintError) -> Unit

/**
 * A factory to create [DiktatProcessor] using [DiktatProcessorFactory] and `KtLint` as engine
 */
class DiktatProcessorFactoryImpl : DiktatProcessorFactory {
    override fun invoke(diktatRuleSet: DiktatRuleSet): DiktatProcessor = object : DiktatProcessor {
        override fun fix(file: Path, callback: DiktatCallback): String =
                ktlintEngine(diktatRuleSet).format(file.toKtLint(), callback.toKtLintForFormat())
        override fun fix(code: String, isScript: Boolean, callback: DiktatCallback): String =
                ktlintEngine(diktatRuleSet).format(code.toKtLint(isScript), callback.toKtLintForFormat())
        override fun check(file: Path, callback: DiktatCallback) =
                ktlintEngine(diktatRuleSet).lint(file.toKtLint(), callback.toKtLintForLint())
        override fun check(code: String, isScript: Boolean, callback: DiktatCallback) =
                ktlintEngine(diktatRuleSet).lint(code.toKtLint(isScript), callback.toKtLintForLint())
    }

    private fun ktlintEngine(
        diktatRuleSet: DiktatRuleSet,
    ): KtLintRuleEngine = KtLintRuleEngine(
        ruleProviders = diktatRuleSet.toKtLint()
    )

    companion object {
        private fun Path.toKtLint(): Code = Code.CodeFile(this.toFile())

        private fun String.toKtLint(isScript: Boolean): Code = Code.CodeSnippet(this, isScript)

        private fun DiktatCallback.toKtLintForFormat(): FormatCallback = { error, isCorrected ->
            this(error.wrap(), isCorrected)
        }

        private fun DiktatCallback.toKtLintForLint(): LintCallback = { error ->
            this(error.wrap(), false)
        }
    }
}
