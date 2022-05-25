/**
 * Utility classes and methods for tests
 */

package org.cqfn.diktat.util

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import com.pinterest.ktlint.core.VisitorProvider
import com.pinterest.ktlint.core.api.FeatureInAlphaState
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.slf4j.LoggerFactory

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

internal const val TEST_FILE_NAME = "TestFileName.kt"

private val log = LoggerFactory.getLogger("TestUtils")

@Suppress("TYPE_ALIAS")
internal val defaultCallback: (lintError: LintError, corrected: Boolean) -> Unit = { lintError, _ ->
    log.warn("Received linting error: $lintError")
}

typealias LintErrorCallback = (LintError, Boolean) -> Unit

/**
 * Compare [LintError]s from [this] with [expectedLintErrors]
 *
 * @param expectedLintErrors expected [LintError]s
 */
internal fun List<LintError>.assertEquals(vararg expectedLintErrors: LintError) {
    if (size == expectedLintErrors.size) {
        Assertions.assertThat(this)
            .allSatisfy(Consumer { actual ->
                val expected = expectedLintErrors[this@assertEquals.indexOf(actual)]
                SoftAssertions.assertSoftly { sa ->
                    sa
                        .assertThat(actual.line)
                        .`as`("Line")
                        .isEqualTo(expected.line)
                    sa
                        .assertThat(actual.col)
                        .`as`("Column")
                        .isEqualTo(expected.col)
                    sa
                        .assertThat(actual.ruleId)
                        .`as`("Rule id")
                        .isEqualTo(expected.ruleId)
                    sa
                        .assertThat(actual.detail)
                        .`as`("Detailed message")
                        .isEqualTo(expected.detail)
                    sa
                        .assertThat(actual.canBeAutoCorrected)
                        .`as`("Can be autocorrected")
                        .isEqualTo(expected.canBeAutoCorrected)
                }
            })
    } else {
        Assertions.assertThat(this).containsExactly(*expectedLintErrors)
    }
}

/**
 * @param ruleSetProviderRef
 * @param text
 * @param fileName
 * @param rulesConfigList
 * @param cb callback to be called on unhandled [LintError]s
 * @return formatted code
 */
@OptIn(FeatureInAlphaState::class)
@Suppress("LAMBDA_IS_NOT_LAST_PARAMETER")
internal fun format(ruleSetProviderRef: (rulesConfigList: List<RulesConfig>?) -> RuleSetProvider,
                    text: String,
                    fileName: String,
                    rulesConfigList: List<RulesConfig>? = null,
                    cb: LintErrorCallback = defaultCallback
): String {
    val ruleSets = listOf(ruleSetProviderRef.invoke(rulesConfigList).get())
    return KtLint.format(
        KtLint.ExperimentalParams(
            text = text,
            ruleSets = ruleSets,
            fileName = fileName.removeSuffix("_copy"),
            script = fileName.removeSuffix("_copy").endsWith("kts"),
            cb = cb,
            userData = mapOf("file_path" to fileName.removeSuffix("_copy"))
        ),
        ruleSets = ruleSets,
        VisitorProvider(
            ruleSets = ruleSets,
            debug = true,
            // setting this to `true` breaks smoke test
            isUnitTestContext = false,
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
    Assertions
        .assertThat(counter.get())
        .`as`("Number of expected asserts")
        .isEqualTo(expectedAsserts)
}
