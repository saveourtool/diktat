package org.cqfn.diktat.ruleset.chapter3.spaces

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_WHITESPACE
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.WhiteSpaceRule
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class WhiteSpaceRuleWarnTest : LintTestBase(::WhiteSpaceRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:horizontal-whitespace"
    private fun keywordWarn(keyword: String, sep: String) =
            "${WRONG_WHITESPACE.warnText()} keyword '$keyword' should be separated from '$sep' with a whitespace"

    private val lbraceWarn = "${WRONG_WHITESPACE.warnText()} there should be a whitespace before '{'"
    private val eolSpaceWarn = "${WRONG_WHITESPACE.warnText()} there should be no spaces in the end of line"
    private fun tokenWarn(token: String, before: Int?, after: Int?, reqBefore: Int?, reqAfter: Int?) =
            "${WRONG_WHITESPACE.warnText()} $token should have" +
                    (if (reqBefore != null) " $reqBefore space(s) before" else "") +
                    (if (reqBefore != null && reqAfter != null) " and" else "") +
                    (if (reqAfter != null) " $reqAfter space(s) after" else "") +
                    ", but has" +
                    (if (before != null) " $before space(s) before" else "") +
                    (if (before != null && after != null) " and" else "") +
                    (if (after != null) " $after space(s) after" else "")

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
                LintError(3, 11, ruleId, keywordWarn("if", "("), true),
                LintError(4, 14, ruleId, keywordWarn("for", "("), true),
                LintError(5, 13, ruleId, keywordWarn("when", "("), true)
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
                LintError(2, 5, ruleId, "${WRONG_WHITESPACE.warnText()} keyword 'constructor' should not be separated from '(' with a whitespace", true)
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
                LintError(4, 14, ruleId, keywordWarn("else", "{"), true),
                LintError(5, 13, ruleId, keywordWarn("try", "{"), true),
                LintError(6, 17, ruleId, keywordWarn("finally", "{"), true)
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
                LintError(7, 33, ruleId, keywordWarn("else", "baz"), true)
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
                LintError(1, 14, ruleId, lbraceWarn, true),
                LintError(2, 14, ruleId, lbraceWarn, true),
                LintError(3, 17, ruleId, lbraceWarn, true),
                LintError(4, 16, ruleId, lbraceWarn, true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `all opening braces should have leading space - exception for lambdas as arguments`() {
        lintMethod(
                """
                    |fun foo(a: (Int) -> Int, b: Int) {
                    |    foo({x: Int -> x}, 5)
                    |}
                    |
                    |fun bar(a: (Int) -> Int, b: Int) {
                    |    bar( {x: Int -> x}, 5)
                    |}
                    |
                    |val lambda = { x: Int -> 2 * x }
                """.trimMargin(),
                LintError(6, 10, ruleId, "${WRONG_WHITESPACE.warnText()} there should be no whitespace before '{' of lambda inside argument list", true)
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
                LintError(1, 31, ruleId, tokenWarn(":", 0, 0, 1, 1), true),
                LintError(1, 44, ruleId, tokenWarn(":", 0, null, 1, 1), true),
                LintError(1, 59, ruleId, tokenWarn(":", null, 0, 1, 1), true),
                LintError(2, 22, ruleId, tokenWarn("+", 0, null, 1, 1), true),
                LintError(3, 23, ruleId, tokenWarn("+", 0, 0, 1, 1), true),
                LintError(4, 24, ruleId, tokenWarn("+", null, 0, 1, 1), true),
                LintError(7, 21, ruleId, tokenWarn(".", 1, null, 0, 0), true),
                LintError(7, 31, ruleId, tokenWarn("::", 1, null, 0, 0), true),
                LintError(7, 38, ruleId, tokenWarn("?.", 1, null, 0, 0), true),
                LintError(7, 54, ruleId, tokenWarn("->", null, 0, 1, 1), true),
                LintError(7, 74, ruleId, tokenWarn("!!", 1, null, 0, 0), true),
                LintError(8, 21, ruleId, tokenWarn(".", 1, 1, 0, 0), true),
                LintError(8, 32, ruleId, tokenWarn("::", 1, 1, 0, 0), true),
                LintError(8, 40, ruleId, tokenWarn("?.", 1, 1, 0, 0), true),
                LintError(8, 56, ruleId, tokenWarn("->", 0, 0, 1, 1), true),
                LintError(8, 76, ruleId, tokenWarn("!!", 1, 1, 0, 0), true),
                LintError(8, 79, ruleId, tokenWarn(".", 1, null, 0, 0), true),
                LintError(9, 20, ruleId, tokenWarn(".", null, 1, 0, 0), true),
                LintError(9, 30, ruleId, tokenWarn("::", null, 1, 0, 0), true),
                LintError(9, 37, ruleId, tokenWarn("?.", null, 1, 0, 0), true),
                LintError(9, 53, ruleId, tokenWarn("->", 0, null, 1, 1), true),
                LintError(9, 75, ruleId, tokenWarn(".", null, 1, 0, 0), true)
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
                LintError(1, 19, ruleId, eolSpaceWarn, true),
                LintError(2, 16, ruleId, tokenWarn(":", 1, 0, 0, 1), true),
                LintError(2, 19, ruleId, tokenWarn(",", 1, 0, 0, 1), true),
                LintError(2, 22, ruleId, tokenWarn(":", null, 0, 0, 1), true),
                LintError(2, 27, ruleId, eolSpaceWarn, true),
                LintError(3, 18, ruleId, tokenWarn(";", null, 0, 0, 1), true),
                LintError(4, 19, ruleId, tokenWarn(";", 1, null, 0, 1), true),
                LintError(7, 11, ruleId, tokenWarn(":", 1, null, 0, 1), true)
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
                LintError(1, 25, ruleId, tokenWarn(":", 0, null, 1, 1), true),
                LintError(1, 31, ruleId, tokenWarn(":", 0, null, 1, 1), true),
                LintError(3, 14, ruleId, tokenWarn(":", 0, null, 1, 1), true),
                LintError(4, 27, ruleId, tokenWarn(":", 0, null, 1, 1), true),
                LintError(6, 19, ruleId, tokenWarn(":", 0, null, 1, 1), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `there should be no space before ? in nullable types`() {
        lintMethod(
                """
                    |class Example {
                    |    lateinit var x: Int?
                    |    lateinit var x: Int ?
                    |}
                """.trimMargin(),
                LintError(3, 25, ruleId, tokenWarn("?", 1, null, 0, null), true)
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
                LintError(2, 14, ruleId, tokenWarn("[", 1, null, 0, 0), true)
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
                LintError(1, 15, ruleId, tokenWarn("(", 1, null, 0, 0), true),
                LintError(2, 26, ruleId, tokenWarn("(", 1, null, 0, 0), true),
                LintError(4, 13, ruleId, tokenWarn("(", 1, null, 0, 0), true),
                LintError(5, 13, ruleId, tokenWarn("(", 1, null, 0, 0), true),
                LintError(6, 31, ruleId, tokenWarn("(", 1, null, 0, 0), true)
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
                LintError(2, 11, ruleId, tokenWarn(":", 1, null, 0, 1), true),
                LintError(3, 11, ruleId, tokenWarn(":", 1, 0, 0, 1), true)
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
                LintError(5, 22, ruleId, tokenWarn(":", null, 1, 0, 0), true),
                LintError(6, 21, ruleId, tokenWarn(":", 1, null, 0, 0), true),
                LintError(7, 23, ruleId, tokenWarn(":", 1, 1, 0, 0), true)
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
                LintError(2, 50, ruleId, tokenWarn(",", 1, 0, 0, 1), true)
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
                LintError(1, 13, ruleId, tokenWarn("(\"Text\")", 1, null, 0, null), true)
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
                LintError(2, 9, ruleId, tokenWarn("=", 0, 0, 1, 1), true),
                LintError(4, 5, ruleId, tokenWarn("=", 0, 0, 1, 1), true)
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
                LintError(1, 10, ruleId, tokenWarn("=", 0, 0, 1, 1), true),
                LintError(3, 7, ruleId, tokenWarn("=", null, 0, 1, 1), true),
                LintError(3, 16, ruleId, tokenWarn("=", 0, 0, 1, 1), true)
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
                LintError(1, 23, ruleId, tokenWarn("=", null, 0, 1, 1), true),
                LintError(1, 24, ruleId, tokenWarn("[", 0, null, 1, 0), true)
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
                LintError(3, 14, ruleId,
                        "${WRONG_WHITESPACE.warnText()} there should be no whitespace before '{' of lambda inside argument list", true)
        )
    }
}
