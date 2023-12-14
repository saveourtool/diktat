/**
 * This file contains util methods for __KtLint__
 */

package com.saveourtool.diktat.ktlint

import com.saveourtool.diktat.api.DiktatCallback
import com.saveourtool.diktat.api.DiktatError
import com.saveourtool.diktat.api.DiktatRuleSet
import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID

import com.pinterest.ktlint.cli.reporter.core.api.KtlintCliError
import com.pinterest.ktlint.cli.reporter.core.api.ReporterProviderV2
import com.pinterest.ktlint.cli.reporter.core.api.ReporterV2
import com.pinterest.ktlint.rule.engine.api.LintError
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.utils.addToStdlib.applyIf
import java.io.OutputStream
import java.io.PrintStream

import java.nio.file.Path

import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.relativeToOrSelf

private const val CANNOT_BE_AUTOCORRECTED_SUFFIX = " (cannot be auto-corrected)"

/**
 * Makes sure this _rule id_ is qualified with a _rule set id_.
 *
 * @param ruleSetId the _rule set id_; defaults to [DIKTAT_RULE_SET_ID].
 * @return the fully-qualified _rule id_ in the form of `ruleSetId:ruleId` as __KtLint__'s [RuleId].
 * @see DIKTAT_RULE_SET_ID
 * @since 1.2.4
 */
fun String.toRuleId(ruleSetId: String = DIKTAT_RULE_SET_ID): RuleId =
    when {
        this.contains(':') -> RuleId(this)
        else -> RuleId("$ruleSetId:$this")
    }

/**
 * @return [DiktatError] from KtLint's [LintError]
 */
fun LintError.wrap(): DiktatError = DiktatError(
    line = this@wrap.line,
    col = this@wrap.col,
    ruleId = this@wrap.ruleId.value,
    detail = this@wrap.detail.removeSuffix(CANNOT_BE_AUTOCORRECTED_SUFFIX),
    canBeAutoCorrected = this@wrap.canBeAutoCorrected,
)

/**
 * @return [DiktatError] from KtLint's [KtlintCliError]
 */
fun KtlintCliError.wrap(): DiktatError = DiktatError(
    line = this@wrap.line,
    col = this@wrap.col,
    ruleId = this@wrap.ruleId,
    detail = this@wrap.detail.removeSuffix(CANNOT_BE_AUTOCORRECTED_SUFFIX),
    canBeAutoCorrected = this@wrap.status == KtlintCliError.Status.LINT_CAN_BE_AUTOCORRECTED,
)

/**
 * @return KtLint [LintError] from [DiktatError] or exception
 */
fun DiktatError.toKtLintForCli(): KtlintCliError = KtlintCliError(
    line = this@toKtLintForCli.line,
    col = this@toKtLintForCli.col,
    ruleId = this@toKtLintForCli.ruleId,
    detail = this@toKtLintForCli.detail.correctErrorDetail(this@toKtLintForCli.canBeAutoCorrected),
    status = if (this@toKtLintForCli.canBeAutoCorrected) {
        KtlintCliError.Status.LINT_CAN_BE_AUTOCORRECTED
    } else {
        KtlintCliError.Status.LINT_CAN_NOT_BE_AUTOCORRECTED
    }
)

/**
 * @receiver [DiktatError.detail]
 * @param canBeAutoCorrected [DiktatError.canBeAutoCorrected]
 * @return input string with [CANNOT_BE_AUTOCORRECTED_SUFFIX] if it's required
 */
fun String.correctErrorDetail(canBeAutoCorrected: Boolean): String = if (canBeAutoCorrected) {
    this
} else {
    "$this$CANNOT_BE_AUTOCORRECTED_SUFFIX"
}

/**
 * @param sourceRootDir
 * @return relative path to [sourceRootDir] as [String]
 */
fun Path.relativePathStringTo(sourceRootDir: Path?): String = (sourceRootDir?.let { relativeToOrSelf(it) } ?: this).invariantSeparatorsPathString

/**
 * @param out [OutputStream] for [ReporterV2]
 * @param closeOutAfterAll close [OutputStream] in [ReporterV2.afterAll]
 * @param opt configuration for [ReporterV2]
 * @return created [ReporterV2] which closes [out] in [ReporterV2.afterAll] if it's required
 */
fun <R : ReporterV2> ReporterProviderV2<R>.get(
    out: OutputStream,
    closeOutAfterAll: Boolean,
    opt: Map<String, String>,
): ReporterV2 = get(out.printStream(), opt).applyIf(closeOutAfterAll) {
    closeAfterAll(out)
}

/**
 * Enables ignoring autocorrected errors when in "fix" mode (i.e. when
 * [com.pinterest.ktlint.core.KtLint.format] is invoked).
 *
 * Before version 0.47, _Ktlint_ only reported non-corrected errors in "fix"
 * mode.
 * Now, this has changed.
 *
 * @receiver the instance of _Ktlint_ parameters.
 * @return the instance [DiktatCallback] that ignores corrected errors.
 * @see com.pinterest.ktlint.core.KtLint.format
 * @since 1.2.4
 */
private fun DiktatCallback.ignoreCorrectedErrors(): DiktatCallback = DiktatCallback { error, isCorrected ->
    if (!isCorrected) {
        this@ignoreCorrectedErrors(error, false)
    }
}

private fun OutputStream.printStream(): PrintStream = (this as? PrintStream) ?: PrintStream(this)

private fun ReporterV2.closeAfterAll(outputStream: OutputStream): ReporterV2 = object : ReporterV2Wrapper(this@closeAfterAll) {
    override fun afterAll() {
        super.afterAll()
        outputStream.flush()
        outputStream.close()
    }
}

/**
 * @param ruleSetSupplier
 * @param file
 * @param cb callback to be called on unhandled [LintError]s
 * @return formatted code
 */
@Suppress("LAMBDA_IS_NOT_LAST_PARAMETER")
fun format(
    ruleSetSupplier: () -> DiktatRuleSet,
    file: Path,
    cb: DiktatCallback,
): String = DiktatProcessorFactoryImpl().invoke(ruleSetSupplier())
    .fix(
        file = file,
        callback = cb.ignoreCorrectedErrors(),
    )

/**
 * @param ruleSetSupplier
 * @param file
 * @param cb callback to be called on unhandled [LintError]s
 * @return formatted code
 */
@Suppress("LAMBDA_IS_NOT_LAST_PARAMETER")
fun lint(
    ruleSetSupplier: () -> DiktatRuleSet,
    file: Path,
    cb: DiktatCallback = DiktatCallback.empty
) = DiktatProcessorFactoryImpl().invoke(ruleSetSupplier())
    .check(
        file = file,
        callback = cb.ignoreCorrectedErrors(),
    )

/**
 * @param ruleSetSupplier
 * @param text
 * @param cb callback to be called on unhandled [LintError]s
 * @return formatted code
 */
@Suppress("LAMBDA_IS_NOT_LAST_PARAMETER")
fun lint(
    ruleSetSupplier: () -> DiktatRuleSet,
    @Language("kotlin") text: String,
    cb: DiktatCallback = DiktatCallback.empty
) = DiktatProcessorFactoryImpl().invoke(ruleSetSupplier())
    .check(
        code = text,
        isScript = false,
        callback = cb.ignoreCorrectedErrors(),
    )
