package org.cqfn.diktat.ruleset.chapter3.spaces

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig

/**
 * Code shared by [IndentationRuleWarnTest] and [IndentationRuleFixTest].
 *
 * @see IndentationRuleWarnTest
 * @see IndentationRuleFixTest
 */
internal object IndentationRuleTestMixin {
    /**
     * Creates an `IndentationConfig` from zero or more
     * [config entries][configEntries]. Invoke without arguments to create a
     * default `IndentationConfig`.
     *
     * @param configEntries the configuration entries to create this instance from.
     * @see [IndentationConfig]
     */
    @Suppress("TestFunctionName", "FUNCTION_NAME_INCORRECT_CASE")
    fun IndentationConfig(vararg configEntries: Pair<String, Any>): IndentationConfig =
        IndentationConfig(mapOf(*configEntries).mapValues(Any::toString))

    /**
     * @param configEntries the optional values which override the state of this
     *   [IndentationConfig].
     * @return the content of this [IndentationConfig] as a map, with some
     *   configuration entries overridden via [configEntries].
     */
    @Suppress("STRING_TEMPLATE_QUOTES")
    fun IndentationConfig.withCustomParameters(vararg configEntries: Pair<String, Any>): Map<String, String> =
        mutableMapOf(
            "alignedParameters" to "$alignedParameters",
            "indentationSize" to "$indentationSize",
            "newlineAtEnd" to "$newlineAtEnd",
            "extendedIndentOfParameters" to "$extendedIndentOfParameters",
            "extendedIndentAfterOperators" to "$extendedIndentAfterOperators",
            "extendedIndentBeforeDot" to "$extendedIndentBeforeDot",
        ).apply {
            configEntries.forEach { (key, value) ->
                this[key] = value.toString()
            }
        }

    /**
     * Converts this map to a list containing a single [RulesConfig].
     *
     * @return the list containing a single [RulesConfig] entry.
     */
    fun Map<String, String>.asRulesConfigList(): List<RulesConfig> =
        listOf(
            RulesConfig(
                name = WRONG_INDENTATION.name,
                enabled = true,
                configuration = this
            )
        )

    /**
     * Allows to simultaneously enable or disable _all_ `extendedIndent*` flags.
     *
     * @param enabled whether the _continuation indent_ should be enabled or
     *   disabled.
     * @return an array of map entries.
     */
    fun extendedIndent(enabled: Boolean): Array<Pair<String, Any>> =
        arrayOf(
            "extendedIndentOfParameters" to enabled,
            "extendedIndentAfterOperators" to enabled,
            "extendedIndentBeforeDot" to enabled)

    /**
     * @return the concatenated content of this array (elements separated with
     *   blank lines).
     */
    private fun Array<String>.concatenated(): String =
        joinToString(separator = "\n\n")

    /**
     * @return a sequence which returns the elements of this array and,
     *   additionally, the result of concatenation of all the elements.
     */
    fun Array<String>.asSequenceWithConcatenation(): Sequence<String> =
        sequence {
            yieldAll(asSequence())

            if (size > 1) {
                yield(concatenated())
            }
        }

    /**
     * @return a brief description of this code fragment.
     */
    fun String.describe(): String {
        val lines = splitToSequence('\n')

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
     * Casts a nullable value to a non-`null` one, similarly to the `!!`
     * operator.
     *
     * @return a non-`null` value.
     */
    fun <T> T?.assertNotNull(): T {
        check(this != null) {
            "Expecting actual not to be null"
        }

        return this
    }
}
