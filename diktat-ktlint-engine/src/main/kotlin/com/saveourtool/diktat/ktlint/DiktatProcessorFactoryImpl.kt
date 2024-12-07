package com.saveourtool.diktat.ktlint

import com.saveourtool.diktat.DiktatProcessor
import com.saveourtool.diktat.DiktatProcessorFactory
import com.saveourtool.diktat.api.DiktatCallback
import com.saveourtool.diktat.api.DiktatRuleSet
import com.saveourtool.diktat.ktlint.KtLintRuleWrapper.Companion.toKtLint
import com.pinterest.ktlint.rule.engine.api.Code
import com.pinterest.ktlint.rule.engine.api.EditorConfigDefaults
import com.pinterest.ktlint.rule.engine.api.KtLintRuleEngine
import com.pinterest.ktlint.rule.engine.api.LintError
import org.ec4j.core.model.EditorConfig
import org.ec4j.core.model.Glob
import org.ec4j.core.model.Property
import org.ec4j.core.model.PropertyType
import java.nio.file.Path
import kotlin.io.path.name

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
            ): String = ktLintRuleEngine.formatSilentlyThenLint(file.toKtLint(), callback.toKtLintForLint())
            override fun fix(
                code: String,
                virtualPath: Path?,
                callback: DiktatCallback
            ): String = ktLintRuleEngine.formatSilentlyThenLint(code.toKtLint(virtualPath), callback.toKtLintForLint())
            override fun check(
                file: Path,
                callback: DiktatCallback,
            ) = ktLintRuleEngine.lint(file.toKtLint(), callback.toKtLintForLint())
            override fun check(
                code: String,
                virtualPath: Path?,
                callback: DiktatCallback
            ) = ktLintRuleEngine.lint(code.toKtLint(virtualPath), callback.toKtLintForLint())
        }
    }

    companion object {
        private fun DiktatRuleSet.toKtLintEngine(): KtLintRuleEngine = KtLintRuleEngine(
            ruleProviders = toKtLint(),
            // use platform dependent endlines in process of editing
            editorConfigDefaults = EditorConfigDefaults(
                EditorConfig.builder()
                    .openSection()
                    .glob(Glob("**"))
                    .property(
                        Property.builder()
                            .name(PropertyType.end_of_line.name)
                            .type(PropertyType.end_of_line)
                            .value(PropertyType.EndOfLineValue.ofEndOfLineString(System.lineSeparator())?.name)
                    )
                    .closeSection()
                    .build()
            )
        )

        private fun Path.toKtLint(): Code = Code.fromFile(this.toFile())

        private fun String.toKtLint(virtualPath: Path?): Code = Code(
            content = this,
            fileName = virtualPath?.name,
            filePath = virtualPath,
            script = virtualPath?.name?.endsWith(".kts", ignoreCase = true) ?: false,
            isStdIn = virtualPath == null,
        )

        private fun DiktatCallback.toKtLintForLint(): LintCallback = { error ->
            this(error.wrap(), false)
        }

        private fun KtLintRuleEngine.formatSilentlyThenLint(
            code: Code,
            callback: LintCallback,
        ): String {
            // this API method is significantly changed in Ktlint, so -werror was disabled due to it
            @Suppress("Deprecation")
            val formatResult = format(code)
            lint(
                code = Code(
                    content = formatResult,
                    fileName = code.fileName,
                    filePath = code.filePath,
                    script = code.script,
                    isStdIn = code.isStdIn,
                ),
                callback = callback,
            )
            return formatResult
        }
    }
}
