/**
 * This file contains util methods for __KtLint__
 */

package org.cqfn.diktat.ktlint

import org.cqfn.diktat.api.DiktatCallback
import org.cqfn.diktat.api.DiktatRuleSet
import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.pinterest.ktlint.core.LintError
import org.intellij.lang.annotations.Language
import java.nio.file.Path
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.relativeTo

/**
 * Makes sure this _rule id_ is qualified with a _rule set id_.
 *
 * @param ruleSetId the _rule set id_; defaults to [DIKTAT_RULE_SET_ID].
 * @return the fully-qualified _rule id_ in the form of `ruleSetId:ruleId`.
 * @see DIKTAT_RULE_SET_ID
 * @since 1.2.4
 */
fun String.qualifiedWithRuleSetId(ruleSetId: String = DIKTAT_RULE_SET_ID): String =
    when {
        this.contains(':') -> this
        else -> "$ruleSetId:$this"
    }

/**
 * @param sourceRootDir
 * @return relative path to [sourceRootDir] as [String]
 */
fun Path.relativePathStringTo(sourceRootDir: Path): String = relativeTo(sourceRootDir).invariantSeparatorsPathString

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

/**
 * @param ruleSetSupplier
 * @param text
 * @param fileName
 * @param cb callback to be called on unhandled [LintError]s
 * @return formatted code
 */
@Suppress("LAMBDA_IS_NOT_LAST_PARAMETER")
fun format(
    ruleSetSupplier: () -> DiktatRuleSet,
    @Language("kotlin") text: String,
    fileName: String,
    cb: DiktatCallback,
): String = DiktatProcessorFactoryImpl().invoke(ruleSetSupplier())
    .fix(
        code = text,
        isScript = fileName.removeSuffix("_copy").endsWith("kts"),
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
