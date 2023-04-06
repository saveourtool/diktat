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
        callback = cb,
    )

/**
 * @param ruleSetSupplier
 * @param text
 * @param fileName
 * @param cb callback to be called on unhandled [LintError]s
 * @return formatted code
 */
@Suppress("LAMBDA_IS_NOT_LAST_PARAMETER")
fun lint(
    ruleSetSupplier: () -> DiktatRuleSet,
    @Language("kotlin") text: String,
    fileName: String = "test.kt",
    cb: DiktatCallback = DiktatCallback.empty
) = DiktatProcessorFactoryImpl().invoke(ruleSetSupplier())
    .check(
        code = text,
        isScript = fileName.removeSuffix("_copy").endsWith("kts"),
        callback = cb,
    )
