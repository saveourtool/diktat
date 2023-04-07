/**
 * Utility classes and methods for tests
 */

package org.cqfn.diktat.util

import org.cqfn.diktat.api.DiktatErrorEmitter
import org.cqfn.diktat.api.DiktatRule
import org.cqfn.diktat.api.DiktatRuleSet
import org.cqfn.diktat.ktlint.lint

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

private val debuggerPromptPrefixes: Array<out String> = arrayOf(
    "Listening for transport dt_socket at address: ",
    "Listening for transport dt_shmem at address: ",
)

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
 * Calls the [block] callback giving it a sequence of all the lines in this file
 * and closes the reader once the processing is complete.
 *
 * If [filterDebuggerPrompt] is `true`, the JVM debugger prompt is filtered out
 * from the sequence of lines before it is consumed by [block].
 *
 * If [filterDebuggerPrompt] is `false`, this function behaves exactly as the
 * overloaded function from the standard library.
 *
 * @param filterDebuggerPrompt whether the JVM debugger prompt should be
 *   filtered out.
 * @param block the callback which consumes the lines produced by this [Reader].
 * @return the value returned by [block].
 */
@OptIn(ExperimentalContracts::class)
internal fun <T> Reader.useLines(
    filterDebuggerPrompt: Boolean,
    block: (Sequence<String>) -> T,
): T {
    contract {
        callsInPlace(block, EXACTLY_ONCE)
    }

    return when {
        filterDebuggerPrompt -> {
            /*
             * Transform the line consumer.
             */
            { lines ->
                lines.filterNot(String::isDebuggerPrompt).let(block)
            }
        }

        else -> block
    }.let(this::useLines)
}

private fun String.isDebuggerPrompt(printIfTrue: Boolean = true): Boolean {
    val isDebuggerPrompt = debuggerPromptPrefixes.any { prefix ->
        this.startsWith(prefix)
    }
    if (isDebuggerPrompt && printIfTrue) {
        /*
         * Print the prompt to the standard out,
         * so that the IDE can attach to the debugger.
         */
        @Suppress("DEBUG_PRINT")
        println(this)
    }
    return isDebuggerPrompt
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
    lint(
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
