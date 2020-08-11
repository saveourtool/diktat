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
