package org.cqfn.diktat.ruleset.chapter3.spaces

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_WHITESPACE
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.WhiteSpaceRule
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Test

class WhiteSpaceRuleWarnTest {
    private val ruleId = "$DIKTAT_RULE_SET_ID:horizontal-whitespace"
    private fun keywordWarn(keyword: String, sep: String) =
            "${WRONG_WHITESPACE.warnText()} keyword '$keyword' should be separated from '$sep' with a whitespace"

    private val lbraceWarn = "${WRONG_WHITESPACE.warnText()} there should be a whitespace before '{'"
    private fun binaryOpWarn(op: String, isException: Boolean = false) =
            "${WRONG_WHITESPACE.warnText()} $op should ${if (isException) "not " else ""}be surrounded by whitespaces"

    @Test
    fun `keywords should have space before opening parenthesis and braces - positive example`() {
        lintMethod(WhiteSpaceRule(),
                """
                    |class Example : SuperExample {
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
    fun `keywords should have space before opening parenthesis`() {
        lintMethod(WhiteSpaceRule(),
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
    fun `constructor should not have space before opening parenthesis`() {
        lintMethod(WhiteSpaceRule(),
                """
                    |class Example {
                    |    constructor (val a: Int)
                    |}
                """.trimMargin(),
                LintError(2, 5, ruleId, "${WRONG_WHITESPACE.warnText()} keyword 'constructor' should not be separated from '(' with a whitespace", true)
        )
    }

    @Test
    fun `keywords should have space before opening braces`() {
        lintMethod(WhiteSpaceRule(),
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
    fun `all opening braces should have leading space`() {
        lintMethod(WhiteSpaceRule(),
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
    fun `all opening braces should have leading space - exception for lambdas as arguments`() {
        lintMethod(WhiteSpaceRule(),
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
    fun `binary operators should be surrounded by spaces - positive example`() {
        lintMethod(WhiteSpaceRule(),
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
    fun `should not false positively trigger when operators are surrounded with newlines`() {
        lintMethod(WhiteSpaceRule(),
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
    fun `binary operators should be surrounded by spaces`() {
        lintMethod(WhiteSpaceRule(),
                """
                    |class Example<T, R, Q> where T:UpperType, R: UpperType, Q :UpperType {
                    |    fun foo(t : T) = t+ 1
                    |    fun foo2(t: T) = t+1
                    |    fun foo3(t: T) = t +1
                    |    
                    |    fun bar() {
                    |        listOf<T>() .map(this ::foo).filter { elem ->predicate(elem) }
                    |        listOf<T>() . map(this :: foo).filter { elem->predicate(elem) }
                    |        listOf<T>(). map(this:: foo).filter { elem-> predicate(elem) }
                    |    }
                    |}
                """.trimMargin(),
                LintError(1, 31, ruleId, binaryOpWarn(":"), true),
                LintError(1, 44, ruleId, binaryOpWarn(":"), true),
                LintError(1, 59, ruleId, binaryOpWarn(":"), true),
                LintError(2, 23, ruleId, binaryOpWarn("+"), true),
                LintError(3, 23, ruleId, binaryOpWarn("+"), true),
                LintError(4, 24, ruleId, binaryOpWarn("+"), true),
                LintError(7, 21, ruleId, binaryOpWarn(".", true), true),
                LintError(7, 31, ruleId, binaryOpWarn("::", true), true),
                LintError(7, 52, ruleId, binaryOpWarn("->"), true),
                LintError(8, 21, ruleId, binaryOpWarn(".", true), true),
                LintError(8, 32, ruleId, binaryOpWarn("::", true), true),
                LintError(8, 53, ruleId, binaryOpWarn("->"), true),
                LintError(9, 20, ruleId, binaryOpWarn(".", true), true),
                LintError(9, 30, ruleId, binaryOpWarn("::", true), true),
                LintError(9, 51, ruleId, binaryOpWarn("->"), true)
        )
    }
}
