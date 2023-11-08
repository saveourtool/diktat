@file:JvmName("IndentationRuleTestUtils")
@file:Suppress("HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE")

package com.saveourtool.diktat.ruleset.chapter3.spaces

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import com.saveourtool.diktat.ruleset.utils.NEWLINE
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.ALIGNED_PARAMETERS
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_AFTER_OPERATORS
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_BEFORE_DOT
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_FOR_EXPRESSION_BODIES
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_OF_PARAMETERS
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.INDENTATION_SIZE
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.NEWLINE_AT_END

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
