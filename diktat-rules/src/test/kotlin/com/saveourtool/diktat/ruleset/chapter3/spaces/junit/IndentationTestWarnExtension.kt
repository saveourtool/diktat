package com.saveourtool.diktat.ruleset.chapter3.spaces.junit

import com.saveourtool.diktat.ruleset.chapter3.spaces.asRulesConfigList
import com.saveourtool.diktat.ruleset.chapter3.spaces.withCustomParameters
import com.saveourtool.diktat.ruleset.rules.chapter3.files.IndentationRule
import com.saveourtool.diktat.ruleset.utils.NEWLINE
import com.saveourtool.diktat.util.LintTestBase
import com.saveourtool.diktat.api.DiktatError
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.extension.ExtensionContext
import kotlin.math.max
import kotlin.math.min

/**
 * The `Extension` implementation for indentation test templates (warn mode).
 *
 * @property customConfig non-default configuration for the indentation rule.
 * @property actualCode the original file content (may well get modified as
 *   fixes are applied).
 */
@Suppress("TOO_MANY_BLANK_LINES")  // Readability
internal class IndentationTestWarnExtension(
    override val customConfig: Map<String, Any>,
    @Language("kotlin") override val actualCode: String,
    private val expectedErrors: Array<DiktatError>
) : LintTestBase(::IndentationRule), IndentationTestExtension {

    override fun beforeTestExecution(context: ExtensionContext) {
        val actualErrors = lintResult(
            actualCode,
            defaultConfig.withCustomParameters(customConfig).asRulesConfigList())

        val description = NEWLINE + actualCode.annotateWith(actualErrors) + NEWLINE

        when {
            expectedErrors.size == 1 && actualErrors.size == 1 -> {
                val actual = actualErrors[0]
                val expected = expectedErrors[0]

                assertThat(actual)
                    .describedAs(description)
                    .isEqualTo(expected)
                assertThat(actual.canBeAutoCorrected)
                    .describedAs("canBeAutoCorrected")
                    .isEqualTo(expected.canBeAutoCorrected)
            }

            else -> assertThat(actualErrors)
                .describedAs(description)
                .apply {
                    when {
                        expectedErrors.isEmpty() -> isEmpty()
                        else -> containsExactly(*expectedErrors)
                    }
                }
        }
    }

    private companion object {
        /**
         * Converts a lint error to the annotation text:
         *
         * ```
         * ^____^
         * ```
         *
         * allowing in-code annotations like
         *
         * ```
         * fun f() {
         *     1 +
         *                                  2
         *         ^________________________^
         * ```
         */
        @Suppress("CUSTOM_GETTERS_SETTERS")
        private val DiktatError.annotationText: String
            get() {
                @Suppress("WRONG_NEWLINES")  // False positives, see #1495.
                val columnNumbers = decimalNumber
                    .findAll(detail)
                    .map { match ->
                        match.groups[1]?.value
                    }.filterNotNull()
                    .map(String::toInt)
                    .toList()
                    .takeLast(2)

                return when (columnNumbers.size) {
                    2 -> sequence {
                        yield(NEWLINE)

                        val columnNumber0 = columnNumbers[0]
                        val columnNumber1 = columnNumbers[1]
                        for (columnNumber in 1..max(columnNumber0, columnNumber1) + 1) {
                            val ch = when {
                                columnNumber <= min(columnNumber0, columnNumber1) -> ' '
                                columnNumber == columnNumber0 + 1 || columnNumber == columnNumber1 + 1 -> '^'
                                else -> '_'
                            }
                            yield(ch)
                        }
                    }.joinToString(separator = "")
                    else -> ""
                }
            }

        private val decimalNumber = Regex("""\b[+-]?(\d++)\b""")

        private fun String.annotateWith(errors: List<DiktatError>): String =
            when {
                errors.isEmpty() -> this
                else -> {
                    val linesAndErrors = errors.asSequence().map { error -> error.line to error }.toMap()

                    lineSequence().mapIndexed { index, line ->
                        when (val error = linesAndErrors[index + 1]) {
                            null -> line
                            else -> "$line${error.annotationText}"
                        }
                    }.joinToString(separator = NEWLINE.toString())
                }
            }
    }
}
