package org.cqfn.diktat.ruleset.chapter2.comments

import org.cqfn.diktat.ruleset.constants.Warnings.COMMENTED_OUT_CODE
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.comments.CommentsRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CommentedCodeTest : LintTestBase(::CommentsRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:comments"

    @Test
    @Tag(WarningNames.COMMENTED_OUT_CODE)
    fun `Should warn if commented out import or package directive is detected (single line comments)`() {
        lintMethod(
            """
                |//package org.cqfn.diktat.example
                |
                |import org.junit.Test
                |// this is an actual comment
                |//import org.junit.Ignore
            """.trimMargin(),
            LintError(1, 1, ruleId, "${COMMENTED_OUT_CODE.warnText()} package org.cqfn.diktat.example", false),
            LintError(5, 1, ruleId, "${COMMENTED_OUT_CODE.warnText()} import org.junit.Ignore", false)
        )
    }

    @Test
    @Tag(WarningNames.COMMENTED_OUT_CODE)
    fun `Should warn if commented out imports are detected (block comments)`() {
        lintMethod(
            """
           |/*import org.junit.Test
           |import org.junit.Ignore*/
        """.trimMargin(),
            LintError(1, 1, ruleId, "${COMMENTED_OUT_CODE.warnText()} import org.junit.Test", false),
            LintError(1, 1, ruleId, "${COMMENTED_OUT_CODE.warnText()} import org.junit.Ignore", false)
        )
    }

    @Test
    @Tag(WarningNames.COMMENTED_OUT_CODE)
    fun `Should warn if commented out code is detected (block comments)`() {
        lintMethod(
            """
           |import org.junit.Test
           |
           |fun foo(a: Int): Int {
           |    /* println(a + 42)
           |    println("This is a test string")
           |    */
           |    return 0
           |}
        """.trimMargin(),
            LintError(4, 5, ruleId, "${COMMENTED_OUT_CODE.warnText()} println(a + 42)", false)
        )
    }

    @Test
    @Tag(WarningNames.COMMENTED_OUT_CODE)
    fun `Should warn if commented out code is detected (single line comments)`() {
        lintMethod(
            """
           |import org.junit.Test
           |
           |fun foo(a: Int): Int {
           |//    println(a + 42)
           |//    println("This is a test string")
           |    return 0
           |}
        """.trimMargin(),
            LintError(4, 1, ruleId, "${COMMENTED_OUT_CODE.warnText()} println(a + 42)", false)
        )
    }

    @Test
    @Tag(WarningNames.COMMENTED_OUT_CODE)
    fun `Should warn if commented out function is detected (single line comments)`() {
        lintMethod(
            """
           |import org.junit.Test
           |
           |//fun foo(a: Int): Int {
           |//    println(a + 42)
           |//    println("This is a test string")
           |//    return 0
           |//}
        """.trimMargin(),
            LintError(3, 1, ruleId, "${COMMENTED_OUT_CODE.warnText()} fun foo(a: Int): Int {", false)
        )
    }

    @Test
    @Tag(WarningNames.COMMENTED_OUT_CODE)
    fun `Should warn if commented out function is detected - single line comments with surrounding text`() {
        lintMethod(
            """
           |import org.junit.Test
           |
           |// this function is disabled for now
           |//fun foo(a: Int): Int {
           |//    println(a + 42)
           |//    println("This is a test string")
           |//    return 0
           |//}
        """.trimMargin(),
            LintError(4, 1, ruleId, "${COMMENTED_OUT_CODE.warnText()} fun foo(a: Int): Int {", false)
        )
    }

    @Test
    @Tag(WarningNames.COMMENTED_OUT_CODE)
    fun `Should warn if commented out function is detected (block comment)`() {
        lintMethod(
            """
           |import org.junit.Test
           |
           |/*fun foo(a: Int): Int {
           |    println(a + 42)
           |    println("This is a test string")
           |    return 0
           |}*/
        """.trimMargin(),
            LintError(3, 1, ruleId, "${COMMENTED_OUT_CODE.warnText()} fun foo(a: Int): Int {", false)
        )
    }

    @Test
    @Tag(WarningNames.COMMENTED_OUT_CODE)
    fun `Should warn if detects commented out code (example with indents)`() {
        lintMethod(
            """
                |//import org.junit.Ignore
                |import org.junit.Test
                |
                |class Example {
                |    // this function is disabled for now
                |    //fun foo(a: Int): Int {
                |    //    println(a + 42)
                |    //    println("This is a test string")
                |    //    return 0
                |    //}
                |}
            """.trimMargin(),
            LintError(1, 1, ruleId, "${COMMENTED_OUT_CODE.warnText()} import org.junit.Ignore", false),
            LintError(6, 5, ruleId, "${COMMENTED_OUT_CODE.warnText()} fun foo(a: Int): Int {", false)
        )
    }

    @Test
    @Tag(WarningNames.COMMENTED_OUT_CODE)
    fun `Should warn if detects commented out code example with IDEA style indents`() {
        lintMethod(
            """
                |//import org.junit.Ignore
                |import org.junit.Test
                |
                |class Example {
                |    // this function is disabled for now
                |//    fun foo(a: Int): Int {
                |//        println(a + 42)
                |//        println("This is a test string")
                |//        return 0
                |//    }
                |}
            """.trimMargin(),
            LintError(1, 1, ruleId, "${COMMENTED_OUT_CODE.warnText()} import org.junit.Ignore", false),
            LintError(6, 1, ruleId, "${COMMENTED_OUT_CODE.warnText()} fun foo(a: Int): Int {", false)
        )
    }

    @Test
    @Tag(WarningNames.COMMENTED_OUT_CODE)
    fun `should trigger on class with one space after comment start token`() {
        lintMethod(
            """
            |// class Test: Exception()
            """.trimMargin(),
            LintError(1, 1, ruleId, "${COMMENTED_OUT_CODE.warnText()} class Test: Exception()", false))
    }

    @Test
    @Tag(WarningNames.COMMENTED_OUT_CODE)
    fun `should trigger on class with one space after comment start token and 2 modifiers #1`() {
        lintMethod(
            """
            |// public data class Test(val some: Int): Exception()
            """.trimMargin(),
            LintError(1, 1, ruleId, "${COMMENTED_OUT_CODE.warnText()} public data class Test(val some: Int): Exception()", false))
    }

    @Test
    @Tag(WarningNames.COMMENTED_OUT_CODE)
    fun `should trigger on class with one space after comment start token and 2 modifiers #2`() {
        lintMethod(
            """
            |// internal sealed class Test: Exception()
            """.trimMargin(),
            LintError(1, 1, ruleId, "${COMMENTED_OUT_CODE.warnText()} internal sealed class Test: Exception()", false))
    }

    @Test
    @Tag(WarningNames.COMMENTED_OUT_CODE)
    fun `should trigger on import with one space after comment start token`() {
        lintMethod(
            """
            |// import some.org
            """.trimMargin(),
            LintError(1, 1, ruleId, "${COMMENTED_OUT_CODE.warnText()} import some.org", false))
    }

    @Test
    @Tag(WarningNames.COMMENTED_OUT_CODE)
    fun `should trigger on package with one space after comment start token`() {
        lintMethod(
            """
            |// package some.org
            """.trimMargin(),
            LintError(1, 1, ruleId, "${COMMENTED_OUT_CODE.warnText()} package some.org", false))
    }

    @Test
    @Tag(WarningNames.COMMENTED_OUT_CODE)
    fun `should trigger on function with one space after comment start token - { sign`() {
        lintMethod(
            """
            |// fun someFunc(name: String): Boolean {
            |//     val a = 5
            |// }
            """.trimMargin(),
            LintError(1, 1, ruleId, "${COMMENTED_OUT_CODE.warnText()} fun someFunc(name: String): Boolean {", false))
    }

    @Test
    @Tag(WarningNames.COMMENTED_OUT_CODE)
    fun `should trigger on function with one space after comment start token - = sign`() {
        lintMethod(
            """
            |// fun someFunc(name: String): Boolean =
            |//     name.contains("a")
            """.trimMargin(),
            LintError(1, 1, ruleId, "${COMMENTED_OUT_CODE.warnText()} fun someFunc(name: String): Boolean =", false))
    }

    @Test
    @Tag(WarningNames.COMMENTED_OUT_CODE)
    fun `should trigger on function with one space after comment start token pulbic modifier`() {
        lintMethod(
            """
            |// public fun someFunc(name: String): Boolean =
            |//     name.contains("a")
            """.trimMargin(),
            LintError(1, 1, ruleId, "${COMMENTED_OUT_CODE.warnText()} public fun someFunc(name: String): Boolean =", false))
    }
}
