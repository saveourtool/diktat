package org.cqfn.diktat.ktlint

import org.cqfn.diktat.DiktatProcessor
import org.cqfn.diktat.DiktatProcessorFactory
import org.cqfn.diktat.api.DiktatCallback
import org.cqfn.diktat.api.DiktatRuleSet
import org.cqfn.diktat.ktlint.DiktatErrorImpl.Companion.wrap
import org.cqfn.diktat.ktlint.KtLintRuleSetWrapper.Companion.toKtLint
import org.cqfn.diktat.util.isKotlinScript

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.api.EditorConfigOverride

import java.nio.charset.StandardCharsets
import java.nio.file.Path

import kotlin.io.path.absolutePathString
import kotlin.io.path.readLines

private typealias KtLintCallback = (LintError, Boolean) -> Unit

/**
 * A factory to create [DiktatProcessor] using [DiktatProcessorFactory] and `KtLint` as engine
 */
class DiktatProcessorFactoryImpl : DiktatProcessorFactory {
    override fun invoke(diktatRuleSet: DiktatRuleSet): DiktatProcessor = object : DiktatProcessor {
        override fun fix(file: Path, callback: DiktatCallback): String = KtLint.format(file.toKtLintParams(diktatRuleSet, callback))
        override fun fix(
            code: String,
            isScript: Boolean,
            callback: DiktatCallback
        ): String = KtLint.format(code.toKtLintParams(isScript, diktatRuleSet, callback))
        override fun check(file: Path, callback: DiktatCallback) = KtLint.lint(file.toKtLintParams(diktatRuleSet, callback))
        override fun check(
            code: String,
            isScript: Boolean,
            callback: DiktatCallback
        ) = KtLint.lint(code.toKtLintParams(isScript, diktatRuleSet, callback))
    }

    companion object {
        private fun Path.toKtLintParams(
            diktatRuleSet: DiktatRuleSet,
            callback: DiktatCallback,
        ): KtLint.ExperimentalParams = ktLintParams(
            fileName = absolutePathString(),
            text = readLines(StandardCharsets.UTF_8).joinToString("\n"),
            isScript = isKotlinScript(),
            diktatRuleSet = diktatRuleSet,
            callback = callback,
        )

        private fun String.toKtLintParams(
            isScript: Boolean,
            diktatRuleSet: DiktatRuleSet,
            callback: DiktatCallback,
        ): KtLint.ExperimentalParams = ktLintParams(
            fileName = if (isScript) "test.kts" else "test.kt",
            text = this,
            isScript = isScript,
            diktatRuleSet = diktatRuleSet,
            callback = callback,
        )

        private fun ktLintParams(
            fileName: String,
            text: String,
            isScript: Boolean,
            diktatRuleSet: DiktatRuleSet,
            callback: DiktatCallback,
        ): KtLint.ExperimentalParams = KtLint.ExperimentalParams(
            fileName = fileName,
            text = text,
            ruleSets = setOf(diktatRuleSet.toKtLint()),
            userData = emptyMap(),
            cb = callback.toKtLint(),
            script = isScript,
            editorConfigPath = null,
            debug = false,  // we do not use it
            editorConfigOverride = EditorConfigOverride.emptyEditorConfigOverride,
            isInvokedFromCli = false
        )

        private fun DiktatCallback.toKtLint(): KtLintCallback = { error, isCorrected ->
            this(error.wrap(), isCorrected)
        }
    }
}
