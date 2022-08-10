@file:Suppress("HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE")

package org.cqfn.diktat.ruleset.chapter3.spaces

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import org.cqfn.diktat.ruleset.utils.NEWLINE
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig.Companion.ALIGNED_PARAMETERS
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_AFTER_OPERATORS
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_BEFORE_DOT
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_FOR_EXPRESSION_BODIES
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_OF_PARAMETERS
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig.Companion.INDENTATION_SIZE
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig.Companion.NEWLINE_AT_END

/**
 * @param configEntries the optional values which override the state of this
 *   [IndentationConfig].
 * @return the content of this [IndentationConfig] as a map, with some
 *   configuration entries overridden via [configEntries].
 */
internal fun IndentationConfig.withCustomParameters(vararg configEntries: Pair<String, Any>): Map<String, String> =
    mutableMapOf(
        ALIGNED_PARAMETERS to alignedParameters.toString(),
        INDENTATION_SIZE to indentationSize.toString(),
        NEWLINE_AT_END to newlineAtEnd.toString(),
        EXTENDED_INDENT_OF_PARAMETERS to extendedIndentOfParameters.toString(),
        EXTENDED_INDENT_FOR_EXPRESSION_BODIES to extendedIndentForExpressionBodies.toString(),
        EXTENDED_INDENT_AFTER_OPERATORS to extendedIndentAfterOperators.toString(),
        EXTENDED_INDENT_BEFORE_DOT to extendedIndentBeforeDot.toString(),
    ).apply {
        configEntries.forEach { (key, value) ->
            this[key] = value.toString()
        }
    }

/**
 * @param configEntries the optional values which override the state of this
 *   [IndentationConfig].
 * @return the content of this [IndentationConfig] as a map, with some
 *   configuration entries overridden via [configEntries].
 */
internal fun IndentationConfig.withCustomParameters(configEntries: Map<String, Any>): Map<String, String> =
    withCustomParameters(*configEntries
        .asSequence()
        .map { (key, value) ->
            key to value
        }.toList()
        .toTypedArray())

/**
 * Converts this map to a list containing a single [RulesConfig].
 *
 * @return the list containing a single [RulesConfig] entry.
 */
internal fun Map<String, String>.asRulesConfigList(): List<RulesConfig> =
    listOf(
        RulesConfig(
            name = WRONG_INDENTATION.name,
            enabled = true,
            configuration = this
        )
    )

/**
 * @return a sequence which returns the elements of this array and,
 *   additionally, the result of concatenation of all the elements.
 */
internal fun Array<String>.asSequenceWithConcatenation(): Sequence<String> =
    sequence {
        yieldAll(asSequence())

        if (size > 1) {
            yield(concatenated())
        }
    }

/**
 * @return a brief description of this code fragment.
 */
internal fun String.describe(): String {
    val lines = splitToSequence(NEWLINE)

    var first: String? = null

    val count = lines.onEachIndexed { index, line ->
        if (index == 0) {
            first = line
        }
    }.count()

    return when (count) {
        1 -> "\"$this\""
        else -> "\"$first\u2026\" ($count line(s))"
    }
}

/**
 * @return the concatenated content of this array (elements separated with
 *   blank lines).
 */
private fun Array<String>.concatenated(): String =
    joinToString(separator = "$NEWLINE$NEWLINE")

/**
 * Creates an `IndentationConfig` from zero or more
 * [config entries][configEntries]. Invoke without arguments to create a
 * default `IndentationConfig`.
 *
 * @param configEntries the configuration entries to create this instance from.
 * @see [IndentationConfig]
 */
@Suppress("TestFunctionName", "FUNCTION_NAME_INCORRECT_CASE")
internal fun IndentationConfig(vararg configEntries: Pair<String, Any>): IndentationConfig =
    IndentationConfigFactory(*configEntries)

/**
 * Allows to simultaneously enable or disable _all_ `extendedIndent*` flags.
 *
 * @param enabled whether the _continuation indent_ should be enabled or
 *   disabled.
 * @return an array of map entries.
 */
internal fun extendedIndent(enabled: Boolean): Array<Pair<String, Any>> =
    arrayOf(
        EXTENDED_INDENT_OF_PARAMETERS to enabled,
        EXTENDED_INDENT_FOR_EXPRESSION_BODIES to enabled,
        EXTENDED_INDENT_AFTER_OPERATORS to enabled,
        EXTENDED_INDENT_BEFORE_DOT to enabled)
