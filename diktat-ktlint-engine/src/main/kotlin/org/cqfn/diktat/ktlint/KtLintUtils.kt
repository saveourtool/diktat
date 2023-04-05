/**
 * This file contains util methods for __KtLint__
 */

package org.cqfn.diktat.ktlint

import org.cqfn.diktat.api.DiktatCallback
import org.cqfn.diktat.api.DiktatRuleSet
import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ktlint.DiktatErrorImpl.Companion.unwrap
import org.cqfn.diktat.ktlint.DiktatErrorImpl.Companion.wrap
import org.cqfn.diktat.ktlint.KtLintRuleWrapper.Companion.toKtLint
import com.pinterest.ktlint.core.Code
import com.pinterest.ktlint.core.KtLintRuleEngine
import com.pinterest.ktlint.core.LintError
import mu.KotlinLogging
import org.intellij.lang.annotations.Language
import java.nio.file.Path
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.relativeTo

private val log = KotlinLogging.logger { }

val defaultCallback = DiktatCallback { error, _ ->
    log.warn { "Received linting error: $error" }
}

typealias LintErrorCallback = (LintError, Boolean) -> Unit

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
 * @return [DiktatCallback] from KtLint [LintErrorCallback]
 */
fun LintErrorCallback.wrap(): DiktatCallback = DiktatCallback { error, isCorrected ->
    this(error.unwrap(), isCorrected)
}

/**
 * @return KtLint [LintErrorCallback] from [DiktatCallback] or exception
 */
fun DiktatCallback.unwrap(): LintErrorCallback = { error, isCorrected ->
    this(error.wrap(), isCorrected)
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
    cb: DiktatCallback = defaultCallback
): String {
    val ktLintRuleEngine = KtLintRuleEngine(
        ruleProviders = ruleSetSupplier().toKtLint()
    )
    return ktLintRuleEngine.format(
        code = Code.CodeSnippet(
            content = text,
            script = fileName.removeSuffix("_copy").endsWith("kts"),
        )
    ) { lintError: LintError, isCorrected: Boolean ->
        if (!isCorrected) {
            cb(lintError.wrap(), false)
        }
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
fun lint(
    ruleSetSupplier: () -> DiktatRuleSet,
    @Language("kotlin") text: String,
    fileName: String = "test.ks",
    cb: DiktatCallback = DiktatCallback.empty
) {
    val ktLintRuleEngine = KtLintRuleEngine(
        ruleProviders = ruleSetSupplier().toKtLint()
    )
    return ktLintRuleEngine.lint(
        code = Code.CodeSnippet(
            content = text,
            script = fileName.removeSuffix("_copy").endsWith("kts"),
        )
    ) { lintError: LintError ->
        cb(lintError.wrap(), false)
    }
}
