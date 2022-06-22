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
                |private fun createLayoutParams(): WindowManager.LayoutParams =
                |    WindowManager.LayoutParams().apply {
                |        type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
                |        token = composeView.applicationWindowToken
                |        width = WindowManager.LayoutParams.MATCH_PARENT
                |        height = WindowManager.LayoutParams.MATCH_PARENT
                |        format = PixelFormat.TRANSLUCENT
                |
                |        // TODO make composable configurable
                |
                |        // see https://stackoverflow.com/questions/43511326/android-making-activity-full-screen-with-status-bar-on-top-of-it
                |        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                |            windowInsetsController?.hide(WindowInsets.Type.statusBars())
                |        } else {
                |            @Suppress("DEPRECATION")
                |            systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE or
                |                View.SYSTEM_UI_FLAG_FULLSCREEN or
                |                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                |                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                |                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                |                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
                |        }
                |    }
                """.trimMargin(),

                """
                |val offsetDelta =
                |    if (shimmerAnimationType != ShimmerAnimationType.FADE) translateAnim.dp
                |    else 2000.dp
                """.trimMargin(),

                """
                |private fun lerp(start: Float, stop: Float, fraction: Float): Float =
                |    (1 - fraction) * start + fraction * stop
                """.trimMargin(),

                """
                |fun foo() =
                |    println()
                """.trimMargin(),
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
                |private fun createLayoutParams(): WindowManager.LayoutParams =
                |        WindowManager.LayoutParams().apply {
                |            type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL
                |            token = composeView.applicationWindowToken
                |            width = WindowManager.LayoutParams.MATCH_PARENT
                |            height = WindowManager.LayoutParams.MATCH_PARENT
                |            format = PixelFormat.TRANSLUCENT
                |
                |            // TODO make composable configurable
                |
                |            // see https://stackoverflow.com/questions/43511326/android-making-activity-full-screen-with-status-bar-on-top-of-it
                |            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                |                windowInsetsController?.hide(WindowInsets.Type.statusBars())
                |            } else {
                |                @Suppress("DEPRECATION")
                |                systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE or
                |                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                |                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                |                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                |                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                |                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
                |            }
                |        }
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

                """
                |fun foo() =
                |        println()
                """.trimMargin(),
            )

    /**
     * See [#1347](https://github.com/saveourtool/diktat/issues/1347).
     *
     * @see whitespaceInStringLiteralsContinuationIndent
     */
    @Suppress("CUSTOM_GETTERS_SETTERS")
    val whitespaceInStringLiteralsSingleIndent: Array<String>
        @Language("kotlin")
        get() =
            arrayOf(
                """
                |@Test
                |fun `test method name`() {
                |    @Language("kotlin")
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
                |fun f0() {
                |    @Language("kotlin")
                |    val code =
                |        ""${'"'}
                |            |@Suppress("diktat")
                |            |fun foo() {
                |            |    val a = 1
                |            |}
                |        ""${'"'}.trimMargin()
                |    lintMethod(code)
                |}
                """.trimMargin(),

                """
                |fun f1() {
                |    @Language("kotlin")
                |    val code =
                |        ""${'"'}
                |            |@Suppress("diktat")
                |            |fun foo() {
                |            |    val a = 1
                |            |}
                |        ""${'"'}.trimMargin("|")
                |    lintMethod(code)
                |}
                """.trimMargin(),

                """
                |fun f2() {
                |    @Language("kotlin")
                |    val code =
                |        ""${'"'}
                |            >@Suppress("diktat")
                |            >fun foo() {
                |            >    val a = 1
                |            >}
                |        ""${'"'} . trimMargin ( marginPrefix = ">" )
                |    lintMethod(code)
                |}
                """.trimMargin(),

                """
                |fun checkScript() {
                |    lintMethod(
                |        ""${'"'}
                |                    |val A = "aa"
                |        ""${'"'}.trimMargin(),
                |    )
                |}
                """.trimMargin(),
            )

    /**
     * See [#1347](https://github.com/saveourtool/diktat/issues/1347).
     *
     * @see whitespaceInStringLiteralsSingleIndent
     */
    @Suppress("CUSTOM_GETTERS_SETTERS")
    val whitespaceInStringLiteralsContinuationIndent: Array<String>
        @Language("kotlin")
        get() =
            arrayOf(
                """
                |@Test
                |fun `test method name`() {
                |    @Language("kotlin")
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
                |fun f0() {
                |    @Language("kotlin")
                |    val code =
                |            ""${'"'}
                |                |@Suppress("diktat")
                |                |fun foo() {
                |                |    val a = 1
                |                |}
                |            ""${'"'}.trimMargin()
                |    lintMethod(code)
                |}
                """.trimMargin(),

                """
                |fun f1() {
                |    @Language("kotlin")
                |    val code =
                |            ""${'"'}
                |                |@Suppress("diktat")
                |                |fun foo() {
                |                |    val a = 1
                |                |}
                |            ""${'"'}.trimMargin("|")
                |    lintMethod(code)
                |}
                """.trimMargin(),

                """
                |fun f2() {
                |    @Language("kotlin")
                |    val code =
                |            ""${'"'}
                |                >@Suppress("diktat")
                |                >fun foo() {
                |                >    val a = 1
                |                >}
                |            ""${'"'} . trimMargin ( marginPrefix = ">" )
                |    lintMethod(code)
                |}
                """.trimMargin(),

                """
                |fun checkScript() {
                |    lintMethod(
                |            ""${'"'}
                |                        |val A = "aa"
                |            ""${'"'}.trimMargin(),
                |    )
                |}
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
     * @return `true` if known-to-fail unit tests can be muted on the CI server.
     */
    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    fun testsCanBeMuted(): Boolean =
        getBooleanProperty("tests.can.be.muted")
}
