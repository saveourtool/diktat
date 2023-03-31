package org.cqfn.diktat.ktlint

import org.cqfn.diktat.DiktatProcessor
import org.cqfn.diktat.DiktatProcessorFactory
import org.cqfn.diktat.api.DiktatCallback
import org.cqfn.diktat.ktlint.KtLintRuleSetWrapper.Companion.toKtLint
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import org.cqfn.diktat.ruleset.utils.LintErrorCallback
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.api.EditorConfigOverride
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.readText

/**
 * A factory to create [DiktatProcessor] using [DiktatRuleSetProvider] using `KtLint`
 */
class DiktatProcessorFactoryImpl : DiktatProcessorFactory {
    override fun invoke(diktatRuleSetProvider: DiktatRuleSetProvider): DiktatProcessor = object : DiktatProcessor() {
        override fun fix(file: Path, callback: DiktatCallback): String = KtLint.format(ktLintParams(diktatRuleSetProvider, file, callback.unwrap()))
        override fun check(file: Path, callback: DiktatCallback) = KtLint.lint(ktLintParams(diktatRuleSetProvider, file, callback.unwrap()))
    }

    @Suppress("DEPRECATION")
    private fun ktLintParams(
        diktatRuleSetProvider: DiktatRuleSetProvider,
        file: Path,
        callback: LintErrorCallback,
    ): KtLint.ExperimentalParams = KtLint.ExperimentalParams(
        fileName = file.absolutePathString(),
        text = file.readText(StandardCharsets.UTF_8),
        ruleSets = setOf(diktatRuleSetProvider().toKtLint()),
        userData = emptyMap(),
        cb = callback,
        script = false, // internal API of KtLint
        editorConfigPath = null,
        debug = false, // we do not use it
        editorConfigOverride = EditorConfigOverride.emptyEditorConfigOverride,
        isInvokedFromCli = false
    )
}
