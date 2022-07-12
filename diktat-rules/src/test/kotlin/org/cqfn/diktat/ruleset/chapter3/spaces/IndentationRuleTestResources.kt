package org.cqfn.diktat.ruleset.chapter3.spaces

import org.intellij.lang.annotations.Language

/**
 * Test resources shared by [IndentationRuleWarnTest] and
 * [IndentationRuleFixTest].
 *
 * @see IndentationRuleWarnTest
 * @see IndentationRuleFixTest
 */
@Suppress("LargeClass")
internal object IndentationRuleTestResources {
    /**
     * See [#1330](https://github.com/saveourtool/diktat/issues/1330).
     *
     * @see expressionBodyFunctionsContinuationIndent
     */
    @Language("kotlin")
    private val expressionBodyFunctionsSingleIndent = arrayOf(
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

        """
        |fun f() =
        |    x + (y +
        |        g(x)
        |    )
        """.trimMargin(),

        """
        |fun f() =
        |    (1 +
        |        2)
        """.trimMargin(),
    )

    /**
     * See [#1330](https://github.com/saveourtool/diktat/issues/1330).
     *
     * @see expressionBodyFunctionsSingleIndent
     */
    @Language("kotlin")
    private val expressionBodyFunctionsContinuationIndent = arrayOf(
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
        |                    View.SYSTEM_UI_FLAG_FULLSCREEN or
        |                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
        |                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
        |                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
        |                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
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

        """
        |fun f() =
        |        x + (y +
        |            g(x)
        |        )
        """.trimMargin(),

        """
        |fun f() =
        |        (1 +
        |            2)
        """.trimMargin(),
    )

    /**
     * See [#1330](https://github.com/saveourtool/diktat/issues/1330).
     */
    val expressionBodyFunctions = mapOf(
        false to expressionBodyFunctionsSingleIndent,
        true to expressionBodyFunctionsContinuationIndent)

    /**
     * See [#1347](https://github.com/saveourtool/diktat/issues/1347).
     *
     * @see whitespaceInStringLiteralsContinuationIndent
     */
    @Language("kotlin")
    private val whitespaceInStringLiteralsSingleIndent = arrayOf(
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
    @Language("kotlin")
    private val whitespaceInStringLiteralsContinuationIndent = arrayOf(
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
     * See [#1347](https://github.com/saveourtool/diktat/issues/1347).
     */
    val whitespaceInStringLiterals = mapOf(
        false to whitespaceInStringLiteralsSingleIndent,
        true to whitespaceInStringLiteralsContinuationIndent)

    /**
     * Expressions wrapped on an operator or an infix function, single indent
     * (`extendedIndentAfterOperators` is **off**).
     *
     * When adding new code fragments to this list, be sure to also add their
     * counterparts (preserving order) to [expressionsWrappedAfterOperatorContinuationIndent].
     *
     * See [#1340](https://github.com/saveourtool/diktat/issues/1340).
     *
     * @see expressionsWrappedAfterOperatorContinuationIndent
     */
    @Language("kotlin")
    private val expressionsWrappedAfterOperatorSingleIndent = arrayOf(
        """
        |fun f() {
        |    systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE or
        |        View.SYSTEM_UI_FLAG_FULLSCREEN or
        |        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
        |        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
        |        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
        |        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        |}
        """.trimMargin(),

        """
        |val systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE or
        |    View.SYSTEM_UI_FLAG_FULLSCREEN or
        |    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
        |    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
        |    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
        |    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        """.trimMargin(),

        """
        |const val FOO = 1
        |
        |const val BAR = 2
        |
        |const val BAZ = 4
        |
        |fun acceptInteger(arg: Int) = Unit
        |
        |fun main() {
        |    acceptInteger(FOO or BAR or BAZ or FOO or BAR or BAZ or
        |        FOO or BAR or BAZ or FOO or BAR or BAZ or FOO or BAR or BAZ or
        |        FOO or BAR or BAZ)
        |}
        """.trimMargin(),

        """
        |const val TRUE = true
        |
        |const val FALSE = false
        |
        |fun acceptBoolean(arg: Boolean) = Unit
        |
        |fun f() {
        |    acceptBoolean(TRUE ||
        |        FALSE ||
        |        TRUE)
        |}
        """.trimMargin(),

        """
        |val c = 3 +
        |    2
        """.trimMargin(),

        """
        |infix fun Int.combineWith(that: Int) = this + that
        |
        |fun f() {
        |    val x = 1 combineWith
        |        2 combineWith
        |        3 combineWith
        |        4 combineWith
        |        5 combineWith
        |        6
        |}
        """.trimMargin(),

        """
        |fun f(i1: Int, i2: Int, i3: Int): Int {
        |    if (i2 > 0 &&
        |        i3 < 0) {
        |        return 2
        |    }
        |    return 0
        |}
        """.trimMargin(),

        """
        |val value1 = 1 to
        |    2 to
        |    3
        """.trimMargin(),

        """
        |val value1a = (1 to
        |    2 to
        |    3)
        """.trimMargin(),

        """
        |val value2 =
        |    1 to
        |        2 to
        |        3
        """.trimMargin(),

        """
        |val value3 =
        |    (1 to
        |        2 to
        |        3)
        """.trimMargin(),

        """
        |fun <T : Any> identity(t: T): T = t
        |
        |val value4 = identity(1 to
        |    2 to
        |    3)
        """.trimMargin(),

        """
        |fun <T : Any> identity(t: T): T = t
        |
        |val value5 = identity(
        |    1 to
        |        2 to
        |        3)
        """.trimMargin(),

        """
        |fun <T : Any> identity(t: T): T = t
        |
        |val value6 =
        |    identity(1 to
        |        2 to
        |        3)
        """.trimMargin(),

        """
        |fun <T : Any> identity(t: T): T = t
        |
        |/**
        | * Line breaks:
        | *
        | * 1. before the expression body (`=`),
        | * 2. before the effective function arguments, and
        | * 3. on each infix function call ([to]).
        | */
        |val value7 =
        |    identity(
        |        1 to
        |            2 to
        |            3)
        """.trimMargin(),

        """
        |fun <T : Any> identity(t: T): T = t
        |
        |val value8 = identity(identity(1 to
        |    2 to
        |    3))
        """.trimMargin(),

        """
        |fun <T : Any> identity(t: T): T = t
        |
        |val value9 = identity(identity(
        |    1 to
        |        2 to
        |        3))
        """.trimMargin(),

        """
        |fun <T : Any> identity(t: T): T = t
        |
        |val value10 =
        |    identity(identity(1 to
        |        2 to
        |        3))
        """.trimMargin(),

        """
        |fun <T : Any> identity(t: T): T = t
        |
        |val value11 =
        |    identity(identity(
        |        1 to
        |            2 to
        |            3))
        """.trimMargin(),

        """
        |// Same as above, but using a custom getter instead of an explicit initializer.
        |val value12
        |    get() =
        |        1 to
        |            2 to
        |            3
        """.trimMargin(),

        """
        |// Same as above, but using a custom getter instead of an explicit initializer.
        |val value13
        |    get() =
        |        (1 to
        |            2 to
        |            3)
        """.trimMargin(),

        """
        |fun <T : Any> identity(t: T): T = t
        |
        |// Same as above, but using a custom getter instead of an explicit initializer.
        |val value14
        |    get() =
        |        identity(1 to
        |            2 to
        |            3)
        """.trimMargin(),

        """
        |fun <T : Any> identity(t: T): T = t
        |
        |// Same as above, but using a custom getter instead of an explicit initializer.
        |val value15
        |    get() =
        |        identity(identity(1 to
        |            2 to
        |            3))
        """.trimMargin(),
    )

    /**
     * Expressions wrapped on an operator or an infix function, continuation
     * indent (`extendedIndentAfterOperators` is **on**).
     *
     * When adding new code fragments to this list, be sure to also add their
     * counterparts (preserving order) to [expressionsWrappedAfterOperatorSingleIndent].
     *
     * See [#1340](https://github.com/saveourtool/diktat/issues/1340).
     *
     * @see expressionsWrappedAfterOperatorSingleIndent
     */
    @Language("kotlin")
    private val expressionsWrappedAfterOperatorContinuationIndent = arrayOf(
        """
        |fun f() {
        |    systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE or
        |            View.SYSTEM_UI_FLAG_FULLSCREEN or
        |            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
        |            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
        |            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
        |            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        |}
        """.trimMargin(),

        """
        |val systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE or
        |        View.SYSTEM_UI_FLAG_FULLSCREEN or
        |        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
        |        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
        |        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
        |        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        """.trimMargin(),

        """
        |const val FOO = 1
        |
        |const val BAR = 2
        |
        |const val BAZ = 4
        |
        |fun acceptInteger(arg: Int) = Unit
        |
        |fun main() {
        |    acceptInteger(FOO or BAR or BAZ or FOO or BAR or BAZ or
        |            FOO or BAR or BAZ or FOO or BAR or BAZ or FOO or BAR or BAZ or
        |            FOO or BAR or BAZ)
        |}
        """.trimMargin(),

        """
        |const val TRUE = true
        |
        |const val FALSE = false
        |
        |fun acceptBoolean(arg: Boolean) = Unit
        |
        |fun f() {
        |    acceptBoolean(TRUE ||
        |            FALSE ||
        |            TRUE)
        |}
        """.trimMargin(),

        """
        |val c = 3 +
        |        2
        """.trimMargin(),

        """
        |infix fun Int.combineWith(that: Int) = this + that
        |
        |fun f() {
        |    val x = 1 combineWith
        |            2 combineWith
        |            3 combineWith
        |            4 combineWith
        |            5 combineWith
        |            6
        |}
        """.trimMargin(),

        """
        |fun f(i1: Int, i2: Int, i3: Int): Int {
        |    if (i2 > 0 &&
        |            i3 < 0) {
        |        return 2
        |    }
        |    return 0
        |}
        """.trimMargin(),

        """
        |val value1 = 1 to
        |        2 to
        |        3
        """.trimMargin(),

        """
        |val value1a = (1 to
        |        2 to
        |        3)
        """.trimMargin(),

        """
        |val value2 =
        |    1 to
        |            2 to
        |            3
        """.trimMargin(),

        """
        |val value3 =
        |    (1 to
        |            2 to
        |            3)
        """.trimMargin(),

        """
        |fun <T : Any> identity(t: T): T = t
        |
        |val value4 = identity(1 to
        |        2 to
        |        3)
        """.trimMargin(),

        """
        |fun <T : Any> identity(t: T): T = t
        |
        |val value5 = identity(
        |    1 to
        |            2 to
        |            3)
        """.trimMargin(),

        """
        |fun <T : Any> identity(t: T): T = t
        |
        |val value6 =
        |    identity(1 to
        |            2 to
        |            3)
        """.trimMargin(),

        """
        |fun <T : Any> identity(t: T): T = t
        |
        |/**
        | * Line breaks:
        | *
        | * 1. before the expression body (`=`),
        | * 2. before the effective function arguments, and
        | * 3. on each infix function call ([to]).
        | */
        |val value7 =
        |    identity(
        |        1 to
        |                2 to
        |                3)
        """.trimMargin(),

        """
        |fun <T : Any> identity(t: T): T = t
        |
        |val value8 = identity(identity(1 to
        |        2 to
        |        3))
        """.trimMargin(),

        """
        |fun <T : Any> identity(t: T): T = t
        |
        |val value9 = identity(identity(
        |    1 to
        |            2 to
        |            3))
        """.trimMargin(),

        """
        |fun <T : Any> identity(t: T): T = t
        |
        |val value10 =
        |    identity(identity(1 to
        |            2 to
        |            3))
        """.trimMargin(),

        """
        |fun <T : Any> identity(t: T): T = t
        |
        |val value11 =
        |    identity(identity(
        |        1 to
        |                2 to
        |                3))
        """.trimMargin(),

        """
        |// Same as above, but using a custom getter instead of an explicit initializer.
        |val value12
        |    get() =
        |        1 to
        |                2 to
        |                3
        """.trimMargin(),

        """
        |// Same as above, but using a custom getter instead of an explicit initializer.
        |val value13
        |    get() =
        |        (1 to
        |                2 to
        |                3)
        """.trimMargin(),

        """
        |fun <T : Any> identity(t: T): T = t
        |
        |// Same as above, but using a custom getter instead of an explicit initializer.
        |val value14
        |    get() =
        |        identity(1 to
        |                2 to
        |                3)
        """.trimMargin(),

        """
        |fun <T : Any> identity(t: T): T = t
        |
        |// Same as above, but using a custom getter instead of an explicit initializer.
        |val value15
        |    get() =
        |        identity(identity(1 to
        |                2 to
        |                3))
        """.trimMargin(),
    )

    /**
     * Expressions wrapped on an operator or an infix function.
     *
     * See [#1340](https://github.com/saveourtool/diktat/issues/1340).
     */
    val expressionsWrappedAfterOperator = mapOf(
        false to expressionsWrappedAfterOperatorSingleIndent,
        true to expressionsWrappedAfterOperatorContinuationIndent)

    /**
     * Parenthesized expressions, single indent
     * (`extendedIndentForExpressionBodies` is **off**).
     *
     * When adding new code fragments to this list, be sure to also add their
     * counterparts (preserving order) to
     * [parenthesesSurroundedInfixExpressionsContinuationIndent].
     *
     * See [#1409](https://github.com/saveourtool/diktat/issues/1409).
     *
     * @see parenthesesSurroundedInfixExpressionsContinuationIndent
     */
    @Language("kotlin")
    private val parenthesesSurroundedInfixExpressionsSingleIndent = arrayOf(
        """
        |fun f1() = (
        |    1 + 2
        |)
        """.trimMargin(),

        """
        |fun f2() = (
        |    1 + 2)
        """.trimMargin(),

        """
        |fun f3() =
        |    (
        |        1 + 2
        |    )
        """.trimMargin(),

        """
        |fun f4() =
        |    (
        |        1 + 2)
        """.trimMargin(),

        """
        |const val v1 = (
        |    1 + 2
        |)
        """.trimMargin(),

        """
        |const val v2 = (
        |    1 + 2)
        """.trimMargin(),

        """
        |const val v3 =
        |    (
        |        1 + 2
        |    )
        """.trimMargin(),

        """
        |const val v4 =
        |    (
        |        1 + 2)
        """.trimMargin(),
    )

    /**
     * Parenthesized expressions, continuation indent
     * (`extendedIndentForExpressionBodies` is **on**).
     *
     * When adding new code fragments to this list, be sure to also add their
     * counterparts (preserving order) to
     * [parenthesesSurroundedInfixExpressionsSingleIndent].
     *
     * See [#1409](https://github.com/saveourtool/diktat/issues/1409).
     *
     * @see parenthesesSurroundedInfixExpressionsSingleIndent
     */
    @Language("kotlin")
    private val parenthesesSurroundedInfixExpressionsContinuationIndent = arrayOf(
        """
        |fun f1() = (
        |    1 + 2
        |)
        """.trimMargin(),

        """
        |fun f2() = (
        |    1 + 2)
        """.trimMargin(),

        """
        |fun f3() =
        |        (
        |            1 + 2
        |        )
        """.trimMargin(),

        """
        |fun f4() =
        |        (
        |            1 + 2)
        """.trimMargin(),

        """
        |const val v1 = (
        |    1 + 2
        |)
        """.trimMargin(),

        """
        |const val v2 = (
        |    1 + 2)
        """.trimMargin(),

        """
        |const val v3 =
        |        (
        |            1 + 2
        |        )
        """.trimMargin(),

        """
        |const val v4 =
        |        (
        |            1 + 2)
        """.trimMargin(),
    )

    /**
     * Parenthesized expressions.
     *
     * See [#1409](https://github.com/saveourtool/diktat/issues/1409).
     */
    val parenthesesSurroundedInfixExpressions = mapOf(
        false to parenthesesSurroundedInfixExpressionsSingleIndent,
        true to parenthesesSurroundedInfixExpressionsContinuationIndent)

    /**
     * Dot-qualified and safe-access expressions, single indent
     * (`extendedIndentBeforeDot` is **off**).
     *
     * When adding new code fragments to this list, be sure to also add their
     * counterparts (preserving order) to
     * [dotQualifiedExpressionsContinuationIndent].
     *
     * See [#1336](https://github.com/saveourtool/diktat/issues/1336).
     *
     * @see dotQualifiedExpressionsContinuationIndent
     */
    @Language("kotlin")
    val dotQualifiedExpressionsSingleIndent = arrayOf(
        """
        |fun LocalDateTime.updateTime(
        |    hour: Int? = null,
        |    minute: Int? = null,
        |    second: Int? = null,
        |): LocalDateTime = withHour(hour ?: getHour())
        |    .withMinute(minute ?: getMinute())
        |    .withSecond(second ?: getSecond())
        """.trimMargin(),

        """
        |fun f() {
        |    first()
        |        .second()
        |        .third()
        |}
        """.trimMargin(),

        """
        |val a = first()
        |    .second()
        |    .third()
        """.trimMargin(),

        """
        |val b = first()
        |    ?.second()
        |    ?.third()
        """.trimMargin(),

        """
        |fun f1() = first()
        |    .second()
        |    .third()
        """.trimMargin(),

        """
        |fun f2() =
        |    first()
        |        .second()
        |        .third()
        """.trimMargin(),

        """
        |fun f3() = g(first()
        |    .second()
        |    .third()
        |    .fourth())
        """.trimMargin(),

        """
        |fun f4() = g(
        |    first()
        |        .second()
        |        .third()
        |        .fourth())
        """.trimMargin(),
    )

    /**
     * Dot-qualified and safe-access expressions, continuation indent
     * (`extendedIndentBeforeDot` is **on**).
     *
     * When adding new code fragments to this list, be sure to also add their
     * counterparts (preserving order) to
     * [dotQualifiedExpressionsSingleIndent].
     *
     * See [#1336](https://github.com/saveourtool/diktat/issues/1336).
     *
     * @see dotQualifiedExpressionsSingleIndent
     */
    @Language("kotlin")
    val dotQualifiedExpressionsContinuationIndent = arrayOf(
        """
        |fun LocalDateTime.updateTime(
        |    hour: Int? = null,
        |    minute: Int? = null,
        |    second: Int? = null,
        |): LocalDateTime = withHour(hour ?: getHour())
        |        .withMinute(minute ?: getMinute())
        |        .withSecond(second ?: getSecond())
        """.trimMargin(),

        """
        |fun f() {
        |    first()
        |            .second()
        |            .third()
        |}
        """.trimMargin(),

        """
        |val a = first()
        |        .second()
        |        .third()
        """.trimMargin(),

        """
        |val b = first()
        |        ?.second()
        |        ?.third()
        """.trimMargin(),

        """
        |fun f1() = first()
        |        .second()
        |        .third()
        """.trimMargin(),

        """
        |fun f2() =
        |    first()
        |            .second()
        |            .third()
        """.trimMargin(),

        """
        |fun f3() = g(first()
        |        .second()
        |        .third()
        |        .fourth())
        """.trimMargin(),

        """
        |fun f4() = g(
        |    first()
        |            .second()
        |            .third()
        |            .fourth())
        """.trimMargin(),
    )

    /**
     * Dot-qualified and safe-access expressions.
     *
     * See [#1336](https://github.com/saveourtool/diktat/issues/1336).
     */
    val dotQualifiedExpressions = mapOf(
        false to dotQualifiedExpressionsSingleIndent,
        true to dotQualifiedExpressionsContinuationIndent)

    @Language("kotlin")
    @Suppress("COMMENT_WHITE_SPACE")
    private val ifExpressionsSingleIndent = arrayOf(
        /*-
         * #1351, case 1.
         *
         * Boolean operator priority (`&&` has higher priority than `||`).
         *
         * Currently, this is an incorrectly formatted code kept to detect the
         * contract breakage. It will be re-formatted once the issue is fixed.
         */
        """
        |fun f1() {
        |    if (valueParameterNode.parents().none { it.elementType == PRIMARY_CONSTRUCTOR } ||
        |        !valueParameterNode.hasChildOfType(VAL_KEYWORD) && 
        |            !valueParameterNode.hasChildOfType(VAR_KEYWORD)
        |    ) {
        |        return
        |    }
        |}
        """.trimMargin(),

        /*-
         * #1351, case 2.
         *
         * IDEA combines the values of `CONTINUATION_INDENT_IN_IF_CONDITIONS`
         * and `CONTINUATION_INDENT_FOR_CHAINED_CALLS`, so the resulting indent
         * can be anything between 8 (2x) and 16 (4x).
         *
         * Currently, this is an incorrectly formatted code kept to detect the
         * contract breakage. It will be re-formatted once the issue is fixed.
         */
        """
        |fun f2() {
        |    val prevComment = if (valueParameterNode.siblings(forward = false)
        |        .takeWhile { it.elementType != EOL_COMMENT && it.elementType != BLOCK_COMMENT }
        |        .all { it.elementType == WHITE_SPACE }
        |    ) {
        |        0
        |    } else {
        |        1
        |    }
        |}
        """.trimMargin(),
    )

    @Language("kotlin")
    @Suppress("COMMENT_WHITE_SPACE")
    private val ifExpressionsContinuationIndent = arrayOf(
        /*-
         * #1351, case 1.
         *
         * Boolean operator priority (`&&` has higher priority than `||`).
         *
         * Currently, this is an incorrectly formatted code kept to detect the
         * contract breakage. It will be re-formatted once the issue is fixed.
         */
        """
        |fun f1() {
        |    if (valueParameterNode.parents().none { it.elementType == PRIMARY_CONSTRUCTOR } ||
        |            !valueParameterNode.hasChildOfType(VAL_KEYWORD) && 
        |                    !valueParameterNode.hasChildOfType(VAR_KEYWORD)
        |    ) {
        |        return
        |    }
        |}
        """.trimMargin(),

        /*-
         * #1351, case 2.
         *
         * IDEA combines the values of `CONTINUATION_INDENT_IN_IF_CONDITIONS`
         * and `CONTINUATION_INDENT_FOR_CHAINED_CALLS`, so the resulting indent
         * can be anything between 8 (2x) and 16 (4x).
         *
         * Currently, this is an incorrectly formatted code kept to detect the
         * contract breakage. It will be re-formatted once the issue is fixed.
         */
        """
        |fun f2() {
        |    val prevComment = if (valueParameterNode.siblings(forward = false)
        |        .takeWhile { it.elementType != EOL_COMMENT && it.elementType != BLOCK_COMMENT }
        |        .all { it.elementType == WHITE_SPACE }
        |    ) {
        |        0
        |    } else {
        |        1
        |    }
        |}
        """.trimMargin(),
    )

    /**
     * `if`-expression examples.
     */
    val ifExpressions = mapOf(
        false to ifExpressionsSingleIndent,
        true to ifExpressionsContinuationIndent)
}
