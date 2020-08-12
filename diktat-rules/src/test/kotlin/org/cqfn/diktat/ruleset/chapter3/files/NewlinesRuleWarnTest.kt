package org.cqfn.diktat.ruleset.chapter3.files

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.constants.Warnings.REDUNDANT_SEMICOLON
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_NEWLINES
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.files.NewlinesRule
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class NewlinesRuleWarnTest {
    private val ruleId = "$DIKTAT_RULE_SET_ID:newlines"
    private val shouldBreakAfter = "${WRONG_NEWLINES.warnText()} should break a line after and not before"
    private val shouldBreakBefore = "${WRONG_NEWLINES.warnText()} should break a line before and not after"
    private val functionalStyleWarn = "${WRONG_NEWLINES.warnText()} should follow functional style at"
    private val lParWarn = "${WRONG_NEWLINES.warnText()} opening parentheses should not be separated from constructor or function name"
    private val commaWarn = "${WRONG_NEWLINES.warnText()} newline should be placed only after comma"

    @Test
    fun `should forbid EOL semicolons`() {
        lintMethod(NewlinesRule(),
                """
                    |enum class Example {
                    |    A,
                    |    B
                    |    ;
                    |    
                    |    fun foo() {};
                    |    val a = 0;
                    |    val b = if (condition) { bar(); baz()} else qux
                    |};
                """.trimMargin(),
                LintError(6, 17, ruleId, "${REDUNDANT_SEMICOLON.warnText()} fun foo() {};", true),
                LintError(7, 14, ruleId, "${REDUNDANT_SEMICOLON.warnText()} val a = 0;", true),
                LintError(9, 2, ruleId, "${REDUNDANT_SEMICOLON.warnText()} };", true)
        )
    }

    @Test
    fun `should not false positively trigger on dots in package directive and imports`() {
        lintMethod(NewlinesRule(),
                """
                    |package org.cqfn.diktat.example
                    |
                    |import org.cqfn.diktat.Foo
                    |import org.cqfn.diktat.example.*
                """.trimMargin()
        )
    }

    @Test
    fun `should not false positively trigger on operators in the middle of the line`() {
        lintMethod(NewlinesRule(),
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
    fun `line breaking at operators - positive example`() {
        lintMethod(NewlinesRule(),
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
    fun `line breaking at operators`() {
        lintMethod(NewlinesRule(),
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
                LintError(3, 9, ruleId, "$shouldBreakAfter &&", true),
                LintError(8, 8, ruleId, "$shouldBreakBefore .", true),
                LintError(10, 8, ruleId, "$shouldBreakBefore ?.", true),
                LintError(12, 9, ruleId, "$shouldBreakBefore ?:", true),
                LintError(14, 8, ruleId, "$shouldBreakBefore ::", true)
        )
    }

    @Test
    fun `line breaking after infix functions - positive example`() {
        lintMethod(NewlinesRule(),
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
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `line breaking after infix functions`() {
        lintMethod(NewlinesRule(),
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
                LintError(3, 9, ruleId, "$shouldBreakAfter xor", true),
                LintError(6, 9, ruleId, "$shouldBreakAfter xor", true)
        )
    }

    @Test
    fun `chained calls should follow functional style - positive example`() {
        lintMethod(NewlinesRule(),
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
    fun `chained calls should follow functional style - should not trigger on single dot calls`() {
        lintMethod(NewlinesRule(),
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
    fun `chained calls should follow functional style`() {
        lintMethod(NewlinesRule(),
                """
                    |fun foo(list: List<Bar>?) {
                    |    list!!.filterNotNull()
                    |        .map { it.baz() }.firstOrNull {
                    |            it.condition()
                    |        }?.qux()
                    |}
                """.trimMargin(),
                LintError(2, 11, ruleId, "$functionalStyleWarn .", true),
                LintError(3, 26, ruleId, "$functionalStyleWarn .", true),
                LintError(5, 10, ruleId, "$functionalStyleWarn ?.", true)
        )
    }

    @Test
    fun `chained calls should follow functional style - exception for ternary if-else`() {
        lintMethod(NewlinesRule(),
                """
                    |fun foo(list: List<Bar>?) {
                    |    if (list.size > n) list.filterNotNull().map { it.baz() } else list.let { it.bar() }.firstOrNull()?.qux()
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `newline should be placed only after assignment operator`() {
        lintMethod(NewlinesRule(),
                """
                    |class Example {
                    |    val a =
                    |        42
                    |    val b
                    |        = 43
                    |}
                """.trimMargin(),
                LintError(5, 9, ruleId, "$shouldBreakAfter =", true)
        )
    }

    @Test
    fun `newline should be placed only after comma`() {
        lintMethod(NewlinesRule(),
                """
                    |fun foo(a: Int
                    |        ,
                    |        b: Int) {
                    |    bar(a
                    |        , b)
                    |}
                """.trimMargin(),
                LintError(2, 9, ruleId, commaWarn, true),
                LintError(5, 9, ruleId, commaWarn, true)
        )
    }

    @Test
    fun `function name should not be separated from ( - positive example`() {
        lintMethod(NewlinesRule(),
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
    fun `function name should not be separated from ( - should not trigger on other parenthesis`() {
        lintMethod(NewlinesRule(),
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
    fun `function name should not be separated from (`() {
        lintMethod(NewlinesRule(),
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
                LintError(1, 16, ruleId, lParWarn, true),
                LintError(3, 5, ruleId, lParWarn, true),
                LintError(7, 5, ruleId, lParWarn, true)
        )
    }

    @Test
    fun `long argument list should be split into several lines - positive example`() {
        lintMethod(NewlinesRule(),
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
    @Disabled("Will be implemented later")
    fun `long argument list should be split into several lines`() {
        lintMethod(NewlinesRule(),
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
                LintError(3, 14, ruleId, "${WRONG_NEWLINES.warnText()} argument list should be split into several lines", true),
                LintError(7, 12, ruleId, "${WRONG_NEWLINES.warnText()} argument list should be split into several lines", true)
        )
    }
}
