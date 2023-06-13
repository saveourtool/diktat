@file:Suppress("FILE_IS_TOO_LONG")

package com.saveourtool.diktat.ruleset.chapter3.spaces

import com.saveourtool.diktat.ruleset.chapter3.spaces.junit.IndentationTest
import com.saveourtool.diktat.ruleset.chapter3.spaces.junit.IndentedSourceCode
import com.saveourtool.diktat.ruleset.junit.BooleanOrDefault.FALSE
import com.saveourtool.diktat.ruleset.junit.BooleanOrDefault.TRUE
import com.saveourtool.diktat.ruleset.junit.NaturalDisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestMethodOrder

/**
 * For legacy indentation tests, see [IndentationRuleWarnTest] and
 * [IndentationRuleFixTest].
 *
 * @see IndentationRuleWarnTest
 * @see IndentationRuleFixTest
 */
@Suppress(
    "LargeClass",
    "MaxLineLength",
    "LONG_LINE",
)
@TestMethodOrder(NaturalDisplayName::class)
class IndentationRuleTest {
    /**
     * See [#1330](https://github.com/saveourtool/diktat/issues/1330).
     */
    @Nested
    @TestMethodOrder(NaturalDisplayName::class)
    inner class `Expression body functions` {
        @IndentationTest(
            first = IndentedSourceCode(
                """
                @Test
                fun `checking that suppression with ignore everything works`() {
                    val code =
                        ""${'"'} // diktat:WRONG_INDENTATION[expectedIndent = 12]
                            @Suppress("diktat")
                            fun foo() {
                                val a = 1
                            }
                        ""${'"'}.trimIndent()
                    lintMethod(code)
                }
                """,
                extendedIndentForExpressionBodies = FALSE),
            second = IndentedSourceCode(
                """
                @Test
                fun `checking that suppression with ignore everything works`() {
                    val code =
                            ""${'"'} // diktat:WRONG_INDENTATION[expectedIndent = 8]
                                @Suppress("diktat")
                                fun foo() {
                                    val a = 1
                                }
                            ""${'"'}.trimIndent()
                    lintMethod(code)
                }
                """,
                extendedIndentForExpressionBodies = TRUE))
        fun `case 1`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                val currentTime: Time
                    get() =
                        with(currentDateTime) {                                 // diktat:WRONG_INDENTATION[expectedIndent = 12]
                            Time(hour = hour, minute = minute, second = second) // diktat:WRONG_INDENTATION[expectedIndent = 16]
                        }                                                       // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentForExpressionBodies = FALSE),
            second = IndentedSourceCode(
                """
                val currentTime: Time
                    get() =
                            with(currentDateTime) {                                 // diktat:WRONG_INDENTATION[expectedIndent = 8]
                                Time(hour = hour, minute = minute, second = second) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                            }                                                       // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentForExpressionBodies = TRUE))
        fun `case 2`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun formatDateByPattern(date: String, pattern: String = "ddMMyy"): String =
                    DateTimeFormatter.ofPattern(pattern).format(LocalDate.parse(date)) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentForExpressionBodies = FALSE),
            second = IndentedSourceCode(
                """
                fun formatDateByPattern(date: String, pattern: String = "ddMMyy"): String =
                        DateTimeFormatter.ofPattern(pattern).format(LocalDate.parse(date)) // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentForExpressionBodies = TRUE))
        fun `case 3`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                private fun createLayoutParams(): WindowManager.LayoutParams =
                    WindowManager.LayoutParams().apply { /* ... */ } // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentForExpressionBodies = FALSE),
            second = IndentedSourceCode(
                """
                private fun createLayoutParams(): WindowManager.LayoutParams =
                        WindowManager.LayoutParams().apply { /* ... */ } // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentForExpressionBodies = TRUE))
        fun `case 4`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                private fun createLayoutParams(): WindowManager.LayoutParams =
                    WindowManager.LayoutParams().apply {                                                                                     // diktat:WRONG_INDENTATION[expectedIndent = 8]
                        type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL                                                             // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        token = composeView.applicationWindowToken                                                                           // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        width = WindowManager.LayoutParams.MATCH_PARENT                                                                      // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        height = WindowManager.LayoutParams.MATCH_PARENT                                                                     // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        format = PixelFormat.TRANSLUCENT                                                                                     // diktat:WRONG_INDENTATION[expectedIndent = 12]

                        // TODO make composable configurable                                                                                 // diktat:WRONG_INDENTATION[expectedIndent = 12]

                        // see https://stackoverflow.com/questions/43511326/android-making-activity-full-screen-with-status-bar-on-top-of-it // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {                                                                // diktat:WRONG_INDENTATION[expectedIndent = 12]
                            windowInsetsController?.hide(WindowInsets.Type.statusBars())                                                     // diktat:WRONG_INDENTATION[expectedIndent = 16]
                        } else {                                                                                                             // diktat:WRONG_INDENTATION[expectedIndent = 12]
                            @Suppress("DEPRECATION")                                                                                         // diktat:WRONG_INDENTATION[expectedIndent = 16]
                            systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE or                                                         // diktat:WRONG_INDENTATION[expectedIndent = 16]
                                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
                        }                                                                                                                    // diktat:WRONG_INDENTATION[expectedIndent = 12]
                    }                                                                                                                        // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentForExpressionBodies = FALSE),
            second = IndentedSourceCode(
                """
                private fun createLayoutParams(): WindowManager.LayoutParams =
                        WindowManager.LayoutParams().apply {                                                                                     // diktat:WRONG_INDENTATION[expectedIndent = 4]
                            type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL                                                             // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            token = composeView.applicationWindowToken                                                                           // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            width = WindowManager.LayoutParams.MATCH_PARENT                                                                      // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            height = WindowManager.LayoutParams.MATCH_PARENT                                                                     // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            format = PixelFormat.TRANSLUCENT                                                                                     // diktat:WRONG_INDENTATION[expectedIndent = 8]

                            // TODO make composable configurable                                                                                 // diktat:WRONG_INDENTATION[expectedIndent = 8]

                            // see https://stackoverflow.com/questions/43511326/android-making-activity-full-screen-with-status-bar-on-top-of-it // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {                                                                // diktat:WRONG_INDENTATION[expectedIndent = 8]
                                windowInsetsController?.hide(WindowInsets.Type.statusBars())                                                     // diktat:WRONG_INDENTATION[expectedIndent = 12]
                            } else {                                                                                                             // diktat:WRONG_INDENTATION[expectedIndent = 8]
                                @Suppress("DEPRECATION")                                                                                         // diktat:WRONG_INDENTATION[expectedIndent = 12]
                                systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE or                                                         // diktat:WRONG_INDENTATION[expectedIndent = 12]
                                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
                            }                                                                                                                    // diktat:WRONG_INDENTATION[expectedIndent = 8]
                        }                                                                                                                        // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentForExpressionBodies = TRUE))
        fun `case 5`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                val offsetDelta =
                    if (shimmerAnimationType != ShimmerAnimationType.FADE) translateAnim.dp // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    else 2000.dp                                                            // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentForExpressionBodies = FALSE),
            second = IndentedSourceCode(
                """
                val offsetDelta =
                        if (shimmerAnimationType != ShimmerAnimationType.FADE) translateAnim.dp // diktat:WRONG_INDENTATION[expectedIndent = 4]
                        else 2000.dp                                                            // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentForExpressionBodies = TRUE))
        fun `case 6`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                private fun lerp(start: Float, stop: Float, fraction: Float): Float =
                    (1 - fraction) * start + fraction * stop // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentForExpressionBodies = FALSE),
            second = IndentedSourceCode(
                """
                private fun lerp(start: Float, stop: Float, fraction: Float): Float =
                        (1 - fraction) * start + fraction * stop // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentForExpressionBodies = TRUE))
        fun `case 7`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun foo() =
                    println() // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentForExpressionBodies = FALSE),
            second = IndentedSourceCode(
                """
                fun foo() =
                        println() // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentForExpressionBodies = TRUE))
        fun `case 8`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f() =
                    x + (y +     // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            g(x)
                    )            // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentForExpressionBodies = FALSE),
            second = IndentedSourceCode(
                """
                fun f() =
                        x + (y +     // diktat:WRONG_INDENTATION[expectedIndent = 4]
                                g(x)
                        )            // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentForExpressionBodies = TRUE))
        fun `case 9`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f() =
                    (1 +       // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            2)
                """,
                extendedIndentForExpressionBodies = FALSE),
            second = IndentedSourceCode(
                """
                fun f() =
                        (1 +       // diktat:WRONG_INDENTATION[expectedIndent = 4]
                                2)
                """,
                extendedIndentForExpressionBodies = TRUE))
        fun `case 10`() = Unit
    }

    @Nested
    @TestMethodOrder(NaturalDisplayName::class)
    inner class `String templates` {
        /**
         * No message like
         *
         * > only spaces are allowed for indentation and each indentation should
         * > equal to 4 spaces (tabs are not allowed): the same number of
         * > indents to the opening and closing quotes was expected
         *
         * should be reported.
         *
         * See [#1490](https://github.com/saveourtool/diktat/issues/1490).
         */
        @IndentationTest(IndentedSourceCode(
            """
            val value = f(
                "text ${'$'}variable text".isEmpty()
            )
            """),
            singleConfiguration = true)
        fun `mis-aligned opening and closing quotes of a string template, false positive, case 1 (#1490)`() = Unit

        /**
         * No message like
         *
         * > only spaces are allowed for indentation and each indentation should
         * > equal to 4 spaces (tabs are not allowed): the same number of
         * > indents to the opening and closing quotes was expected
         *
         * should be reported.
         *
         * See [#1490](https://github.com/saveourtool/diktat/issues/1490).
         */
        @IndentationTest(IndentedSourceCode(
            """
            val value = f(
                "text ${'$'}variable text".trimIndent()
            )
            """),
            singleConfiguration = true)
        fun `mis-aligned opening and closing quotes of a string template, false positive, case 2 (#1490)`() = Unit

        /**
         * No message like
         *
         * > only spaces are allowed for indentation and each indentation should
         * > equal to 4 spaces (tabs are not allowed): the same number of
         * > indents to the opening and closing quotes was expected
         *
         * should be reported.
         *
         * See [#1490](https://github.com/saveourtool/diktat/issues/1490).
         */
        @IndentationTest(IndentedSourceCode(
            """
            val value = f(
                "text ${'$'}variable text".trimMargin()
            )
            """),
            singleConfiguration = true)
        fun `mis-aligned opening and closing quotes of a string template, false positive, case 3 (#1490)`() = Unit
    }

    /**
     * See [#1347](https://github.com/saveourtool/diktat/issues/1347).
     */
    @Nested
    @TestMethodOrder(NaturalDisplayName::class)
    inner class `Multi-line string literals` {
        @IndentationTest(
            first = IndentedSourceCode(
                """
                @Test
                fun `test method name`() {
                    @Language("kotlin")
                    val code =
                        ""${'"'}
                            @Suppress("diktat")
                            fun foo() {
                                val a = 1
                            }
                        ""${'"'}.trimIndent()
                    lintMethod(code)
                }
                """,
                extendedIndentOfParameters = FALSE,
                extendedIndentForExpressionBodies = FALSE,
                extendedIndentAfterOperators = FALSE,
                extendedIndentBeforeDot = FALSE),
            second = IndentedSourceCode(
                """
                @Test
                fun `test method name`() {
                    @Language("kotlin")
                    val code =
                            ""${'"'}
                                @Suppress("diktat")
                                fun foo() {
                                    val a = 1
                                }
                            ""${'"'}.trimIndent()
                    lintMethod(code)
                }
                """,
                extendedIndentOfParameters = TRUE,
                extendedIndentForExpressionBodies = TRUE,
                extendedIndentAfterOperators = TRUE,
                extendedIndentBeforeDot = TRUE),
            includeWarnTests = false
        )
        fun `no whitespace should be injected, case 1`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f0() {
                    @Language("kotlin")
                    val code =
                        ""${'"'}
                            |@Suppress("diktat")
                            |fun foo() {
                            |    val a = 1
                            |}
                        ""${'"'}.trimMargin()
                    lintMethod(code)
                }
                """,
                extendedIndentOfParameters = FALSE,
                extendedIndentForExpressionBodies = FALSE,
                extendedIndentAfterOperators = FALSE,
                extendedIndentBeforeDot = FALSE),
            second = IndentedSourceCode(
                """
                fun f0() {
                    @Language("kotlin")
                    val code =
                            ""${'"'}
                                |@Suppress("diktat")
                                |fun foo() {
                                |    val a = 1
                                |}
                            ""${'"'}.trimMargin()
                    lintMethod(code)
                }
                """,
                extendedIndentOfParameters = TRUE,
                extendedIndentForExpressionBodies = TRUE,
                extendedIndentAfterOperators = TRUE,
                extendedIndentBeforeDot = TRUE),
            includeWarnTests = false
        )
        fun `no whitespace should be injected, case 2`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f1() {
                    @Language("kotlin")
                    val code =
                        ""${'"'}
                            |@Suppress("diktat")
                            |fun foo() {
                            |    val a = 1
                            |}
                        ""${'"'}.trimMargin("|")
                    lintMethod(code)
                }
                """,
                extendedIndentOfParameters = FALSE,
                extendedIndentForExpressionBodies = FALSE,
                extendedIndentAfterOperators = FALSE,
                extendedIndentBeforeDot = FALSE),
            second = IndentedSourceCode(
                """
                fun f1() {
                    @Language("kotlin")
                    val code =
                            ""${'"'}
                                |@Suppress("diktat")
                                |fun foo() {
                                |    val a = 1
                                |}
                            ""${'"'}.trimMargin("|")
                    lintMethod(code)
                }
                """,
                extendedIndentOfParameters = TRUE,
                extendedIndentForExpressionBodies = TRUE,
                extendedIndentAfterOperators = TRUE,
                extendedIndentBeforeDot = TRUE),
            includeWarnTests = false
        )
        fun `no whitespace should be injected, case 3`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f2() {
                    @Language("kotlin")
                    val code =
                        ""${'"'}
                            >@Suppress("diktat")
                            >fun foo() {
                            >    val a = 1
                            >}
                        ""${'"'} . trimMargin ( marginPrefix = ">" )
                    lintMethod(code)
                }
                """,
                extendedIndentOfParameters = FALSE,
                extendedIndentForExpressionBodies = FALSE,
                extendedIndentAfterOperators = FALSE,
                extendedIndentBeforeDot = FALSE),
            second = IndentedSourceCode(
                """
                fun f2() {
                    @Language("kotlin")
                    val code =
                            ""${'"'}
                                >@Suppress("diktat")
                                >fun foo() {
                                >    val a = 1
                                >}
                            ""${'"'} . trimMargin ( marginPrefix = ">" )
                    lintMethod(code)
                }
                """,
                extendedIndentOfParameters = TRUE,
                extendedIndentForExpressionBodies = TRUE,
                extendedIndentAfterOperators = TRUE,
                extendedIndentBeforeDot = TRUE),
            includeWarnTests = false
        )
        fun `no whitespace should be injected, case 4`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun checkScript() {
                    lintMethod(
                        ""${'"'}
                                    |val A = "aa"
                        ""${'"'}.trimMargin(),
                    )
                }
                """,
                extendedIndentOfParameters = FALSE,
                extendedIndentForExpressionBodies = FALSE,
                extendedIndentAfterOperators = FALSE,
                extendedIndentBeforeDot = FALSE),
            second = IndentedSourceCode(
                """
                fun checkScript() {
                    lintMethod(
                            ""${'"'}
                                        |val A = "aa"
                            ""${'"'}.trimMargin(),
                    )
                }
                """,
                extendedIndentOfParameters = TRUE,
                extendedIndentForExpressionBodies = TRUE,
                extendedIndentAfterOperators = TRUE,
                extendedIndentBeforeDot = TRUE),
            includeWarnTests = false
        )
        fun `no whitespace should be injected, case 5`() = Unit
    }

    /**
     * Expressions wrapped on an operator or an infix function.
     *
     * See [#1340](https://github.com/saveourtool/diktat/issues/1340).
     */
    @Nested
    @TestMethodOrder(NaturalDisplayName::class)
    inner class `Expressions wrapped after operator` {
        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f() {
                    systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // diktat:WRONG_INDENTATION[expectedIndent = 12]
                }
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun f() {
                    systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE or
                            View.SYSTEM_UI_FLAG_FULLSCREEN or // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // diktat:WRONG_INDENTATION[expectedIndent = 8]
                }
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 1`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                val systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                val systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or // diktat:WRONG_INDENTATION[expectedIndent = 4]
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or // diktat:WRONG_INDENTATION[expectedIndent = 4]
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or // diktat:WRONG_INDENTATION[expectedIndent = 4]
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or // diktat:WRONG_INDENTATION[expectedIndent = 4]
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 2`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                const val FOO = 1

                const val BAR = 2

                const val BAZ = 4

                fun acceptInteger(arg: Int) = Unit

                fun main() {
                    acceptInteger(FOO or BAR or BAZ or FOO or BAR or BAZ or
                        FOO or BAR or BAZ or FOO or BAR or BAZ or FOO or BAR or BAZ or // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        FOO or BAR or BAZ) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                }
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                const val FOO = 1

                const val BAR = 2

                const val BAZ = 4

                fun acceptInteger(arg: Int) = Unit

                fun main() {
                    acceptInteger(FOO or BAR or BAZ or FOO or BAR or BAZ or
                            FOO or BAR or BAZ or FOO or BAR or BAZ or FOO or BAR or BAZ or // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            FOO or BAR or BAZ) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                }
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 3`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                const val TRUE = true

                const val FALSE = false

                fun acceptBoolean(arg: Boolean) = Unit

                fun f() {
                    acceptBoolean(TRUE ||
                        FALSE || // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        TRUE) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                }
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                const val TRUE = true

                const val FALSE = false

                fun acceptBoolean(arg: Boolean) = Unit

                fun f() {
                    acceptBoolean(TRUE ||
                            FALSE || // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            TRUE) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                }
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 4`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                val c = 3 +
                    2 // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                val c = 3 +
                        2 // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 5`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                infix fun Int.combineWith(that: Int) = this + that

                fun f() {
                    val x = 1 combineWith
                        2 combineWith // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        3 combineWith // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        4 combineWith // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        5 combineWith // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        6 // diktat:WRONG_INDENTATION[expectedIndent = 12]
                }
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                infix fun Int.combineWith(that: Int) = this + that

                fun f() {
                    val x = 1 combineWith
                            2 combineWith // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            3 combineWith // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            4 combineWith // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            5 combineWith // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            6 // diktat:WRONG_INDENTATION[expectedIndent = 8]
                }
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 6`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f(i1: Int, i2: Int, i3: Int): Int {
                    if (i2 > 0 &&
                        i3 < 0) { // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        return 2
                    }
                    return 0
                }
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun f(i1: Int, i2: Int, i3: Int): Int {
                    if (i2 > 0 &&
                            i3 < 0) { // diktat:WRONG_INDENTATION[expectedIndent = 8]
                        return 2
                    }
                    return 0
                }
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 7`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                val value1 = 1 to
                    2 to // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    3 // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                val value1 = 1 to
                        2 to // diktat:WRONG_INDENTATION[expectedIndent = 4]
                        3 // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 8`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                val value1a = (1 to
                    2 to // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    3) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                val value1a = (1 to
                        2 to // diktat:WRONG_INDENTATION[expectedIndent = 4]
                        3) // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 9`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                val value2 =
                    1 to
                        2 to // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        3 // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                val value2 =
                    1 to
                            2 to // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            3 // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 10`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                val value3 =
                    (1 to
                        2 to // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        3) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                val value3 =
                    (1 to
                            2 to // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            3) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 11`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value4 = identity(1 to
                    2 to // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    3) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value4 = identity(1 to
                        2 to // diktat:WRONG_INDENTATION[expectedIndent = 4]
                        3) // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 12`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value5 = identity(
                    1 to
                        2 to // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        3) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value5 = identity(
                    1 to
                            2 to // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            3) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 13`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value6 =
                    identity(1 to
                        2 to // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        3) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value6 =
                    identity(1 to
                            2 to // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            3) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 14`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                /**
                 * Line breaks:
                 *
                 * 1. before the expression body (`=`),
                 * 2. before the effective function arguments, and
                 * 3. on each infix function call ([to]).
                 */
                val value7 =
                    identity(
                        1 to
                            2 to // diktat:WRONG_INDENTATION[expectedIndent = 16]
                            3) // diktat:WRONG_INDENTATION[expectedIndent = 16]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                /**
                 * Line breaks:
                 *
                 * 1. before the expression body (`=`),
                 * 2. before the effective function arguments, and
                 * 3. on each infix function call ([to]).
                 */
                val value7 =
                    identity(
                        1 to
                                2 to // diktat:WRONG_INDENTATION[expectedIndent = 12]
                                3) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 15`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value8 = identity(identity(1 to
                    2 to // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    3)) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value8 = identity(identity(1 to
                        2 to // diktat:WRONG_INDENTATION[expectedIndent = 4]
                        3)) // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 16`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value9 = identity(identity(
                    1 to
                        2 to // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        3)) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value9 = identity(identity(
                    1 to
                            2 to // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            3)) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 17`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value10 =
                    identity(identity(1 to
                        2 to // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        3)) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value10 =
                    identity(identity(1 to
                            2 to // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            3)) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 18`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value11 =
                    identity(identity(
                        1 to
                            2 to // diktat:WRONG_INDENTATION[expectedIndent = 16]
                            3)) // diktat:WRONG_INDENTATION[expectedIndent = 16]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value11 =
                    identity(identity(
                        1 to
                                2 to // diktat:WRONG_INDENTATION[expectedIndent = 12]
                                3)) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 19`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                // Same as above, but using a custom getter instead of an explicit initializer.
                val value12
                    get() =
                        1 to
                            2 to // diktat:WRONG_INDENTATION[expectedIndent = 16]
                            3 // diktat:WRONG_INDENTATION[expectedIndent = 16]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                // Same as above, but using a custom getter instead of an explicit initializer.
                val value12
                    get() =
                        1 to
                                2 to // diktat:WRONG_INDENTATION[expectedIndent = 12]
                                3 // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 20`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                // Same as above, but using a custom getter instead of an explicit initializer.
                val value13
                    get() =
                        (1 to
                            2 to // diktat:WRONG_INDENTATION[expectedIndent = 16]
                            3) // diktat:WRONG_INDENTATION[expectedIndent = 16]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                // Same as above, but using a custom getter instead of an explicit initializer.
                val value13
                    get() =
                        (1 to
                                2 to // diktat:WRONG_INDENTATION[expectedIndent = 12]
                                3) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 21`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                // Same as above, but using a custom getter instead of an explicit initializer.
                val value14
                    get() =
                        identity(1 to
                            2 to // diktat:WRONG_INDENTATION[expectedIndent = 16]
                            3) // diktat:WRONG_INDENTATION[expectedIndent = 16]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                // Same as above, but using a custom getter instead of an explicit initializer.
                val value14
                    get() =
                        identity(1 to
                                2 to // diktat:WRONG_INDENTATION[expectedIndent = 12]
                                3) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 22`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                // Same as above, but using a custom getter instead of an explicit initializer.
                val value15
                    get() =
                        identity(identity(1 to
                            2 to // diktat:WRONG_INDENTATION[expectedIndent = 16]
                            3)) // diktat:WRONG_INDENTATION[expectedIndent = 16]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                // Same as above, but using a custom getter instead of an explicit initializer.
                val value15
                    get() =
                        identity(identity(1 to
                                2 to // diktat:WRONG_INDENTATION[expectedIndent = 12]
                                3)) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 23`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f() {
                    g(42 as
                        Integer) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                }
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun f() {
                    g(42 as
                            Integer) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                }
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 24`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f() {
                    g("" as?
                        String?) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                }
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun f() {
                    g("" as?
                            String?) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                }
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 25`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f() {
                    // The dot-qualified expression is always single-indented.
                    ""
                        .length as
                        Int // diktat:WRONG_INDENTATION[expectedIndent = 12]
                }
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun f() {
                    // The dot-qualified expression is always single-indented.
                    ""
                        .length as
                            Int // diktat:WRONG_INDENTATION[expectedIndent = 8]
                }
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 26`() = Unit
    }

    /**
     * Expressions wrapped before an operator or an infix function.
     *
     * See [#1340](https://github.com/saveourtool/diktat/issues/1340).
     */
    @Nested
    @TestMethodOrder(NaturalDisplayName::class)
    inner class `Expressions wrapped before operator` {
        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f() {
                    systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                        or View.SYSTEM_UI_FLAG_FULLSCREEN // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                }
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun f() {
                    systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                            or View.SYSTEM_UI_FLAG_FULLSCREEN // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                }
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 1`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                val systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                    or View.SYSTEM_UI_FLAG_FULLSCREEN // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                val systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                        or View.SYSTEM_UI_FLAG_FULLSCREEN // diktat:WRONG_INDENTATION[expectedIndent = 4]
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE // diktat:WRONG_INDENTATION[expectedIndent = 4]
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY // diktat:WRONG_INDENTATION[expectedIndent = 4]
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // diktat:WRONG_INDENTATION[expectedIndent = 4]
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 2`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                const val FOO = 1

                const val BAR = 2

                const val BAZ = 4

                fun acceptInteger(arg: Int) = Unit

                fun main() {
                    acceptInteger(FOO or BAR or BAZ or FOO or BAR or BAZ
                        or FOO or BAR or BAZ or FOO or BAR or BAZ or FOO or BAR or BAZ // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        or FOO or BAR or BAZ) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                }
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                const val FOO = 1

                const val BAR = 2

                const val BAZ = 4

                fun acceptInteger(arg: Int) = Unit

                fun main() {
                    acceptInteger(FOO or BAR or BAZ or FOO or BAR or BAZ
                            or FOO or BAR or BAZ or FOO or BAR or BAZ or FOO or BAR or BAZ // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            or FOO or BAR or BAZ) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                }
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 3`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                const val TRUE = true

                const val FALSE = false

                fun acceptBoolean(arg: Boolean) = Unit

                fun f() {
                    acceptBoolean(TRUE
                        || FALSE // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        || TRUE) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                }
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                const val TRUE = true

                const val FALSE = false

                fun acceptBoolean(arg: Boolean) = Unit

                fun f() {
                    acceptBoolean(TRUE
                            || FALSE // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            || TRUE) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                }
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 4`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                val c = (3
                    + 2) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                val c = (3
                        + 2) // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 5`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                infix fun Int.combineWith(that: Int) = this + that

                fun f() {
                    val x = (1
                        combineWith 2 // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        combineWith 3 // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        combineWith 4 // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        combineWith 5 // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        combineWith 6) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                }
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                infix fun Int.combineWith(that: Int) = this + that

                fun f() {
                    val x = (1
                            combineWith 2 // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            combineWith 3 // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            combineWith 4 // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            combineWith 5 // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            combineWith 6) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                }
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 6`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f(i1: Int, i2: Int, i3: Int): Int {
                    if (i2 > 0
                        && i3 < 0) { // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        return 2
                    }
                    return 0
                }
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun f(i1: Int, i2: Int, i3: Int): Int {
                    if (i2 > 0
                            && i3 < 0) { // diktat:WRONG_INDENTATION[expectedIndent = 8]
                        return 2
                    }
                    return 0
                }
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 7`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                val value1 = (1
                    to 2 // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    to 3) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                val value1 = (1
                        to 2 // diktat:WRONG_INDENTATION[expectedIndent = 4]
                        to 3) // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 8`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                val value2 =
                    (1
                        to 2 // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        to 3) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                val value2 =
                    (1
                            to 2 // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            to 3) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 9`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value3 = identity(1
                    to 2 // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    to 3) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value3 = identity(1
                        to 2 // diktat:WRONG_INDENTATION[expectedIndent = 4]
                        to 3) // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 10`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value4 = identity(
                    1
                        to 2 // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        to 3) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value4 = identity(
                    1
                            to 2 // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            to 3) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 11`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value5 =
                    identity(1
                        to 2 // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        to 3) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value5 =
                    identity(1
                            to 2 // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            to 3) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 12`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                /**
                 * Line breaks:
                 *
                 * 1. before the expression body (`=`),
                 * 2. before the effective function arguments, and
                 * 3. on each infix function call ([to]).
                 */
                val value6 =
                    identity(
                        1
                            to 2 // diktat:WRONG_INDENTATION[expectedIndent = 16]
                            to 3) // diktat:WRONG_INDENTATION[expectedIndent = 16]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                /**
                 * Line breaks:
                 *
                 * 1. before the expression body (`=`),
                 * 2. before the effective function arguments, and
                 * 3. on each infix function call ([to]).
                 */
                val value6 =
                    identity(
                        1
                                to 2 // diktat:WRONG_INDENTATION[expectedIndent = 12]
                                to 3) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 13`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value7 = identity(identity(1
                    to 2 // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    to 3)) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value7 = identity(identity(1
                        to 2 // diktat:WRONG_INDENTATION[expectedIndent = 4]
                        to 3)) // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 14`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value8 = identity(identity(
                    1
                        to 2 // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        to 3)) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value8 = identity(identity(
                    1
                            to 2 // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            to 3)) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 15`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value9 =
                    identity(identity(1
                        to 2 // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        to 3)) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value9 =
                    identity(identity(1
                            to 2 // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            to 3)) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 16`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value10 =
                    identity(identity(
                        1
                            to 2 // diktat:WRONG_INDENTATION[expectedIndent = 16]
                            to 3)) // diktat:WRONG_INDENTATION[expectedIndent = 16]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                val value10 =
                    identity(identity(
                        1
                                to 2 // diktat:WRONG_INDENTATION[expectedIndent = 12]
                                to 3)) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 17`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                // Same as above, but using a custom getter instead of an explicit initializer.
                val value11
                    get() =
                        (1
                            to 2 // diktat:WRONG_INDENTATION[expectedIndent = 16]
                            to 3) // diktat:WRONG_INDENTATION[expectedIndent = 16]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                // Same as above, but using a custom getter instead of an explicit initializer.
                val value11
                    get() =
                        (1
                                to 2 // diktat:WRONG_INDENTATION[expectedIndent = 12]
                                to 3) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 18`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                // Same as above, but using a custom getter instead of an explicit initializer.
                val value12
                    get() =
                        identity(1
                            to 2 // diktat:WRONG_INDENTATION[expectedIndent = 16]
                            to 3) // diktat:WRONG_INDENTATION[expectedIndent = 16]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                // Same as above, but using a custom getter instead of an explicit initializer.
                val value12
                    get() =
                        identity(1
                                to 2 // diktat:WRONG_INDENTATION[expectedIndent = 12]
                                to 3) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 19`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                // Same as above, but using a custom getter instead of an explicit initializer.
                val value13
                    get() =
                        identity(identity(1
                            to 2 // diktat:WRONG_INDENTATION[expectedIndent = 16]
                            to 3)) // diktat:WRONG_INDENTATION[expectedIndent = 16]
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun <T : Any> identity(t: T): T = t

                // Same as above, but using a custom getter instead of an explicit initializer.
                val value13
                    get() =
                        identity(identity(1
                                to 2 // diktat:WRONG_INDENTATION[expectedIndent = 12]
                                to 3)) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 20`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f() {
                    g(42
                        as Integer) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                }
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun f() {
                    g(42
                            as Integer) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                }
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 21`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f() {
                    g(""
                        as? String?) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                }
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun f() {
                    g(""
                            as? String?) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                }
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 22`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f() {
                    // The dot-qualified expression is always single-indented.
                    ""
                        .length
                        as Int // diktat:WRONG_INDENTATION[expectedIndent = 12]
                }
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun f() {
                    // The dot-qualified expression is always single-indented.
                    ""
                        .length
                            as Int // diktat:WRONG_INDENTATION[expectedIndent = 8]
                }
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 23`() = Unit
    }

    /**
     * Parenthesized expressions.
     *
     * See [#1409](https://github.com/saveourtool/diktat/issues/1409).
     */
    @Nested
    @TestMethodOrder(NaturalDisplayName::class)
    inner class `Parentheses-surrounded infix expressions` {
        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f1() = (
                        1 + 2 // diktat:WRONG_INDENTATION[expectedIndent = 4]
                )
                """,
                extendedIndentForExpressionBodies = FALSE,
                extendedIndentAfterOperators = TRUE),
            second = IndentedSourceCode(
                """
                fun f1() = (
                    1 + 2 // diktat:WRONG_INDENTATION[expectedIndent = 8]
                )
                """,
                extendedIndentForExpressionBodies = TRUE,
                extendedIndentAfterOperators = FALSE))
        fun `case 1`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f2() = (
                        1 + 2) // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentForExpressionBodies = FALSE,
                extendedIndentAfterOperators = TRUE),
            second = IndentedSourceCode(
                """
                fun f2() = (
                    1 + 2) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentForExpressionBodies = TRUE,
                extendedIndentAfterOperators = FALSE))
        fun `case 2`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f3() =
                    ( // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            1 + 2
                    ) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentForExpressionBodies = FALSE,
                extendedIndentAfterOperators = TRUE),
            second = IndentedSourceCode(
                """
                fun f3() =
                        ( // diktat:WRONG_INDENTATION[expectedIndent = 4]
                            1 + 2
                        ) // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentForExpressionBodies = TRUE,
                extendedIndentAfterOperators = FALSE))
        fun `case 3`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f4() =
                    ( // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            1 + 2)
                """,
                extendedIndentForExpressionBodies = FALSE,
                extendedIndentAfterOperators = TRUE),
            second = IndentedSourceCode(
                """
                fun f4() =
                        ( // diktat:WRONG_INDENTATION[expectedIndent = 4]
                            1 + 2)
                """,
                extendedIndentForExpressionBodies = TRUE,
                extendedIndentAfterOperators = FALSE))
        fun `case 4`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                const val v1 = (
                        1 + 2 // diktat:WRONG_INDENTATION[expectedIndent = 4]
                )
                """,
                extendedIndentForExpressionBodies = FALSE,
                extendedIndentAfterOperators = TRUE),
            second = IndentedSourceCode(
                """
                const val v1 = (
                    1 + 2 // diktat:WRONG_INDENTATION[expectedIndent = 8]
                )
                """,
                extendedIndentForExpressionBodies = TRUE,
                extendedIndentAfterOperators = FALSE))
        fun `case 5`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                const val v2 = (
                        1 + 2) // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentForExpressionBodies = FALSE,
                extendedIndentAfterOperators = TRUE),
            second = IndentedSourceCode(
                """
                const val v2 = (
                    1 + 2) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentForExpressionBodies = TRUE,
                extendedIndentAfterOperators = FALSE))
        fun `case 6`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                const val v3 =
                    ( // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            1 + 2
                    ) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentForExpressionBodies = FALSE,
                extendedIndentAfterOperators = TRUE),
            second = IndentedSourceCode(
                """
                const val v3 =
                        ( // diktat:WRONG_INDENTATION[expectedIndent = 4]
                            1 + 2
                        ) // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentForExpressionBodies = TRUE,
                extendedIndentAfterOperators = FALSE))
        fun `case 7`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                const val v4 =
                    ( // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            1 + 2)
                """,
                extendedIndentForExpressionBodies = FALSE,
                extendedIndentAfterOperators = TRUE),
            second = IndentedSourceCode(
                """
                const val v4 =
                        ( // diktat:WRONG_INDENTATION[expectedIndent = 4]
                            1 + 2)
                """,
                extendedIndentForExpressionBodies = TRUE,
                extendedIndentAfterOperators = FALSE))
        fun `case 8`() = Unit
    }

    /**
     * Dot-qualified and safe-access expressions.
     *
     * See [#1336](https://github.com/saveourtool/diktat/issues/1336).
     */
    @Nested
    @TestMethodOrder(NaturalDisplayName::class)
    inner class `Dot- and safe-qualified expressions` {
        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun LocalDateTime.updateTime(
                    hour: Int? = null,
                    minute: Int? = null,
                    second: Int? = null,
                ): LocalDateTime = withHour(hour ?: getHour())
                    .withMinute(minute ?: getMinute()) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    .withSecond(second ?: getSecond()) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentBeforeDot = FALSE),
            second = IndentedSourceCode(
                """
                fun LocalDateTime.updateTime(
                    hour: Int? = null,
                    minute: Int? = null,
                    second: Int? = null,
                ): LocalDateTime = withHour(hour ?: getHour())
                        .withMinute(minute ?: getMinute()) // diktat:WRONG_INDENTATION[expectedIndent = 4]
                        .withSecond(second ?: getSecond()) // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentBeforeDot = TRUE))
        fun `case 1`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f() {
                    first()
                        .second() // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        .third() // diktat:WRONG_INDENTATION[expectedIndent = 12]
                }
                """,
                extendedIndentBeforeDot = FALSE),
            second = IndentedSourceCode(
                """
                fun f() {
                    first()
                            .second() // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            .third() // diktat:WRONG_INDENTATION[expectedIndent = 8]
                }
                """,
                extendedIndentBeforeDot = TRUE))
        fun `case 2`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                val a = first()
                    .second() // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    .third() // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentBeforeDot = FALSE),
            second = IndentedSourceCode(
                """
                val a = first()
                        .second() // diktat:WRONG_INDENTATION[expectedIndent = 4]
                        .third() // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentBeforeDot = TRUE))
        fun `case 3`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                val b = first()
                    ?.second() // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    ?.third() // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentBeforeDot = FALSE),
            second = IndentedSourceCode(
                """
                val b = first()
                        ?.second() // diktat:WRONG_INDENTATION[expectedIndent = 4]
                        ?.third() // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentBeforeDot = TRUE))
        fun `case 4`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f1() = first()
                    .second() // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    .third() // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentBeforeDot = FALSE),
            second = IndentedSourceCode(
                """
                fun f1() = first()
                        .second() // diktat:WRONG_INDENTATION[expectedIndent = 4]
                        .third() // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentBeforeDot = TRUE))
        fun `case 5`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f2() =
                    first()
                        .second() // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        .third() // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentBeforeDot = FALSE),
            second = IndentedSourceCode(
                """
                fun f2() =
                    first()
                            .second() // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            .third() // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentBeforeDot = TRUE))
        fun `case 6`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f3() = g(first()
                    .second() // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    .third() // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    .fourth()) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentBeforeDot = FALSE),
            second = IndentedSourceCode(
                """
                fun f3() = g(first()
                        .second() // diktat:WRONG_INDENTATION[expectedIndent = 4]
                        .third() // diktat:WRONG_INDENTATION[expectedIndent = 4]
                        .fourth()) // diktat:WRONG_INDENTATION[expectedIndent = 4]
                """,
                extendedIndentBeforeDot = TRUE))
        fun `case 7`() = Unit

        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f4() = g(
                    first()
                        .second() // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        .third() // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        .fourth()) // diktat:WRONG_INDENTATION[expectedIndent = 12]
                """,
                extendedIndentBeforeDot = FALSE),
            second = IndentedSourceCode(
                """
                fun f4() = g(
                    first()
                            .second() // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            .third() // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            .fourth()) // diktat:WRONG_INDENTATION[expectedIndent = 8]
                """,
                extendedIndentBeforeDot = TRUE))
        fun `case 8`() = Unit
    }

    @Nested
    @TestMethodOrder(NaturalDisplayName::class)
    inner class `If expressions` {
        /**
         * #1351, case 1.
         *
         * Boolean operator priority (`&&` has higher priority than `||`).
         *
         * Currently, this is an incorrectly formatted code kept to detect the
         * contract breakage. It will be re-formatted once the issue is fixed.
         */
        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f1() {
                    if (valueParameterNode.parents().none { it.elementType == PRIMARY_CONSTRUCTOR } ||
                        !valueParameterNode.hasChildOfType(VAL_KEYWORD) &&  // diktat:WRONG_INDENTATION[expectedIndent = 12]
                            !valueParameterNode.hasChildOfType(VAR_KEYWORD) // diktat:WRONG_INDENTATION[expectedIndent = 16]
                    ) {
                        return
                    }
                }
                """,
                extendedIndentAfterOperators = FALSE),
            second = IndentedSourceCode(
                """
                fun f1() {
                    if (valueParameterNode.parents().none { it.elementType == PRIMARY_CONSTRUCTOR } ||
                            !valueParameterNode.hasChildOfType(VAL_KEYWORD) &&      // diktat:WRONG_INDENTATION[expectedIndent =  8]
                                    !valueParameterNode.hasChildOfType(VAR_KEYWORD) // diktat:WRONG_INDENTATION[expectedIndent = 16]
                    ) {
                        return
                    }
                }
                """,
                extendedIndentAfterOperators = TRUE))
        fun `case 1`() = Unit

        /**
         * #1351, case 2.
         *
         * IDEA combines the values of `CONTINUATION_INDENT_IN_IF_CONDITIONS`
         * and `CONTINUATION_INDENT_FOR_CHAINED_CALLS`, so the resulting indent
         * can be anything between 8 (2x) and 16 (4x).
         *
         * Currently, this is an incorrectly formatted code kept to detect the
         * contract breakage. It will be re-formatted once the issue is fixed.
         */
        @IndentationTest(
            first = IndentedSourceCode(
                """
                fun f2() {
                    val prevComment = if (valueParameterNode.siblings(forward = false)
                        .takeWhile { it.elementType != EOL_COMMENT && it.elementType != BLOCK_COMMENT } // diktat:WRONG_INDENTATION[expectedIndent = 12]
                        .all { it.elementType == WHITE_SPACE }                                          // diktat:WRONG_INDENTATION[expectedIndent = 12]
                    ) {
                        0
                    } else {
                        1
                    }
                }
                """,
                extendedIndentBeforeDot = FALSE),
            second = IndentedSourceCode(
                """
                fun f2() {
                    val prevComment = if (valueParameterNode.siblings(forward = false)
                            .takeWhile { it.elementType != EOL_COMMENT && it.elementType != BLOCK_COMMENT } // diktat:WRONG_INDENTATION[expectedIndent = 8]
                            .all { it.elementType == WHITE_SPACE }                                          // diktat:WRONG_INDENTATION[expectedIndent = 8]
                    ) {
                        0
                    } else {
                        1
                    }
                }
                """,
                extendedIndentBeforeDot = TRUE))
        fun `case 2`() = Unit
    }

    @Nested
    @TestMethodOrder(NaturalDisplayName::class)
    inner class `Comments inside binary expressions` {
        @IndentationTest(
            IndentedSourceCode(
                """
                val x: Int = functionCall()
                    // This is a comment
                    ?: 42
                """),
            singleConfiguration = true)
        fun `case 1`() = Unit

        @IndentationTest(
            IndentedSourceCode(
                """
                val x: Int = functionCall() as Int?
                    // This is a comment
                    ?: 42
                """),
            singleConfiguration = true)
        fun `case 2`() = Unit

        @IndentationTest(
            IndentedSourceCode(
                """
                val x: Int = null as Int?
                    // This is a comment
                    ?: 42
                """),
            singleConfiguration = true)
        fun `case 3`() = Unit

        @IndentationTest(
            IndentedSourceCode(
                """
                val x: Int = 42 as Int?
                    // This is a comment
                    ?: 42
                """),
            singleConfiguration = true)
        fun `case 4`() = Unit

        @IndentationTest(
            IndentedSourceCode(
                """
                val x: Boolean = functionCall()
                    /*
                     * This is a block comment
                     */
                    ?: true
                """),
            singleConfiguration = true)
        fun `case 5`() = Unit

        @IndentationTest(
            IndentedSourceCode(
                """
                val x: Boolean = functionCall() as Boolean?
                    /*
                     * This is a block comment
                     */
                    ?: true
                """),
            singleConfiguration = true)
        fun `case 6`() = Unit

        @IndentationTest(
            IndentedSourceCode(
                """
                val x: Boolean = null as Boolean?
                    /*
                     * This is a block comment
                     */
                    ?: true
                """),
            singleConfiguration = true)
        fun `case 7`() = Unit

        @IndentationTest(
            IndentedSourceCode(
                """
                val x: Boolean = true as Boolean?
                    /*
                     * This is a block comment
                     */
                    ?: true
                """),
            singleConfiguration = true)
        fun `case 8`() = Unit

        @IndentationTest(
            IndentedSourceCode(
                """
                fun f(): String {
                    return functionCall()
                        // This is a comment
                        // This is the 2nd line of the comment
                        ?: "default value"
                }
                """),
            singleConfiguration = true)
        fun `case 9`() = Unit

        @IndentationTest(
            IndentedSourceCode(
                """
                fun f(): String {
                    return functionCall() as String?
                        // This is a comment
                        // This is the 2nd line of the comment
                        ?: "default value"
                }
                """),
            singleConfiguration = true)
        fun `case 10`() = Unit

        @IndentationTest(
            IndentedSourceCode(
                """
                fun f(): String {
                    return null as String?
                        // This is a comment
                        // This is the 2nd line of the comment
                        ?: "default value"
                }
                """),
            singleConfiguration = true)
        fun `case 11`() = Unit

        @IndentationTest(
            IndentedSourceCode(
                """
                fun f(): String {
                    return "return value" as String?
                        // This is a comment
                        // This is the 2nd line of the comment
                        ?: "default value"
                        // This is a comment
                        // This is the 2nd line of the comment
                        ?: "unreachable code"
                }
                """),
            singleConfiguration = true)
        fun `case 12`() = Unit
    }

    @Nested
    @TestMethodOrder(NaturalDisplayName::class)
    inner class `Multi-line Elvis expressions` {
        @IndentationTest(
            IndentedSourceCode(
                """
                val elvisExpressionInsideBinaryExpressionA = true &&
                        ""
                            ?.trim()
                            ?.trim()
                            ?.trim()
                            ?.isEmpty()
                        ?: true
                """),
            singleConfiguration = true)
        fun `case 1`() = Unit

        @IndentationTest(
            IndentedSourceCode(
                """
                val elvisExpressionInsideBinaryExpressionB = false ||
                        ""
                            .trim()
                            .trim()
                            .trim()
                            .isEmpty()
                        ?: true
                """),
            singleConfiguration = true)
        fun `case 2`() = Unit

        @IndentationTest(
            IndentedSourceCode(
                """
                val elvisExpressionInsideBinaryExpressionC = true &&
                        null as Boolean?
                        ?: true
                """),
            singleConfiguration = true)
        fun `case 3`() = Unit

        @IndentationTest(
            IndentedSourceCode(
                """
                val elvisExpressionInsideBinaryExpressionD = false ||
                        (null as Boolean?)
                        ?: true
                """),
            singleConfiguration = true)
        fun `case 4`() = Unit

        @IndentationTest(
            IndentedSourceCode(
                """
                val elvisExpressionInsideBinaryExpressionE = true &&
                        (42 as? Boolean)
                        ?: true
                """),
            singleConfiguration = true)
        fun `case 5`() = Unit

        /**
         * _Elvis_ after a _safe-access_ expression should have the same
         * indentation level as the previous function calls.
         */
        @IndentationTest(
            IndentedSourceCode(
                """
                val elvisAfterSafeAccess = ""
                    ?.trim()
                    ?.trim()
                    ?.trim()
                    ?.isEmpty()
                    ?: ""
                """),
            singleConfiguration = true)
        fun `case 6`() = Unit

        /**
         * _Elvis_ after a _dot-qualified_ expression should have the same
         * indentation level as the previous function calls.
         */
        @IndentationTest(
            IndentedSourceCode(
                """
                val elvisAfterDotQualified = ""
                    .trim()
                    .trim()
                    .trim()
                    .isEmpty()
                    ?: ""
                """),
            singleConfiguration = true)
        fun `case 7`() = Unit

        @IndentationTest(
            IndentedSourceCode(
                """
                fun f(): Boolean {
                    return list.getChildren(null)
                        .none { it.elementType in badModifiers } &&
                            classBody?.getAllChildrenWithType(FUN)
                                ?.isEmpty()
                            ?: false &&
                            getFirstChildWithType(SUPER_TYPE_LIST) == null
                }
                """),
            singleConfiguration = true)
        fun `case 8`() = Unit

        @IndentationTest(
            IndentedSourceCode(
                """
                fun f(): Boolean {
                    return classBody?.getFirstChildWithType(FUN) == null &&
                            getFirstChildWithType(SUPER_TYPE_LIST) == null &&
                            // if there is any prop with logic in accessor then don't recommend to convert class to data class
                            classBody?.let(::areGoodProps)
                            ?: true
                }
                """),
            singleConfiguration = true)
        fun `case 9`() = Unit

        @IndentationTest(
            IndentedSourceCode(
                """
                fun f(): Boolean {
                    return block.getChildrenOfType<KtProperty>()
                        .any { it.nameAsName == property.nameAsName && expression.node.isGoingAfter(it.node) } ||
                            block.parent
                                .let { it as? KtFunctionLiteral }
                                ?.valueParameters
                                ?.any { it.nameAsName == property.nameAsName }
                            ?: false
                }
                """),
            singleConfiguration = true)
        fun `case 10`() = Unit

        @IndentationTest(
            IndentedSourceCode(
                """
                fun f(): Boolean {
                    return blockExpression
                        .statements
                        .takeWhile { !it.isAncestor(this, true) }
                        .mapNotNull { it as? KtProperty }
                        .find {
                            it.isLocal &&
                                    it.hasInitializer() &&
                                    it.name?.equals(getReferencedName())
                                    ?: false
                        }
                }
                """),
            singleConfiguration = true)
        fun `case 11`() = Unit

        @IndentationTest(
            IndentedSourceCode(
                """
                fun f(): Any {
                    return siblings(forward = true, withItself = false)
                        .filterNot { it.node.isPartOfComment() || it is PsiWhiteSpace }
                        .takeWhile {
                            // statements like `name.field = value` where name == propertyName
                            it is KtBinaryExpression && it.node.findChildByType(OPERATION_REFERENCE)?.findChildByType(EQ) != null &&
                                    (it.left as? KtDotQualifiedExpression)?.run {
                                        (receiverExpression as? KtNameReferenceExpression)?.getReferencedName() == propertyName
                                    }
                                    ?: false
                        }
                }
                """),
            singleConfiguration = true)
        fun `case 12`() = Unit

        @IndentationTest(
            IndentedSourceCode(
                """
                fun f(): Any {
                    return blockExpression
                        .statements
                        .takeWhile { !it.isAncestor(this, true) }
                        .mapNotNull { it as? KtProperty }
                        .find {
                            it.isLocal &&
                                    it.hasInitializer() &&
                                    it.name?.equals(getReferencedName())
                                    ?: false
                        }
                }
                """),
            singleConfiguration = true)
        fun `case 13`() = Unit
    }
}
