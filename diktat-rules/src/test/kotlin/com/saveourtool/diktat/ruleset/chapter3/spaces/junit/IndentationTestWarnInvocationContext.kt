package com.saveourtool.diktat.ruleset.chapter3.spaces.junit

import com.saveourtool.diktat.ruleset.chapter3.spaces.ExpectedIndentationError
import com.saveourtool.diktat.ruleset.junit.ExpectedLintError
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.extension.Extension
import java.util.SortedMap

/**
 * The `TestTemplateInvocationContext` implementation for indentation tests
 * (warn mode).
 *
 * @property customConfig non-default configuration for the indentation rule.
 * @property actualCode the original file content (may well get modified as
 *   fixes are applied).
 */
@Suppress("BLANK_LINE_BETWEEN_PROPERTIES")
internal class IndentationTestWarnInvocationContext(
    override val customConfig: SortedMap<String, out Any>,
    @Language("kotlin") override val actualCode: String,
    private val expectedErrors: List<ExpectedIndentationError> = emptyList()
) : IndentationTestInvocationContext {
    override val mode: String = "warn"

    override val correctlyIndented: Boolean = expectedErrors.isEmpty()

    override val displayName: String = when {
        correctlyIndented -> "should raise no warnings if properly indented"
        else -> "should be reported if mis-indented"
    }

    override fun getAdditionalExtensions(): List<Extension> =
        listOf(IndentationTestWarnExtension(
            customConfig,
            actualCode,
            expectedErrors.map(ExpectedLintError::asLintError).toTypedArray()))
}
