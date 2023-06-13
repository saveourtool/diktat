package com.saveourtool.diktat.ruleset.chapter3.spaces

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings.WRONG_WHITESPACE
import com.saveourtool.diktat.ruleset.rules.chapter3.files.WhiteSpaceRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Suppress("LargeClass")
class WhiteSpaceRuleWarnTest : LintTestBase(::WhiteSpaceRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${WhiteSpaceRule.NAME_ID}"
    private val eolSpaceWarn = "${WRONG_WHITESPACE.warnText()} there should be no spaces in the end of line"
    private val lbraceWarn = "${WRONG_WHITESPACE.warnText()} there should be a whitespace before '{'"
    private fun keywordWarn(keyword: String, sep: String) =
        "${WRONG_WHITESPACE.warnText()} keyword '$keyword' should be separated from '$sep' with a whitespace"

    private fun tokenWarn(token: String,
                          before: Int?,
                          after: Int?,
                          reqBefore: Int?,
                          reqAfter: Int?
    ) = "${WRONG_WHITESPACE.warnText()} $token should have" +
            (reqBefore?.let { " $it space(s) before" } ?: "") +
            (if (reqBefore != null && reqAfter != null) " and" else "") +
            (reqAfter?.let { " $it space(s) after" } ?: "") +
            ", but has" +
            (before?.let { " $it space(s) before" } ?: "") +
            (if (before != null && after != null) " and" else "") +
            (after?.let { " $it space(s) after" } ?: "")

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `keywords should have space before opening parenthesis and braces - positive example`() {
        lintMethod(
            """
                    |class Example {
                    |    constructor(val a: Int)
                    |
                    |    fun foo() {
                    |         if (condition) { }
                    |         else { }
                    |         for (i in 1..100) { }
                    |         when (expression) { }
                    |    }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `keywords should have space before opening parenthesis`() {
        lintMethod(
            """
                    |class Example {
                    |    fun foo() {
                    |        if(condition) { }
                    |        for  (i in 1..100) { }
                    |        when(expression) { }
                    |    }
                    |}
            """.trimMargin(),
            DiktatError(3, 11, ruleId, keywordWarn("if", "("), true),
            DiktatError(4, 14, ruleId, keywordWarn("for", "("), true),
            DiktatError(5, 13, ruleId, keywordWarn("when", "("), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `constructor should not have space before opening parenthesis`() {
        lintMethod(
            """
                    |class Example {
                    |    constructor (val a: Int)
                    |}
            """.trimMargin(),
            DiktatError(2, 5, ruleId, "${WRONG_WHITESPACE.warnText()} keyword 'constructor' should not be separated from '(' with a whitespace", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `keywords should have space before opening braces`() {
        lintMethod(
            """
                    |class Example {
                    |    fun foo() {
                    |         if (condition) { }
                    |         else{}
                    |         try{ }
                    |         finally{ }
                    |    }
                    |}
            """.trimMargin(),
            DiktatError(4, 14, ruleId, keywordWarn("else", "{"), true),
            DiktatError(5, 13, ruleId, keywordWarn("try", "{"), true),
            DiktatError(6, 17, ruleId, keywordWarn("finally", "{"), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `keywords should have space before opening braces - else without braces`() {
        lintMethod(
            """
                    |fun foo() {
                    |     if (condition)
                    |         bar()
                    |     else
                    |         baz()
                    |
                    |     if (condition) bar() else  baz()
                    |}
            """.trimMargin(),
            DiktatError(7, 33, ruleId, keywordWarn("else", "baz"), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `all opening braces should have leading space`() {
        lintMethod(
            """
                    |class Example{
                    |    fun foo(){
                    |        list.run{
                    |            map{ bar(it) }
                    |        }
                    |    }
                    |}
            """.trimMargin(),
            DiktatError(1, 14, ruleId, lbraceWarn, true),
            DiktatError(2, 14, ruleId, lbraceWarn, true),
            DiktatError(3, 17, ruleId, lbraceWarn, true),
            DiktatError(4, 16, ruleId, lbraceWarn, true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `all opening braces should have leading space - exception for lambdas as arguments`() {
        lintMethod(
            """
                    |fun foo(a: (Int) -> Int, b: Int) {
                    |    foo({ x: Int -> x }, 5)
                    |}
                    |
                    |fun bar(a: (Int) -> Int, b: Int) {
                    |    bar( { x: Int -> x }, 5)
                    |}
                    |
                    |val lambda = { x: Int -> 2 * x }
            """.trimMargin(),
            DiktatError(6, 10, ruleId, "${WRONG_WHITESPACE.warnText()} there should be no whitespace before '{' of lambda inside argument list", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `binary operators should be surrounded by spaces - positive example`() {
        lintMethod(
            """
                    |class Example<T> where T : UpperType {
                    |    fun foo(t: T) = t + 1
                    |
                    |    fun bar() {
                    |        listOf<T>().map(this::foo).filter { elem -> predicate(elem) }
                    |    }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `should not false positively trigger when operators are surrounded with newlines`() {
        lintMethod(
            """
                    |class Example<T> where T
                    |                 :
                    |                 UpperType {
                    |    fun foo(t: T) =
                    |            t + 1
                    |
                    |    fun bar() {
                    |        listOf<T>()
                    |            .map(this
                    |                ::foo)
                    |            .filter { elem ->
                    |                 predicate(elem)
                    |             }
                    |    }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `should not false positively trigger when operators are surrounded with newlines and EOL comments`() {
        lintMethod(
            """
                    |class Example<T> where T
                    |                 :  // comment about UpperType
                    |                 UpperType {
                    |    fun foo(t: T) =  // another comment
                    |            t + 1
                    |
                    |    fun bar() {
                    |        listOf<T>()
                    |            .map(this  // lorem ipsum
                    |                ::foo)
                    |            .filter { elem ->  // dolor sit amet
                    |                 predicate(elem)
                    |             }
                    |    }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    @Suppress("TOO_LONG_FUNCTION")
    fun `binary operators should be surrounded by spaces`() {
        lintMethod(
            """
                    |class Example<T, R, Q> where T:UpperType, R: UpperType, Q :UpperType {
                    |    fun foo(t: T) = t+ 1
                    |    fun foo2(t: T) = t+1
                    |    fun foo3(t: T) = t +1
                    |
                    |    fun bar() {
                    |        listOf<T>() .map(this ::foo) ?.filter { elem ->predicate(elem) } !!.first()
                    |        listOf<T>() . map(this :: foo) ?. filter { elem->predicate(elem) } !! .first()
                    |        listOf<T>(). map(this:: foo)?. filter { elem-> predicate(elem) }!!. first()
                    |    }
                    |}
            """.trimMargin(),
            DiktatError(1, 31, ruleId, tokenWarn(":", 0, 0, 1, 1), true),
            DiktatError(1, 44, ruleId, tokenWarn(":", 0, null, 1, 1), true),
            DiktatError(1, 59, ruleId, tokenWarn(":", null, 0, 1, 1), true),
            DiktatError(2, 22, ruleId, tokenWarn("+", 0, null, 1, 1), true),
            DiktatError(3, 23, ruleId, tokenWarn("+", 0, 0, 1, 1), true),
            DiktatError(4, 24, ruleId, tokenWarn("+", null, 0, 1, 1), true),
            DiktatError(7, 21, ruleId, tokenWarn(".", 1, null, 0, 0), true),
            DiktatError(7, 31, ruleId, tokenWarn("::", 1, null, 0, 0), true),
            DiktatError(7, 38, ruleId, tokenWarn("?.", 1, null, 0, 0), true),
            DiktatError(7, 54, ruleId, tokenWarn("->", null, 0, 1, 1), true),
            DiktatError(7, 74, ruleId, tokenWarn("!!", 1, null, 0, null), true),
            DiktatError(8, 21, ruleId, tokenWarn(".", 1, 1, 0, 0), true),
            DiktatError(8, 32, ruleId, tokenWarn("::", 1, 1, 0, 0), true),
            DiktatError(8, 40, ruleId, tokenWarn("?.", 1, 1, 0, 0), true),
            DiktatError(8, 56, ruleId, tokenWarn("->", 0, 0, 1, 1), true),
            DiktatError(8, 76, ruleId, tokenWarn("!!", 1, null, 0, null), true),
            DiktatError(8, 79, ruleId, tokenWarn(".", 1, null, 0, 0), true),
            DiktatError(9, 20, ruleId, tokenWarn(".", null, 1, 0, 0), true),
            DiktatError(9, 30, ruleId, tokenWarn("::", null, 1, 0, 0), true),
            DiktatError(9, 37, ruleId, tokenWarn("?.", null, 1, 0, 0), true),
            DiktatError(9, 53, ruleId, tokenWarn("->", 0, null, 1, 1), true),
            DiktatError(9, 75, ruleId, tokenWarn(".", null, 1, 0, 0), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `operators with single space after - positive example`() {
        lintMethod(
            """
                    |class Example<T> {
                    |    fun foo(t1: T, t2: T) {
                    |        println(); println()
                    |    }
                    |
                    |    fun bar(t: T,
                    |            d: T) {
                    |        println();
                    |    }
                    |
                    |    val x: Int
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `operators with single space after`() {
        lintMethod(
            """
                    |class Example<T> {${" "}
                    |    fun foo(t1 :T ,t2:T) {${" "}
                    |        println();println()
                    |        println() ; println()
                    |    }
                    |
                    |    val x : Int
                    |}
            """.trimMargin(),
            DiktatError(1, 19, ruleId, eolSpaceWarn, true),
            DiktatError(2, 16, ruleId, tokenWarn(":", 1, 0, 0, 1), true),
            DiktatError(2, 19, ruleId, tokenWarn(",", 1, 0, 0, 1), true),
            DiktatError(2, 22, ruleId, tokenWarn(":", null, 0, 0, 1), true),
            DiktatError(2, 27, ruleId, eolSpaceWarn, true),
            DiktatError(3, 18, ruleId, tokenWarn(";", null, 0, 0, 1), true),
            DiktatError(4, 19, ruleId, tokenWarn(";", 1, null, 0, 1), true),
            DiktatError(7, 11, ruleId, tokenWarn(":", 1, null, 0, 1), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `operators with single space after - exceptional cases - positive example`() {
        lintMethod(
            """
                    |abstract class Foo<out T : Any> : IFoo { }
                    |
                    |class FooImpl : Foo() {
                    |    constructor(x: String) : this(x) { /*...*/ }
                    |
                    |    val x = object : IFoo { /*...*/ }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `operators with single space after - exceptional cases`() {
        lintMethod(
            """
                    |abstract class Foo<out T: Any>: IFoo { }
                    |
                    |class FooImpl: Foo() {
                    |    constructor(x: String): this(x) { /*...*/ }
                    |
                    |    val x = object: IFoo { /*...*/ }
                    |}
            """.trimMargin(),
            DiktatError(1, 25, ruleId, tokenWarn(":", 0, null, 1, 1), true),
            DiktatError(1, 31, ruleId, tokenWarn(":", 0, null, 1, 1), true),
            DiktatError(3, 14, ruleId, tokenWarn(":", 0, null, 1, 1), true),
            DiktatError(4, 27, ruleId, tokenWarn(":", 0, null, 1, 1), true),
            DiktatError(6, 19, ruleId, tokenWarn(":", 0, null, 1, 1), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `there should be no space before question mark in nullable types`() {
        lintMethod(
            """
                    |class Example {
                    |    lateinit var x: Int?
                    |    lateinit var x: Int ?
                    |}
            """.trimMargin(),
            DiktatError(3, 25, ruleId, tokenWarn("?", 1, null, 0, null), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `there should be no space before and after square bracket`() {
        lintMethod(
            """
                    |val x = list[0]
                    |val y = list [0]
            """.trimMargin(),
            DiktatError(2, 14, ruleId, tokenWarn("[", 1, null, 0, 0), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `there should be no space between constructor or function name and opening parentheses - positive example`() {
        lintMethod(
            """
                    |class Example(val x: Int) {
                    |    constructor() : this(0)
                    |
                    |    fun foo(y: Int): AnotherExample {
                    |        bar(x)
                    |        return AnotherExample(y)
                    |    }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `there should be no space between constructor or function name and opening parentheses`() {
        lintMethod(
            """
                    |class Example (val x: Int) {
                    |    constructor() : this (0)
                    |
                    |    fun foo (y: Int): AnotherExample {
                    |        bar (x)
                    |        return AnotherExample (y)
                    |    }
                    |}
            """.trimMargin(),
            DiktatError(1, 15, ruleId, tokenWarn("(", 1, null, 0, 0), true),
            DiktatError(2, 26, ruleId, tokenWarn("(", 1, null, 0, 0), true),
            DiktatError(4, 13, ruleId, tokenWarn("(", 1, null, 0, 0), true),
            DiktatError(5, 13, ruleId, tokenWarn("(", 1, null, 0, 0), true),
            DiktatError(6, 31, ruleId, tokenWarn("(", 1, null, 0, 0), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `there should be no space before and single space after colon in function return type`() {
        lintMethod(
            """
                    |fun foo(): String = "lorem"
                    |fun bar() : String = "ipsum"
                    |fun baz() :String = "dolor"
            """.trimMargin(),
            DiktatError(2, 11, ruleId, tokenWarn(":", 1, null, 0, 1), true),
            DiktatError(3, 11, ruleId, tokenWarn(":", 1, 0, 0, 1), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `there should be no space before and after colon when use-site annotation is used`() {
        lintMethod(
            """
                    |class Example(@field:Anno val foo: Type,
                    |              @get:Anno val bar: Type,
                    |              @param:Anno val baz: Type)
                    |
                    |class Example2(@field: Anno val foo: Type,
                    |               @get :Anno val bar: Type,
                    |               @param : Anno val baz: Type)
            """.trimMargin(),
            DiktatError(5, 22, ruleId, tokenWarn(":", null, 1, 0, 0), true),
            DiktatError(6, 21, ruleId, tokenWarn(":", 1, null, 0, 0), true),
            DiktatError(7, 23, ruleId, tokenWarn(":", 1, 1, 0, 0), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `regression - comma after !!`() {
        lintMethod(
            """
                    |fun foo() {
                    |    val codeFix = CodeFix(codeForm.initialCode!! ,codeFormHtml.ruleSet[0])
                    |}
            """.trimMargin(),
            DiktatError(2, 50, ruleId, tokenWarn(",", 1, 0, 0, 1), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `space after annotation`() {
        lintMethod(
            """
                    |@Annotation ("Text")
                    |fun foo() {
                    |
                    |}
            """.trimMargin(),
            DiktatError(1, 13, ruleId, tokenWarn("(\"Text\")", 1, null, 0, null), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `check space on both sides of equals`() {
        lintMethod(
            """
                    |fun foo() {
                    |   val q=10
                    |   var w = 10
                    |   w=q
                    |}
            """.trimMargin(),
            DiktatError(2, 9, ruleId, tokenWarn("=", 0, 0, 1, 1), true),
            DiktatError(4, 5, ruleId, tokenWarn("=", 0, 0, 1, 1), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `check eq in other cases`() {
        lintMethod(
            """
                    |fun foo()=10
                    |
                    |val q =goo(text=ty)
            """.trimMargin(),
            DiktatError(1, 10, ruleId, tokenWarn("=", 0, 0, 1, 1), true),
            DiktatError(3, 7, ruleId, tokenWarn("=", null, 0, 1, 1), true),
            DiktatError(3, 16, ruleId, tokenWarn("=", 0, 0, 1, 1), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `singe space after open brace`() {
        lintMethod(
            """
                    |fun foo() {
                    |   "${"$"}{foo()}"
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `array initializers in annotations`() {
        lintMethod(
            """
                    |@RequestMapping(value =["/"], method = [RequestMethod.GET])
                    |fun foo() {
                    |   a[0]
                    |}
            """.trimMargin(),
            DiktatError(1, 23, ruleId, tokenWarn("=", null, 0, 1, 1), true),
            DiktatError(1, 24, ruleId, tokenWarn("[", 0, null, 1, 0), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `lambda as rigth value in arguments`() {
        lintMethod(
            """
                    |fun foo() {
                    |   Example(cb = { _, _ -> Unit })
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `lambdas as argument for function`() {
        lintMethod(
            """
                    |val q = foo(bar, { it.baz() })
                    |val q = foo({ it.baz() })
                    |val q = foo( { it.baz() })
            """.trimMargin(),
            DiktatError(3, 14, ruleId,
                "${WRONG_WHITESPACE.warnText()} there should be no whitespace before '{' of lambda inside argument list", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `regression - prefix coloncolon should be checked separately - positive example`() {
        lintMethod(
            """
                    |fun foo() {
                    |    Example(::ClassName)
                    |    bar(param1, ::ClassName)
                    |    bar(param1, param2 = ::ClassName)
                    |    list.map(::operationReference)
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `regression - prefix coloncolon should be checked separately`() {
        lintMethod(
            """
                    |fun foo() {
                    |    Example( :: ClassName)
                    |    bar(param1,  :: ClassName)
                    |    bar(param1, param2 = :: ClassName)
                    |    list.map(:: operationReference)
                    |}
            """.trimMargin(),
            DiktatError(2, 12, ruleId, tokenWarn("(", null, 1, 0, 0), true),
            DiktatError(2, 14, ruleId, tokenWarn("::", null, 1, null, 0), true),
            DiktatError(3, 15, ruleId, tokenWarn(",", null, 2, 0, 1), true),
            DiktatError(3, 18, ruleId, tokenWarn("::", null, 1, null, 0), true),
            DiktatError(4, 26, ruleId, tokenWarn("::", null, 1, null, 0), true),
            DiktatError(5, 14, ruleId, tokenWarn("::", null, 1, null, 0), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `regression - should correctly handle prefix and postfix operators`() {
        lintMethod(
            """
                |fun foo() {
                |    var index = 1
                |    --index
                |    return index++
                |}
                |
                |fun bar() {
                |    var index = 1
                |    -- index
                |    return index ++
                |}
            """.trimMargin(),
            DiktatError(9, 5, ruleId, tokenWarn("--", null, 1, null, 0), true),
            DiktatError(10, 18, ruleId, tokenWarn("++", 1, null, 0, null), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `check whitespaces around braces in lambda example - good`() {
        lintMethod(
            """
                |fun foo() {
                |    list.map { it.text }
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `check whitespaces around braces in lambda example - bad`() {
        lintMethod(
            """
                |fun foo() {
                |    list.map {it.text}
                |}
            """.trimMargin(),
            DiktatError(2, 14, ruleId, "${WRONG_WHITESPACE.warnText()} there should be a whitespace after {", true),
            DiktatError(2, 22, ruleId, "${WRONG_WHITESPACE.warnText()} there should be a whitespace before }", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `should not trigger on braces with empty body #1`() {
        lintMethod(
            """
                |val project = KotlinCoreEnvironment.createForProduction(
                |   Disposable {},
                |   compilerConfiguration,
                |   EnvironmentConfigFiles.JVM_CONFIG_FILES
                |).project
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `should not trigger on braces with empty body #2`() {
        lintMethod(
            """
                |val project = KotlinCoreEnvironment.createForProduction(
                |   Disposable { },
                |   compilerConfiguration,
                |   EnvironmentConfigFiles.JVM_CONFIG_FILES
                |).project
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `should not trigger in braces on the beginning of the line`() {
        lintMethod(
            """
                |val onClick: () -> Unit = remember {
                |    {
                |        /* do stuff */
                |    }
                |}
            """.trimMargin()
        )
    }
}
