package com.saveourtool.diktat.ruleset.chapter3.files

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.COMPLEX_EXPRESSION
import com.saveourtool.diktat.ruleset.constants.Warnings.WRONG_NEWLINES
import com.saveourtool.diktat.ruleset.rules.chapter3.files.NewlinesRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

@Suppress("LargeClass")
class NewlinesRuleWarnTest : LintTestBase(::NewlinesRule) {
    private val rulesConfigList: List<RulesConfig> = listOf(
        RulesConfig(WRONG_NEWLINES.name, true,
            mapOf("maxCallsInOneLine" to "3"))
    )
    private val rulesConfigListShort: List<RulesConfig> = listOf(
        RulesConfig(WRONG_NEWLINES.name, true,
            mapOf("maxCallsInOneLine" to "1"))
    )
    private val rulesConfigListLong: List<RulesConfig> = listOf(
        RulesConfig(WRONG_NEWLINES.name, true,
            mapOf("maxCallsInOneLine" to "10"))
    )
    private val ruleId = "$DIKTAT_RULE_SET_ID:${NewlinesRule.NAME_ID}"
    private val dotQuaOrSafeAccessOrPostfixExpression = "${WRONG_NEWLINES.warnText()} wrong split long `dot qualified expression` or `safe access expression`"
    private val shouldBreakAfter = "${WRONG_NEWLINES.warnText()} should break a line after and not before"
    private val shouldBreakBefore = "${WRONG_NEWLINES.warnText()} should break a line before and not after"
    private val functionalStyleWarn = "${WRONG_NEWLINES.warnText()} should follow functional style at"
    private val lparWarn = "${WRONG_NEWLINES.warnText()} opening parentheses should not be separated from constructor or function name"
    private val commaWarn = "${WRONG_NEWLINES.warnText()} newline should be placed only after comma"
    private val prevColonWarn = "${WRONG_NEWLINES.warnText()} newline shouldn't be placed before colon"
    private val lambdaWithArrowWarn = "${WRONG_NEWLINES.warnText()} in lambda with several lines in body newline should be placed after an arrow"
    private val lambdaWithoutArrowWarn = "${WRONG_NEWLINES.warnText()} in lambda with several lines in body newline should be placed after an opening brace"
    private val singleReturnWarn = "${WRONG_NEWLINES.warnText()} functions with single return statement should be simplified to expression body"

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should not false positively trigger on dots in package directive and imports`() {
        lintMethod(
            """
                    |package com.saveourtool.diktat.example
                    |
                    |import com.saveourtool.diktat.Foo
                    |import com.saveourtool.diktat.example.*
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should not false positively trigger on operators in the middle of the line`() {
        lintMethod(
            """
                    |fun foo() {
                    |    val log = LoggerFactory.getLogger(Foo::class.java)
                    |    val c = c1 && c2
                    |    obj.foo()
                    |    obj?.foo()
                    |    obj::foo
                    |    bar().let(::baz)
                    |    ::baz
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `line breaking at operators - positive example`() {
        lintMethod(
            """
                    |fun foo() {
                    |    val foo: Foo? = bar ?: Bar(javaClass.classLoader).readResource("baz")
                    |    foo?: bar
                    |
                    |    val and = condition1 &&
                    |        condition2
                    |    val plus = x +
                    |        y
                    |
                    |    obj!!
                    |
                    |    obj
                    |        .foo()
                    |    obj
                    |        ?.foo()
                    |    obj
                    |        ?: OBJ
                    |    obj
                    |        ::foo
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `line breaking at operators`() {
        lintMethod(
            """
                    |fun foo() {
                    |    val and = condition1
                    |        && condition2
                    |    // this isn't an expression
                    |    val plus = x
                    |        + y
                    |
                    |    obj.
                    |        foo()
                    |    obj?.
                    |        foo()
                    |    obj ?:
                    |        OBJ
                    |    obj::
                    |        foo
                    |}
            """.trimMargin(),
            DiktatError(3, 9, ruleId, "$shouldBreakAfter &&", true),
            DiktatError(8, 8, ruleId, "$shouldBreakBefore .", true),
            DiktatError(10, 8, ruleId, "$shouldBreakBefore ?.", true),
            DiktatError(12, 9, ruleId, "$shouldBreakBefore ?:", true),
            DiktatError(14, 8, ruleId, "$shouldBreakBefore ::", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `line breaking after infix functions - positive example`() {
        lintMethod(
            """
                    |fun foo() {
                    |    true xor
                    |        false
                    |
                    |    true
                    |        .xor(false)
                    |
                    |    (true xor
                    |        false)
                    |
                    |    true xor false or true
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `line breaking after infix functions`() {
        lintMethod(
            """
                    |fun foo() {
                    |    (true
                    |        xor false)
                    |
                    |    (true
                    |        xor
                    |        false)
                    |}
            """.trimMargin(),
            DiktatError(3, 9, ruleId, "$shouldBreakAfter xor", true),
            DiktatError(6, 9, ruleId, "$shouldBreakAfter xor", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `line breaking after infix functions - several functions in a chain`() {
        lintMethod(
            """
                    |fun foo() {
                    |    (true xor false
                    |        or true
                    |    )
                    |
                    |    (true
                    |        xor false
                    |        or true
                    |    )
                    |
                    |    (true
                    |        xor
                    |        false
                    |        or
                    |        true
                    |    )
                    |}
            """.trimMargin(),
            DiktatError(3, 9, ruleId, "$shouldBreakAfter or", true),
            DiktatError(7, 9, ruleId, "$shouldBreakAfter xor", true),
            DiktatError(8, 9, ruleId, "$shouldBreakAfter or", true),
            DiktatError(12, 9, ruleId, "$shouldBreakAfter xor", true),
            DiktatError(14, 9, ruleId, "$shouldBreakAfter or", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `chained calls should follow functional style - positive example`() {
        lintMethod(
            """
                    |fun foo(list: List<Bar>?) {
                    |    list!!
                    |        .filterNotNull()
                    |        .map { it.baz() }
                    |        .firstOrNull {
                    |            it.condition()
                    |        }
                    |        ?.qux()
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `chained calls should follow functional style - should not trigger on single dot calls`() {
        lintMethod(
            """
                    |fun foo(bar: Bar?) {
                    |    bar.baz()
                    |    bar?.baz()
                    |    bar!!.baz()
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `chained calls should follow functional style`() {
        lintMethod(
            """
                    |fun foo(list: List<Bar>?) {
                    |    list!!.filterNotNull()
                    |        .map { it.baz() }.firstOrNull {
                    |            it.condition()
                    |        }?.qux()
                    |}
            """.trimMargin(),
            DiktatError(2, 5, ruleId, dotQuaOrSafeAccessOrPostfixExpression, true),
            DiktatError(2, 11, ruleId, "$functionalStyleWarn .", true),
            DiktatError(3, 26, ruleId, "$functionalStyleWarn .", true),
            DiktatError(5, 10, ruleId, "$functionalStyleWarn ?.", true),
            rulesConfigList = rulesConfigListShort
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `chained calls should follow functional style - exception for ternary if-else`() {
        lintMethod(
            """
                    |fun foo(list: List<Bar>?) {
                    |    if (list.size > n) list.filterNotNull().map { it.baz() } else list.let { it.bar() }.firstOrNull()?.qux()
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `newline should be placed only after assignment operator`() {
        lintMethod(
            """
                    |class Example {
                    |    val a =
                    |        42
                    |    val b
                    |        = 43
                    |}
            """.trimMargin(),
            DiktatError(5, 9, ruleId, "$shouldBreakAfter =", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `newline should be placed only after comma`() {
        lintMethod(
            """
                    |fun foo(a: Int
                    |        ,
                    |        b: Int) {
                    |    bar(a
                    |        , b)
                    |}
            """.trimMargin(),
            DiktatError(2, 9, ruleId, commaWarn, true),
            DiktatError(5, 9, ruleId, commaWarn, true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `newline shouldn't be placed before colon`() {
        lintMethod(
            """
                    |fun foo(a
                    |        : Int,
                    |        b
                    |        : Int) {
                    |    bar(a, b)
                    |}
            """.trimMargin(),
            DiktatError(2, 9, ruleId, prevColonWarn, true),
            DiktatError(4, 9, ruleId, prevColonWarn, true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `One line parameters list sheet must contain no more than 2 parameters`() {
        lintMethod(
            """
                    |fun foo(a: Int, b: Int, c: Int) {
                    |      bar(a, b)
                    |}
            """.trimMargin(),
            DiktatError(1, 8, ruleId, "${WRONG_NEWLINES.warnText()} first parameter should be placed on a separate line or all other parameters " +
                    "should be aligned with it in declaration of <foo>", true),
            DiktatError(1, 8, ruleId, "${WRONG_NEWLINES.warnText()} value parameters should be placed on different lines in declaration of <foo>", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `newline after colon`() {
        lintMethod(
            """
                    |fun foo(a:
                    |      Int,
                    |      b:
                    |      Int) {
                    |      bar(a, b)
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `list parameter should be placed on different lines`() {
        lintMethod(
            """
                    |fun foo(
                    |      a: Int,
                    |      b: Int,
                    |      c: Int
                    |      ) {
                    |}
            """.trimMargin(),
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `function name should not be separated from ( - positive example`() {
        lintMethod(
            """
                    |val foo = Foo(arg1, arg2)
                    |class Example(
                    |    val x: Int
                    |) {
                    |    fun foo(
                    |        a: Int
                    |    ) { }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `function name should not be separated from ( - should not trigger on other parenthesis`() {
        lintMethod(
            """
                    |val x = (2 + 2) * 2
                    |val y = if (condition) 2 else 1
                    |fun foo(f: (Int) -> Pair<Int, Int>) {
                    |    val (a, b) = f(0)
                    |}
                    |fun bar(f: (x: Int) -> Unit) { }
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `function name should not be separated from (`() {
        lintMethod(
            """
                    |val foo = Foo  (arg1, arg2)
                    |class Example
                    |    (
                    |        val x: Int
                    |    ) {
                    |    fun foo
                    |    (
                    |        a: Int
                    |    ) { }
                    |}
            """.trimMargin(),
            DiktatError(1, 16, ruleId, lparWarn, true),
            DiktatError(3, 5, ruleId, lparWarn, true),
            DiktatError(7, 5, ruleId, lparWarn, true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `newline should be placed only after comma - positive example`() {
        lintMethod(
            """
                    |fun foo(a: Int,
                    |        b: Int) {
                    |    bar(a, b)
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `in lambdas newline should be placed after an arrow - positive example`() {
        lintMethod(
            """
                    |class Example {
                    |    val a = list.map { elem ->
                    |        foo(elem)
                    |    }
                    |    val b = list.map { elem: Type ->
                    |        foo(elem)
                    |    }
                    |    val c = list.map {
                    |        bar(elem)
                    |    }
                    |    val d = list.map { elem -> bar(elem) }
                    |    val e = list.map { elem: Type -> bar(elem) }
                    |    val f = list.map { bar(elem) }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `in lambdas newline should be placed after an arrow`() {
        lintMethod(
            """
                    |class Example {
                    |    val a = list.map {
                    |        elem ->
                    |            foo(elem)
                    |    }
                    |    val b = list.map { elem: Type
                    |        ->
                    |            foo(elem)
                    |    }
                    |    val c = list.map { elem
                    |        -> bar(elem)
                    |    }
                    |    val d = list.map { elem: Type -> bar(elem)
                    |        foo(elem)
                    |    }
                    |    val e = list.map { bar(elem)
                    |        foo(elem)
                    |    }
                    |}
            """.trimMargin(),
            DiktatError(3, 14, ruleId, lambdaWithArrowWarn, true),
            DiktatError(7, 9, ruleId, lambdaWithArrowWarn, true),
            DiktatError(11, 9, ruleId, lambdaWithArrowWarn, true),
            DiktatError(13, 35, ruleId, lambdaWithArrowWarn, true),
            DiktatError(16, 22, ruleId, lambdaWithoutArrowWarn, true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should warn if function consists of a single return statement - positive example`() {
        lintMethod(
            """
                    |fun foo() = "lorem ipsum"
                    |
                    |fun bar(): String {
                    |    baz()
                    |    return "lorem ipsum"
                    |}
                    |
                    |fun qux(list: List<Int>): Int {
                    |    list.filter {
                    |        return@filter condition(it)
                    |    }.forEach {
                    |        return 0
                    |    }
                    |    return list.first()
                    |}
                    |
                    |fun quux() { return }
                    |
                    |fun quux2(): Unit { return }
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should warn for array access expression`() {
        lintMethod(
            """
                    |fun bar(): String {
                    |    val a = list.responseBody!![0].
                    |    name
                    |}
            """.trimMargin(),
            DiktatError(2, 35, ruleId, "$shouldBreakBefore .", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `long argument list should be split into several lines - positive example`() {
        lintMethod(
            """
                    |class SmallExample(val a: Int)
                    |
                    |class Example(val a: Int,
                    |              val b: Int) {
                    |    fun foo(a: Int) { }
                    |
                    |    fun bar(
                    |            a: Int,
                    |            b: Int
                    |    ) { }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    @Disabled("Will be implemented later")
    fun `long argument list should be split into several lines`() {
        lintMethod(
            """
                    |class SmallExample(val a: Int)
                    |
                    |class Example(val a: Int, val b: Int) {
                    |    fun foo(a: Int) { }
                    |
                    |    fun bar(
                    |            a: Int, b: Int
                    |    ) { }
                    |}
            """.trimMargin(),
            DiktatError(3, 14, ruleId, "${WRONG_NEWLINES.warnText()} argument list should be split into several lines", true),
            DiktatError(7, 12, ruleId, "${WRONG_NEWLINES.warnText()} argument list should be split into several lines", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should warn if function consists of a single return statement`() {
        lintMethod(
            """
                    |fun foo(): String {
                    |    return "lorem ipsum"
                    |}
            """.trimMargin(),
            DiktatError(2, 5, ruleId, singleReturnWarn, true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `shouldn't warn if function consists of a single return statement with a nested return`() {
        lintMethod(
            """
                    |fun foo(): String {
                    |    return Demo(string ?: return null)
                    |}
            """.trimMargin(),
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    @Suppress("TOO_LONG_FUNCTION")
    fun `should not trigger`() {
        lintMethod(
            """
                    |fun foo(): String {
                    |
                    |       val a = java.lang.Boolean.getBoolean(properties.getProperty("parallel.mode"))
                    |
                    |        allProperties?.filter {
                    |           predicate(it)
                    |           val x = listOf(1,2,3).filter { it < 3 }
                    |           x == 0
                    |        }
                    |        .foo()
                    |        .bar()
                    |
                    |        allProperties?.filter {
                    |           predicate(it)
                    |        }
                    |        .foo()
                    |        .bar()
                    |        .let {
                    |           it.some()
                    |        }
                    |
                    |        allProperties
                    |        ?.filter {
                    |           predicate(it)
                    |        }
                    |        .foo()
                    |        .bar()
                    |        .let {
                    |           mutableListOf().also {
                    |
                    |           }
                    |        }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should trigger for several lambdas on same line`() {
        lintMethod(
            """
                    |fun foo(): String {
                    |        allProperties.filter { predicate(it) }
                    |        .foo()
                    |        .bar()
                    |
                    |        allProperties?.filter { predicate(it) }
                    |        .foo()
                    |        .bar()
                    |
                    |        list.foo()
                    |           .bar()
                    |           .filter {
                    |               baz()
                    |           }
                    |
                    |        list.filter {
                    |
                    |        }
                    |        .map(::foo).filter {
                    |           bar()
                    |         }
                    |}
            """.trimMargin(),
            DiktatError(16, 9, ruleId, dotQuaOrSafeAccessOrPostfixExpression, true),
            DiktatError(19, 20, ruleId, "$functionalStyleWarn .", true),
            rulesConfigList = rulesConfigListShort
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should suggest newlines in a long argument list of a constructor`() {
        lintMethod(
            """
                |class Foo(val arg1: Int, arg2: Int) { }
                |
                |class Foo(val arg1: Int, arg2: Int, arg3: Int) {
                |    constructor(arg1: Int, arg2: String, arg3: String) : this(arg1, 0, 0) { }
                |}
                |
                |class Foo(val arg1: Int,
                |          var arg2: Int,
                |          arg3: Int
                |) { }
            """.trimMargin(),
            DiktatError(3, 10, ruleId, "${WRONG_NEWLINES.warnText()} first parameter should be placed on a separate line or all other parameters " +
                    "should be aligned with it in declaration of <Foo>", true),
            DiktatError(3, 10, ruleId, "${WRONG_NEWLINES.warnText()} value parameters should be placed on different lines in declaration of <Foo>", true),
            DiktatError(4, 16, ruleId, "${WRONG_NEWLINES.warnText()} first parameter should be placed on a separate line or all other parameters " +
                    "should be aligned with it in declaration of <Foo>", true),
            DiktatError(4, 16, ruleId, "${WRONG_NEWLINES.warnText()} value parameters should be placed on different lines in declaration of <Foo>", true),
            DiktatError(4, 62, ruleId, "${WRONG_NEWLINES.warnText()} first value argument (arg1) should be placed on the new line or " +
                    "all other parameters should be aligned with it", true),
            DiktatError(4, 62, ruleId, "${WRONG_NEWLINES.warnText()} value arguments should be placed on different lines", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should suggest newlines in a long argument list`() {
        lintMethod(
            """
                |fun foo(arg1: Int, arg2: Int) { }
                |
                |fun bar(arg1: Int, arg2: Int, arg3: Int) { }
                |
                |// should not trigger on functional types
                |fun bar(arg1: (_arg1: Int, _arg2: Int, _arg3: Int) -> Int) { }
                |
                |// should not trigger on functional type receivers
                |fun bar(arg1: Foo.(_arg1: Int, _arg2: Int, _arg3: Int) -> Int) { }
                |
                |fun baz(arg1: Int,
                |        arg2: Int,
                |        arg3: Int
                |) { }
            """.trimMargin(),
            DiktatError(3, 8, ruleId, "${WRONG_NEWLINES.warnText()} first parameter should be placed on a separate line or all other parameters " +
                    "should be aligned with it in declaration of <bar>", true),
            DiktatError(3, 8, ruleId, "${WRONG_NEWLINES.warnText()} value parameters should be placed on different lines in declaration of <bar>", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should not raise warning on value arguments`() {
        lintMethod(
            """
                |class SomeRule(configRules: List<Int>) : Rule("id",
                |configRules,
                |listOf("foo", "baz")
                |) {
                |
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should not raise warning on list params`() {
        lintMethod(
            """
                |class SomeRule(configRules: List<Int>) : Rule("id",
                |configRules,
                |listOf("foo", "baz", "triple", "bar")
                |) {
                |
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should raise warning on value arguments`() {
        lintMethod(
            """
                |class SomeRule(configRules: List<Int>) : Rule("id", configRules, listOf("foo", "baz")) {
                |
                |}
            """.trimMargin(),
            DiktatError(1, 46, ruleId, "${WRONG_NEWLINES.warnText()} first value argument (\"id\") should be placed on the new line or " +
                    "all other parameters should be aligned with it", true),
            DiktatError(1, 46, ruleId, "${WRONG_NEWLINES.warnText()} value arguments should be placed on different lines", true),
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should suggest newlines in a long supertype list`() {
        lintMethod(
            """
                |class Foo :
                |    FooBase<Bar>(),
                |    BazInterface,
                |    BazSuperclass { }
                |
                |class Foo : FooBase<Bar>(), BazInterface,
                |    BazSuperclass { }
                |
                |class Foo : FooBase<Bar>(), BazInterface, BazSuperclass { }
                |
                |class Foo : FooBase<Bar>() { }
            """.trimMargin(),
            DiktatError(6, 13, ruleId, "${WRONG_NEWLINES.warnText()} supertype list entries should be placed on different lines in declaration of <Foo>", true),
            DiktatError(9, 13, ruleId, "${WRONG_NEWLINES.warnText()} supertype list entries should be placed on different lines in declaration of <Foo>", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should warn dot qualified with first on same line`() {
        lintMethod(
            """
                |fun foo() {
                |   x.map()
                |   .gre().few().dfh().qwe()
                |}
                |
                |fun foo() {
                |   x.map()
                |   .gre()
                |   .few()
                |}
                |
                |fun foo() {
                |   x.map().gre().few().qwe()
                |}
            """.trimMargin(),
            DiktatError(2, 4, ruleId, dotQuaOrSafeAccessOrPostfixExpression, true),
            DiktatError(3, 22, ruleId, "$functionalStyleWarn .", true),
            DiktatError(13, 4, ruleId, dotQuaOrSafeAccessOrPostfixExpression, true),
            DiktatError(13, 23, ruleId, "$functionalStyleWarn .", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should warn dot qualified with first on diff line`() {
        lintMethod(
            """
                |fun foo() {
                |   x
                |   .map()
                |   .gre().few().qwe().qwe()
                |}
                |
                |fun foo() {
                |   x
                |   .map().gre().few().vfd()
                |}
                |
                |fun foo() {
                |   x
                |   .map()
                |   .gre()
                |   .few()
                |}
            """.trimMargin(),
            DiktatError(2, 4, ruleId, dotQuaOrSafeAccessOrPostfixExpression, true),
            DiktatError(4, 22, ruleId, "$functionalStyleWarn .", true),
            DiktatError(8, 4, ruleId, dotQuaOrSafeAccessOrPostfixExpression, true),
            DiktatError(9, 22, ruleId, "$functionalStyleWarn .", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should warn dot qualified with safe access`() {
        lintMethod(
            """
                |fun foo() {
                |   x
                |   ?.map()
                |   .gre().few()
                |}
                |
                |fun foo() {
                |   x
                |   ?.map().gre().few()
                |}
                |fun foo() {
                |   x
                |   ?.map()
                |   .gre()
                |   .few()
                |}
                |
                |fun foo() {
                |   x?.map()
                |   .gre()
                |   .few()
                |}
            """.trimMargin(),
            DiktatError(2, 4, ruleId, dotQuaOrSafeAccessOrPostfixExpression, true),
            DiktatError(4, 10, ruleId, "$functionalStyleWarn .", true),
            DiktatError(8, 4, ruleId, dotQuaOrSafeAccessOrPostfixExpression, true),
            DiktatError(9, 11, ruleId, "$functionalStyleWarn .", true),
            DiktatError(9, 17, ruleId, "$functionalStyleWarn .", true),
            rulesConfigList = rulesConfigListShort
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should warn dot qualified with exclexcl`() {
        lintMethod(
            """
                |fun foo() {
                |   x!!.map()
                |   .gre()
                |   .few()
                |}
                |
                |fun foo() {
                |   x!!
                |   .map()
                |   .gre()
                |   .few()
                |}
                |
                |fun foo() {
                |   x!!
                |   .map().gre()
                |   .few()
                |}
                |
                |fun foo() {
                |   x!!.map()
                |   .gre().few()
                |}
            """.trimMargin(),
            DiktatError(2, 7, ruleId, "$functionalStyleWarn .", true),
            DiktatError(15, 4, ruleId, dotQuaOrSafeAccessOrPostfixExpression, true),
            DiktatError(16, 10, ruleId, "$functionalStyleWarn .", true),
            DiktatError(21, 4, ruleId, dotQuaOrSafeAccessOrPostfixExpression, true),
            DiktatError(21, 7, ruleId, "$functionalStyleWarn .", true),
            DiktatError(22, 10, ruleId, "$functionalStyleWarn .", true),
            rulesConfigList = rulesConfigListShort
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should warn elvis`() {
        lintMethod(
            """
                |fun foo() {
                |
                |   z.goo()
                |       ?:
                |        goo()
                |
                |   x.goo()
                |       ?:goo()
                |
                |   y.ds()?:gh()
                |}
            """.trimMargin(),
            DiktatError(4, 8, ruleId, "$shouldBreakBefore ?:", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `should warn elvis with several dot qualifided`() {
        lintMethod(
            """
                |fun foo() {
                |   z.goo()
                |       ?:
                |        goo().gor().goo()
                |}
            """.trimMargin(),
            DiktatError(3, 8, ruleId, "$shouldBreakBefore ?:", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `test configuration for calls in one line`() {
        lintMethod(
            """
                |fun foo() {
                |   z.goo().foo().qwe()
                |   z!!.htr().foo()
                |   x.goo().foo().goo()
                |   x.gf().gfh() ?: true
                |   x.gf().fge().qwe().fd()
                |}
            """.trimMargin(),
            DiktatError(6, 4, ruleId, dotQuaOrSafeAccessOrPostfixExpression, true),
            DiktatError(6, 22, ruleId, "$functionalStyleWarn .", true), rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `more test for prefix`() {
        lintMethod(
            """
                |fun foo() {
                |   foo
                |       .bar()
                |       .goo()
                |       .qwe()!!
                |
                |   goo()!!.gre()
                |
                |   bfr()!!.qwe().foo().qwe().dg()
                |}
                |
                |fun foo() {
                |   foo
                |       .bar()
                |       .goo()!!
                |       .qwe()
                |}
            """.trimMargin(),
            DiktatError(9, 4, ruleId, dotQuaOrSafeAccessOrPostfixExpression, true),
            DiktatError(9, 29, ruleId, "$functionalStyleWarn .", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `more test for unsafe calls`() {
        lintMethod(
            """
                |fun foo() {
                |   foo
                |       .bar()
                |       .goo()!!
                |       .qwe()
                |
                |   val x = foo
                |       .bar!!
                |       .baz
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `test for null safety one line`() {
        lintMethod(
            """
                |fun foo() {
                |   foo.qwe() ?: bar.baz()
                |   foo ?: bar().qwe()
                |   foo ?: bar().qwe().qwe()
                |}
            """.trimMargin(),
            DiktatError(2, 14, ruleId, "$functionalStyleWarn ?:", true),
            DiktatError(4, 8, ruleId, "$functionalStyleWarn ?:", true),
            DiktatError(4, 11, ruleId, dotQuaOrSafeAccessOrPostfixExpression, true),
            DiktatError(4, 22, ruleId, "$functionalStyleWarn .", true),
            rulesConfigList = rulesConfigListShort
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `test for not first prefix`() {
        lintMethod(
            """
                |fun foo() {
                |   a().b!!.c()
                |   a().b.c()!!
                |}
            """.trimMargin(),
            DiktatError(2, 4, ruleId, dotQuaOrSafeAccessOrPostfixExpression, true),
            DiktatError(2, 11, ruleId, "$functionalStyleWarn .", true),
            DiktatError(3, 4, ruleId, dotQuaOrSafeAccessOrPostfixExpression, true),
            DiktatError(3, 9, ruleId, "$functionalStyleWarn .", true),
            rulesConfigList = rulesConfigListShort
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `test for null safety several lines`() {
        lintMethod(
            """
                |fun foo() {
                |   foo.qwe()
                |       ?: bar.baz()
                |   foo
                |       ?: bar().qwe()
                |   foo
                |       ?: bar().qwe().qwe()
                |   foo
                |       .qwe() ?: qwe().qwe()
                |   foo().qwe() ?: qwe().
                |                           qwe()
                |   foo().qwe() ?: qwe()
                |                       .qwe()
                |}
            """.trimMargin(),
            DiktatError(7, 11, ruleId, dotQuaOrSafeAccessOrPostfixExpression, true),
            DiktatError(7, 22, ruleId, "$functionalStyleWarn .", true),
            DiktatError(9, 15, ruleId, "$functionalStyleWarn ?:", true),
            DiktatError(10, 16, ruleId, "$functionalStyleWarn ?:", true),
            DiktatError(10, 24, ruleId, "$shouldBreakBefore .", true),
            DiktatError(12, 16, ruleId, "$functionalStyleWarn ?:", true),
            rulesConfigList = rulesConfigListShort
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `test for null safety correct examples`() {
        lintMethod(
            """
                |fun foo() {
                |   foo
                |       ?: bar
                |           .bar()
                |           .qux()
                |
                |
                |   foo.bar()
                |       .baz()
                |       .qux()
                |       ?: boo
                |}
            """.trimMargin(),
            rulesConfigList = rulesConfigListShort
        )
    }

    @Test
    @Tags(Tag(WarningNames.WRONG_NEWLINES), Tag(WarningNames.COMPLEX_EXPRESSION))
    fun `complex expression in condition`() {
        lintMethod(
            """
                |fun foo() {
                |   if(a.b.c) {}
                |   while(a?.b?.c) {}
                |   when(a.b!!.c) {}
                |   goo(a?.b.c)
                |}
            """.trimMargin(),
            DiktatError(2, 10, ruleId, "${COMPLEX_EXPRESSION.warnText()} .", false),
            DiktatError(3, 14, ruleId, "${COMPLEX_EXPRESSION.warnText()} ?.", false),
            DiktatError(4, 14, ruleId, "${COMPLEX_EXPRESSION.warnText()} .", false),
            DiktatError(5, 12, ruleId, "${COMPLEX_EXPRESSION.warnText()} .", false),
            rulesConfigList = rulesConfigListShort
        )
    }

    @Test
    @Tags(Tag(WarningNames.WRONG_NEWLINES), Tag(WarningNames.COMPLEX_EXPRESSION))
    fun `complex expression in condition with double warnings`() {
        lintMethod(
            """
                |fun foo() {
                |   if(a().b().c()) {}
                |}
            """.trimMargin(),
            DiktatError(2, 7, ruleId, dotQuaOrSafeAccessOrPostfixExpression, true),
            DiktatError(2, 14, ruleId, "${COMPLEX_EXPRESSION.warnText()} .", false),
            DiktatError(2, 14, ruleId, "$functionalStyleWarn .", true),
            rulesConfigList = rulesConfigListShort
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `shouldn't fall with NPE`() {
        lintMethod(
            """
                |val TEST_FUNC = { _: Int, _: Set<Int>, _: Int, ->
                |}
            """.trimMargin(),
            DiktatError(1, 19, ruleId, "${WRONG_NEWLINES.warnText()} " +
                    "first parameter should be placed on a separate line or all other parameters should be aligned with it in declaration of <null>", true),
            DiktatError(1, 19, ruleId, "${WRONG_NEWLINES.warnText()} value parameters should be placed on different lines", true),
            )
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES)
    fun `not complaining on fun without return type`() {
        lintMethod(
            """
                |fun foo(x: Int, y: Int) = x + y
            """.trimMargin(),
        )
    }

    @Test
    @Tag(WarningNames.COMPLEX_EXPRESSION)
    fun `COMPLEX_EXPRESSION shouldn't trigger for declarations in kts`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                |dependencies {
                |    implementation(libs.spring.cloud.starter.gateway)
                |}
            """.trimMargin(),
            tempDir = tempDir,
            fileName = "build.gradle.kts"
        )
    }

    @Test
    @Tags(Tag(WarningNames.WRONG_NEWLINES), Tag(WarningNames.COMPLEX_EXPRESSION))
    fun `shouldn't trigger on allowed many calls in one line`() {
        lintMethod(
            """
                |fun foo() {
                |   if(a().b().c().d().e()) {}
                |}
            """.trimMargin(),
            rulesConfigList = rulesConfigListLong
        )
    }
}
