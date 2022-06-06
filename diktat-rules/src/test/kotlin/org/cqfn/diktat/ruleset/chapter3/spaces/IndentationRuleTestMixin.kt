package org.cqfn.diktat.ruleset.chapter3.spaces

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import org.cqfn.diktat.ruleset.utils.indentation.IndentationConfig
import org.intellij.lang.annotations.Language

import java.lang.Boolean.getBoolean as getBooleanProperty

/**
 * Code shared by [IndentationRuleWarnTest] and [IndentationRuleFixTest].
 *
 * @see IndentationRuleWarnTest
 * @see IndentationRuleFixTest
 */
internal interface IndentationRuleTestMixin {
    /**
     * See [#1330](https://github.com/saveourtool/diktat/issues/1330).
     *
     * @see expressionBodyFunctionsContinuationIndent
     */
    @Suppress("CUSTOM_GETTERS_SETTERS")
    val expressionBodyFunctionsSingleIndent: Array<String>
        @Language("kotlin")
        get() =
            arrayOf(
                """
                |@Test
                |fun `checking that suppression with ignore everything works`() {
                |    val code =
                |        ""${'"'}
                |            @Suppress("diktat")
                |            fun foo() {
                |                val a = 1
                |            }
                |        ""${'"'}.trimIndent()
                |    lintMethod(code)
                |}
                """.trimMargin(),

                """
                |val currentTime: Time
                |    get() =
                |        with(currentDateTime) {
                |            Time(hour = hour, minute = minute, second = second)
                |        }
                """.trimMargin(),

                """
                |fun formatDateByPattern(date: String, pattern: String = "ddMMyy"): String =
                |    DateTimeFormatter.ofPattern(pattern).format(LocalDate.parse(date))
                """.trimMargin(),

                """
                |private fun createLayoutParams(): WindowManager.LayoutParams =
                |    WindowManager.LayoutParams().apply { /* ... */ }
                """.trimMargin(),

                """
                |val offsetDelta =
                |    if (shimmerAnimationType != ShimmerAnimationType.FADE) translateAnim.dp
                |    else 2000.dp
                """.trimMargin(),

                """
                |private fun lerp(start: Float, stop: Float, fraction: Float): Float =
                |    (1 - fraction) * start + fraction * stop
                """.trimMargin()
            )

    /**
     * See [#1330](https://github.com/saveourtool/diktat/issues/1330).
     *
     * @see expressionBodyFunctionsSingleIndent
     */
    @Suppress("CUSTOM_GETTERS_SETTERS")
    val expressionBodyFunctionsContinuationIndent: Array<String>
        @Language("kotlin")
        get() =
            arrayOf(
                """
                |@Test
                |fun `checking that suppression with ignore everything works`() {
                |    val code =
                |            ""${'"'}
                |                @Suppress("diktat")
                |                fun foo() {
                |                    val a = 1
                |                }
                |            ""${'"'}.trimIndent()
                |    lintMethod(code)
                |}
                """.trimMargin(),

                """
                |val currentTime: Time
                |    get() =
                |            with(currentDateTime) {
                |                Time(hour = hour, minute = minute, second = second)
                |            }
                """.trimMargin(),

                """
                |fun formatDateByPattern(date: String, pattern: String = "ddMMyy"): String =
                |        DateTimeFormatter.ofPattern(pattern).format(LocalDate.parse(date))
                """.trimMargin(),

                """
                |private fun createLayoutParams(): WindowManager.LayoutParams =
                |        WindowManager.LayoutParams().apply { /* ... */ }
                """.trimMargin(),

                """
                |val offsetDelta =
                |        if (shimmerAnimationType != ShimmerAnimationType.FADE) translateAnim.dp
                |        else 2000.dp
                """.trimMargin(),

                """
                |private fun lerp(start: Float, stop: Float, fraction: Float): Float =
                |        (1 - fraction) * start + fraction * stop
                """.trimMargin(),
            )

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
     * @return the concatenated content of this array (elements separated with
     *   blank lines).
     */
    fun Array<String>.concatenated(): String =
        joinToString(separator = "\n\n")

    /**
     * @return `true` if known-to-fail unit tests can be muted on the CI server.
     */
    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    fun testsCanBeMuted(): Boolean =
        getBooleanProperty("tests.can.be.muted")
}
