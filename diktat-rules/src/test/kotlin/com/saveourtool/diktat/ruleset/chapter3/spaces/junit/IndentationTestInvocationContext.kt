package com.saveourtool.diktat.ruleset.chapter3.spaces.junit

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.extension.TestTemplateInvocationContext
import java.util.SortedMap

/**
 * The [TestTemplateInvocationContext] implementation for indentation tests.
 */
interface IndentationTestInvocationContext : TestTemplateInvocationContext {
    /**
     * Non-default configuration for the indentation rule.
     */
    val customConfig: SortedMap<String, out Any>

    /**
     * The original file content (may well get modified as fixes are applied).
     */
    @get:Language("kotlin")
    val actualCode: String

    /**
     * The mode of this invocation context, either "warn" or "fix".
     */
    val mode: String

    /**
     * Whether the code in this test is indented in accordance with the
     * effective configuration.
     */
    val correctlyIndented: Boolean

    /**
     * The detailed display name of this invocation context.
     */
    val displayName: String

    override fun getDisplayName(invocationIndex: Int): String {
        val parameterDescription = when {
            customConfig.isEmpty() -> invocationIndex
            else -> customConfig.asParameterList()
        }

        return "[$mode] $displayName [$parameterDescription]"
    }

    private companion object {
        private fun Map<*, *>.asParameterList(): String =
            asSequence().map { (key, value) ->
                "$key = $value"
            }.joinToString()
    }
}
