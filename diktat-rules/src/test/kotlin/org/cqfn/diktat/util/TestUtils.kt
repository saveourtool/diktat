/**
 * Utility classes and methods for tests
 */

package org.cqfn.diktat.util

import org.cqfn.diktat.common.utils.loggerWithKtlintConfig
import org.cqfn.diktat.ruleset.constants.EmitType

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import com.pinterest.ktlint.core.api.FeatureInAlphaState
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

import java.util.concurrent.atomic.AtomicInteger

internal const val TEST_FILE_NAME = "TestFileName.kt"

@Suppress("EMPTY_BLOCK_STRUCTURE_ERROR")
private val log = KotlinLogging.loggerWithKtlintConfig {}

@Suppress("TYPE_ALIAS")
internal val defaultCallback: (lintError: LintError, corrected: Boolean) -> Unit = { lintError, _ ->
    log.warn("Received linting error: $lintError")
}

typealias LintErrorCallback = (LintError, Boolean) -> Unit

/**
 * Casts a nullable value to a non-`null` one, similarly to the `!!`
 * operator.
 *
 * @param lazyFailureMessage the message to evaluate in case of a failure.
 * @return a non-`null` value.
 */
internal fun <T> T?.assertNotNull(lazyFailureMessage: () -> String = { "Expecting actual not to be null" }): T =
    this ?: fail(lazyFailureMessage())

/**
 * @param ruleSetProviderRef
 * @param text
 * @param fileName
 * @param cb callback to be called on unhandled [LintError]s
 * @return formatted code
 */
@Suppress("LAMBDA_IS_NOT_LAST_PARAMETER")
internal fun format(
    ruleSetProviderRef: () -> RuleSetProvider,
    @Language("kotlin") text: String,
    fileName: String,
    cb: LintErrorCallback = defaultCallback
): String {
    val ruleSets = listOf(ruleSetProviderRef().get())
    return KtLint.format(
        KtLint.ExperimentalParams(
            text = text,
            ruleSets = ruleSets,
            fileName = fileName.removeSuffix("_copy"),
            script = fileName.removeSuffix("_copy").endsWith("kts"),
            cb = cb,
            debug = true,
        )
    )
}

/**
 * This utility function lets you run arbitrary code on every node of given [code].
 * It also provides you with counter which can be incremented inside [applyToNode] and then will be compared to [expectedAsserts].
 * This allows you to keep track of how many assertions have actually been run on your code during tests.
 *
 * @param code
 * @param expectedAsserts Number of expected times of assert invocation
 * @param applyToNode Function to be called on each AST node, should increment counter if assert is called
 */
@OptIn(FeatureInAlphaState::class)
@Suppress("TYPE_ALIAS")
internal fun applyToCode(code: String,
                         expectedAsserts: Int,
                         applyToNode: (node: ASTNode, counter: AtomicInteger) -> Unit
) {
    val counter = AtomicInteger(0)
    KtLint.lint(
        KtLint.ExperimentalParams(
            text = code,
            ruleSets = listOf(
                RuleSet("test", object : Rule("astnode-utils-test") {
                    override fun visit(node: ASTNode,
                                       autoCorrect: Boolean,
                                       emit: EmitType
                    ) {
                        applyToNode(node, counter)
                    }
                })
            ),
            cb = { _, _ -> }
        )
    )
    assertThat(counter.get())
        .`as`("Number of expected asserts")
        .isEqualTo(expectedAsserts)
}
