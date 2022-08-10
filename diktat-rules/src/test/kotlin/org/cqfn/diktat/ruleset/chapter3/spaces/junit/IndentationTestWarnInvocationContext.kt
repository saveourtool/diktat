package org.cqfn.diktat.ruleset.chapter3.spaces.junit

import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.chapter3.spaces.ExpectedIndentationError
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import org.cqfn.diktat.ruleset.rules.chapter3.files.IndentationRule.Companion.NAME_ID
import com.pinterest.ktlint.core.LintError
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
            expectedErrors.map(asLintError).toTypedArray()))

    private companion object {
        private val warnText: (Int) -> (Int) -> String = { expectedIndent ->
            { actualIndent ->
                "${WRONG_INDENTATION.warnText()} expected $expectedIndent but was $actualIndent"
            }
        }

        /**
         * Converts this instance to a [LintError].
         */
        private val asLintError: ExpectedIndentationError.() -> LintError = {
            LintError(
                line,
                column,
                "$DIKTAT_RULE_SET_ID:$NAME_ID",
                warnText(expectedIndent)(actualIndent),
                true)
        }
    }
}
