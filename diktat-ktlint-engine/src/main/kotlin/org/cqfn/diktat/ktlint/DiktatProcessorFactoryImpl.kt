package org.cqfn.diktat.ktlint

import org.cqfn.diktat.DiktatProcessor
import org.cqfn.diktat.DiktatProcessorFactory
import org.cqfn.diktat.api.DiktatCallback
import org.cqfn.diktat.api.DiktatRuleSet
import org.cqfn.diktat.ktlint.KtLintRuleSetWrapper.Companion.toKtLint
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.api.EditorConfigOverride
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.readText

/**
 * A factory to create [DiktatProcessor] using [DiktatProcessorFactory] and `KtLint` as engine
 */
class DiktatProcessorFactoryImpl : DiktatProcessorFactory {
    override fun invoke(diktatRuleSet: DiktatRuleSet): DiktatProcessor = object : DiktatProcessor {
        override fun fix(file: Path, callback: DiktatCallback): String = KtLint.format(ktLintParams(diktatRuleSet, file, callback.unwrap()))
        override fun check(file: Path, callback: DiktatCallback) = KtLint.lint(ktLintParams(diktatRuleSet, file, callback.unwrap()))
    }

    private fun ktLintParams(
        diktatRuleSet: DiktatRuleSet,
        file: Path,
        callback: LintErrorCallback,
    ): KtLint.ExperimentalParams = KtLint.ExperimentalParams(
        fileName = file.absolutePathString(),
        text = file.readText(StandardCharsets.UTF_8),
        ruleSets = setOf(diktatRuleSet.toKtLint()),
        userData = emptyMap(),
        cb = callback,
        script = false,  // internal API of KtLint
        editorConfigPath = null,
        debug = false,  // we do not use it
        editorConfigOverride = EditorConfigOverride.emptyEditorConfigOverride,
        isInvokedFromCli = false
    )
}
