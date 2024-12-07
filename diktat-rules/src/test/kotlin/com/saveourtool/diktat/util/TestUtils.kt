/**
 * Utility classes and methods for tests
 */

package com.saveourtool.diktat.util

import com.saveourtool.diktat.api.DiktatErrorEmitter
import com.saveourtool.diktat.api.DiktatRule
import com.saveourtool.diktat.api.DiktatRuleSet
import com.saveourtool.diktat.ktlint.check

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

import java.io.Reader
import java.util.concurrent.atomic.AtomicInteger
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract

internal const val TEST_FILE_NAME = "TestFileName.kt"

/**
 * Casts a nullable value to a non-`null` one, similarly to the `!!`
 * operator.
 *
 * @param lazyFailureMessage the message to evaluate in case of a failure.
 * @return a non-`null` value.
 */
@OptIn(ExperimentalContracts::class)
internal fun <T : Any> T?.assertNotNull(lazyFailureMessage: () -> String = { "Expecting actual not to be null" }): T {
    contract {
        returns() implies (this@assertNotNull != null)
    }

    return this ?: fail(lazyFailureMessage())
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
@Suppress("TYPE_ALIAS")
internal fun applyToCode(@Language("kotlin") code: String,
                         expectedAsserts: Int,
                         applyToNode: (node: ASTNode, counter: AtomicInteger) -> Unit
) {
    val counter = AtomicInteger(0)
    check(
        ruleSetSupplier = {
            DiktatRuleSet(listOf(object : DiktatRule {
                override val id: String
                    get() = "astnode-utils-test"
                override fun invoke(node: ASTNode, autoCorrect: Boolean, emitter: DiktatErrorEmitter) {
                    applyToNode(node, counter)
                }
            }))
        },
        text = code,
    )
    assertThat(counter.get())
        .`as`("Number of expected asserts")
        .isEqualTo(expectedAsserts)
}
