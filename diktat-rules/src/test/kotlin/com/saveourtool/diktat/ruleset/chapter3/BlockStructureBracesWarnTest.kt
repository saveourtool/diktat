package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.BRACES_BLOCK_STRUCTURE_ERROR
import com.saveourtool.diktat.ruleset.rules.chapter3.BlockStructureBraces
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class BlockStructureBracesWarnTest : LintTestBase(::BlockStructureBraces) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${BlockStructureBraces.NAME_ID}"
    private val rulesConfigList: List<RulesConfig> = listOf(
        RulesConfig(BRACES_BLOCK_STRUCTURE_ERROR.name, true,
            mapOf("openBraceNewline" to "False", "closeBraceNewline" to "False"))
    )
    private val rulesConfigListIgnoreOpen: List<RulesConfig> = listOf(
        RulesConfig(BRACES_BLOCK_STRUCTURE_ERROR.name, true,
            mapOf("openBraceNewline" to "False"))
    )
    private val rulesConfigListIgnoreClose: List<RulesConfig> = listOf(
        RulesConfig(BRACES_BLOCK_STRUCTURE_ERROR.name, true,
            mapOf("closeBraceNewline" to "False"))
    )

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check if expression with new line else`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if (x < -5) {
                    |       goo()
                    |    }
                    |    else {
                    |       koo()}
                    |}
            """.trimMargin(),
            DiktatError(4, 6, ruleId, "${BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect new line after closing brace", true),
            DiktatError(6, 13, ruleId, "${BRACES_BLOCK_STRUCTURE_ERROR.warnText()} no newline before closing brace", true)
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `correct if expression without else`() {
        val withBrace =
            """
                |fun foo() {
                |   if (x > 5) {
                |       x--
                |   }
                |}
            """.trimMargin()
        lintMethod(withBrace)

        val withoutBrace =
            """
                |fun foo() {
                |   if (x > 5)
                |       x--
                |}
            """.trimMargin()
        lintMethod(withoutBrace)
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check correct if with else-if expression`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if (x < -5) {
                    |       goo()
                    |    } else if (x > 5) {
                    |       hoo()
                    |    } else {
                    |       koo()
                    |    }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check lambda with empty block`() {
        lintMethod(
            """
                    |fun foo() {
                    |   run {
                    |
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check empty block in else expression`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if (x < -5) {
                    |       goo()
                    |    } else if (x > 5) {
                    |       hoo()
                    |    } else {
                    |    }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check wrong empty block in if expression`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if (x < -5) {
                    |       goo()
                    |    } else {}
                    |}
            """.trimMargin(),
            DiktatError(4, 13, ruleId, "${BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect same line after opening brace", true),
            DiktatError(4, 13, ruleId, "${BRACES_BLOCK_STRUCTURE_ERROR.warnText()} no newline before closing brace", true)
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check if expression with wrong opening brace position`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if (x < -5)
                    |    {
                    |       bf()
                    |    } else { f()
                    |    }
                    |}
            """.trimMargin(),
            DiktatError(2, 16, ruleId, "${BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect newline before opening brace", true),
            DiktatError(5, 13, ruleId, "${BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect same line after opening brace", true)
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check if expression with wrong closing brace position`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if (x < -5) {
                    |       bf() }
                    |    else {
                    |       f() }
                    |}
            """.trimMargin(),
            DiktatError(3, 13, ruleId, "${BRACES_BLOCK_STRUCTURE_ERROR.warnText()} no newline before closing brace", true),
            DiktatError(3, 14, ruleId, "${BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect new line after closing brace", true),
            DiktatError(5, 12, ruleId, "${BRACES_BLOCK_STRUCTURE_ERROR.warnText()} no newline before closing brace", true)
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check wrong brace in if expression but with off configuration`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if (x < -5)
                    |    {
                    |       goo() }
                    |    else
                    |    {
                    |       hoo() }
                    |}
            """.trimMargin(),
            DiktatError(4, 15, ruleId, "${BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect new line after closing brace", true),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check function expression with wrong open brace with configuration`() {
        lintMethod(
            """
                    |fun foo()
                    |{
                    |   pyu()
                    |}
            """.trimMargin(), rulesConfigList = rulesConfigListIgnoreOpen
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check empty fun expression with override annotation`() {
        lintMethod(
            """
                    |override fun foo() {
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check one line fun`() {
        lintMethod(
            """
                    |fun foo() = 0
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check fun with empty block won't be processed`() {
        lintMethod(
            """
                    |fun foo() {}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check function expression with wrong close brace`() {
        lintMethod(
            """
                    |fun foo() {
                    |   pyu() }
            """.trimMargin(),
            DiktatError(2, 10, ruleId, "${BRACES_BLOCK_STRUCTURE_ERROR.warnText()} no newline before closing brace", true)
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check simple wrong open brace when expression`() {
        lintMethod(
            """
                    |fun a(x: Int) {
                    |   when (x)
                    |   {
                    |       1 -> println(2)
                    |       else -> println("df")
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(2, 12, ruleId, "${BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect newline before opening brace", true)
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check correct simple for without brace `() {
        lintMethod(
            """
                    |fun a(x: Int) {
                    |   for (i in 1..3)
                    |       println(i)
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check wrong for expression with empty block but with config`() {
        lintMethod(
            """
                    |fun a(x: Int) {
                    |   for (i in 1..3) {}
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check correct while without brace`() {
        lintMethod(
            """
                    |fun sdf() {
                    |   while (x > 0)
                    |       x--
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check wrong do-while with open brace`() {
        lintMethod(
            """
                    |fun sdf() {
                    |   do
                    |   {
                    |       x--
                    |   } while (x != 0)
                    |}
            """.trimMargin(),
            DiktatError(2, 6, ruleId, "${BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect newline before opening brace", true)
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check try-catch-finally with wrong position catch and finally words `() {
        lintMethod(
            """
                    |fun divideOrZero(numerator: Int, denominator: Int): Int {
                    |   try {
                    |       return numerator / denominator
                    |   } catch (e: ArithmeticException) {
                    |       return 0
                    |   }
                    |   catch (e: Exception) {
                    |       return 1
                    |   }
                    |   finally {
                    |       println("Hello")
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(6, 5, ruleId, "${BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect new line after closing brace", true),
            DiktatError(9, 5, ruleId, "${BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect new line after closing brace", true)
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check wrong try-catch with open and close braces`() {
        lintMethod(
            """
                    |fun divideOrZero(numerator: Int, denominator: Int): Int {
                    |   try { return numerator / denominator
                    |   } catch (e: ArithmeticException) {
                    |       return 0
                    |   } catch (e: Exception) {
                    |       return 1 }
                    |}
            """.trimMargin(),
            DiktatError(2, 9, ruleId, "${BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect same line after opening brace", true),
            DiktatError(6, 17, ruleId, "${BRACES_BLOCK_STRUCTURE_ERROR.warnText()} no newline before closing brace", true)
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check correct simple class expression`() {
        lintMethod(
            """
                    |class A {
                    |   fun foo() {
                    |       println("Hello")
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check wrong simple class expression but with config`() {
        lintMethod(
            """
                    |class A
                    |{
                    |   fun foo() {
                    |       println("Hello")
                    |   } }
            """.trimMargin(), rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check wrong close brace in object expression but with ignore config`() {
        lintMethod(
            """
                    |object A {
                    |   fun foo() {
                    |       println("Hello")
                    |   }
                    |
                    |   val x: Int = 10 }
            """.trimMargin(), rulesConfigList = rulesConfigListIgnoreClose
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check wrong open brace in object expression`() {
        lintMethod(
            """
                    |object A
                    |{
                    |   fun foo() {
                    |       println("Hello")
                    |   }
                    |
                    |   val x: Int = 10
                    |}
            """.trimMargin(),
            DiktatError(1, 9, ruleId, "${BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect newline before opening brace", true)
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check init expression with wrong opening and closing brace position `() {
        lintMethod(
            """
                    |class A {
                    |   init
                    |   {
                    |       foo() }
                    |
                    |   fun foo() {
                    |       println("Hello")
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(2, 8, ruleId, "${BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect newline before opening brace", true),
            DiktatError(4, 14, ruleId, "${BRACES_BLOCK_STRUCTURE_ERROR.warnText()} no newline before closing brace", true)
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check correct simple constructor expression`() {
        lintMethod(
            """
                    |class Person() {
                    |   constructor(id: Int) {
                    |       println(id)
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check wrong constructor expression but with ignore opening brace config`() {
        lintMethod(
            """
                    |class Person()
                    |{
                    |   constructor(id: Int) {
                    |       println(id)
                    |   }
                    |}
            """.trimMargin(), rulesConfigList = rulesConfigListIgnoreOpen
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check lambda with incorrect close brace position`() {
        lintMethod(
            """
                    |class Person() {
                    |   val list = listOf("Hello", "World")
                    |
                    |   fun foo(){
                    |       val size = list.map { it.length }
                    |       size.forEach { println(it) }
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.BRACES_BLOCK_STRUCTURE_ERROR)
    fun `check lambdas`() {
        lintMethod(
            """
                    |fun foo() {
                    |   val x = t.map {it -> it.size}
                    |   val y = q
                    |          .map { it.treeParent }
                    |           .filter { it.elementType == CLASS }
                    |   val y = q
                    |           .map { it.treeParent }
                    |           .filter { it.elementType == CLASS &&
                    |               it.text == "sdc" }
                    |}
            """.trimMargin(),
            DiktatError(9, 33, ruleId, "${BRACES_BLOCK_STRUCTURE_ERROR.warnText()} no newline before closing brace", true)
        )
    }
}
