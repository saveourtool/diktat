package org.cqfn.diktat.ruleset.chapter2.comments

import org.cqfn.diktat.ruleset.constants.Warnings.COMMENTED_OUT_CODE
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter2.comments.CommentsRule
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
        """.trimMargin())
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
        """.trimMargin())
    }

    @Test
    @Tag(WarningNames.COMMENTED_OUT_CODE)
    fun `Should warn if commented out function is detected single line comments`() {
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
            """.trimMargin())
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
            """.trimMargin())
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

    @Test
    @Tag(WarningNames.COMMENTED_OUT_CODE)
    fun `should not trigger on multiline comments #1`() {
        lintMethod(
            """
            |/*
            |
            |   Copyright 2018-2020 John Doe.
            |   
            |   Licensed under the Apache License, Version 2.0 (the "License");
            |   you may not use this file except in compliance with the License.
            |   You may obtain a copy of the License at
            |   
            |       http://www.apache.org/licenses/LICENSE-2.0
            |       
            |   Unless required by applicable law or agreed to in writing, software
            |   distributed under the License is distributed on an "AS IS" BASIS,
            |   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
            |   See the License for the specific language governing permissions and
            |   limitations under the License.
            |   
            |*/
            """.trimMargin())
    }

    @Test
    @Tag(WarningNames.COMMENTED_OUT_CODE)
    fun `should not trigger on multiline comments #2`() {
        lintMethod(
            """
            |   /*
            |   * some text here
            |   maybe even with another line
            |   */
            """.trimMargin())
    }

    @Test
    @Tag(WarningNames.COMMENTED_OUT_CODE)
    fun `should not trigger on Copyright and another comment`() {
        lintMethod(
            """
            /*
                Copyright (c) Your Company Name Here. 2010-2021
            */
            
            package org.cqfn.diktat
            
            /*
                x = 2 + 4 + 1
            */
            // x = 2+4
            
            // if true make this
            """.trimMargin(),
            LintError(7, 13, ruleId, "${COMMENTED_OUT_CODE.warnText()} ", false),
            LintError(10, 13, ruleId, "${COMMENTED_OUT_CODE.warnText()} x = 2+4", false)
            )
    }
}
