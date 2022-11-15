/**
 * Utility classes and methods for tests
 */

@file:Suppress(
    "Deprecation"
)

package org.cqfn.diktat.util

import org.cqfn.diktat.ruleset.constants.EmitType

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

import java.util.concurrent.atomic.AtomicInteger

internal const val TEST_FILE_NAME = "TestFileName.kt"

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
 * This utility function lets you run arbitrary code on every node of given [code].
 * It also provides you with counter which can be incremented inside [applyToNode] and then will be compared to [expectedAsserts].
 * This allows you to keep track of how many assertions have actually been run on your code during tests.
 *
 * @param code
 * @param expectedAsserts Number of expected times of assert invocation
 * @param applyToNode Function to be called on each AST node, should increment counter if assert is called
 */
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
