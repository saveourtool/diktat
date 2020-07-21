package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.BlockStructureBraces
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.utils.lintMethod
import org.junit.Test

class BlockStructureBracesWarnTest {

    private val ruleId = "$DIKTAT_RULE_SET_ID:block-structure"

    private val rulesConfigList: List<RulesConfig> = listOf(
            RulesConfig(Warnings.BRACES_BLOCK_STRUCTURE_ERROR.name, true,
                    mapOf("openBraceNewline" to "False", "closeBraceNewline" to "False"))
    )

    private val rulesConfigListIgnoreOpen: List<RulesConfig> = listOf(
            RulesConfig(Warnings.BRACES_BLOCK_STRUCTURE_ERROR.name, true,
                    mapOf("openBraceNewline" to "False"))
    )

    private val rulesConfigListIgnoreClose: List<RulesConfig> = listOf(
            RulesConfig(Warnings.BRACES_BLOCK_STRUCTURE_ERROR.name, true,
                    mapOf("closeBraceNewline" to "False"))
    )

    @Test
    fun `check if expression with new line else`() {
        lintMethod(BlockStructureBraces(),
                """
                    |fun foo() {
                    |    if (x < -5) {
                    |       goo()
                    |    }
                    |    else {
                    |       koo() 
                    |    }
                    |}
                """.trimMargin(),
                LintError(4, 6, ruleId, "${Warnings.BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect new line after closing brace", false)
        )
    }

    @Test
    fun `correct if expression without else`() {
        val withBrace =
                """
                    |fun foo() {
                    |   if (x > 5){
                    |       x--
                    |   }
                    |}
                """.trimMargin()
        val withoutBrace =
                """
                    |fun foo() {
                    |   if (x > 5)
                    |       x--
                    |}
                """.trimMargin()
        lintMethod(BlockStructureBraces(), withBrace)
        lintMethod(BlockStructureBraces(), withoutBrace)
    }

    @Test
    fun `check correct if with else-if expression`() {
        lintMethod(BlockStructureBraces(),
                """
                    |fun foo() {
                    |    if (x < -5) {
                    |       goo()
                    |    }
                    |    else if (x > 5) {
                    |       hoo()
                    |    } else {
                    |       koo() 
                    |    }
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `check if expression with wrong opening brace position`() {
        lintMethod(BlockStructureBraces(),
                """
                    |fun foo() {
                    |    if (x < -5)
                    |    {
                    |       bf()
                    |    } else { f()
                    |    }
                    |}
                """.trimMargin(),
                LintError(2, 16, ruleId, "${Warnings.BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect newline before opening brace", false),
                LintError(5, 13, ruleId, "${Warnings.BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect same line after opening brace", false)
        )
    }

    @Test
    fun `check if expression with wrong closing brace position`() {
        lintMethod(BlockStructureBraces(),
                """
                    |fun foo() {
                    |    if (x < -5) {
                    |       bf() }
                    |    else {
                    |       f() }
                    |}
                """.trimMargin(),
                LintError(3, 13, ruleId, "${Warnings.BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect same line after closing brace", false),
                LintError(3, 14, ruleId, "${Warnings.BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect new line after closing brace", false),
                LintError(5, 12, ruleId, "${Warnings.BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect same line after closing brace", false)
        )
    }

    @Test
    fun `check wrong brace in if expression but with off configuration`() {
        lintMethod(BlockStructureBraces(),
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
                LintError(4, 15, ruleId, "${Warnings.BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect new line after closing brace", false),
                rulesConfigList = rulesConfigList
        )
    }

    @Test
    fun `check function expression with wrong open brace with configuration`() {
        lintMethod(BlockStructureBraces(),
                """
                    |fun foo() {
                    |   pyu()
                    |}
                """.trimMargin(), rulesConfigList = rulesConfigListIgnoreOpen
        )
    }

    @Test
    fun `check one line fun`() {
        lintMethod(BlockStructureBraces(),
                """
                    |fun foo() = 0
                """.trimMargin()
        )
    }

    @Test
    fun `check function expression with wrong close brace`() {
        lintMethod(BlockStructureBraces(),
                """
                    |fun foo() {
                    |   pyu() }
                """.trimMargin(),
                LintError(2, 10, ruleId, "${Warnings.BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect same line after closing brace", false)
        )
    }

    @Test
    fun `check simple wrong open brace when expression`() {
        lintMethod(BlockStructureBraces(),
                """
                    |fun a(x: Int) {
                    |   when (x)
                    |   {
                    |       1 -> println(2)
                    |       else -> println("df")
                    |   }
                    |}
                """.trimMargin(),
                LintError(2, 12, ruleId, "${Warnings.BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect newline before opening brace", false)
        )
    }

    @Test
    fun `check correct simple for without brace `() {
        lintMethod(BlockStructureBraces(),
                """
                    |fun a(x: Int) {
                    |   for (i in 1..3)
                    |       println(i)
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `check correct while without brace`() {
        lintMethod(BlockStructureBraces(),
                """
                    |fun sdf() {
                    |   while (x > 0)
                    |       x--
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `check wrong do-while with open brace`() {
        lintMethod(BlockStructureBraces(),
                """
                    |fun sdf() {
                    |   do
                    |   {
                    |       x-- 
                    |   } while (x != 0)
                    |}
                """.trimMargin(),
                LintError(2, 6, ruleId, "${Warnings.BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect newline before opening brace", false)
        )
    }

    @Test
    fun `check try-catch-finally with wrong position catch and finally words `() {
        lintMethod(BlockStructureBraces(),
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
                LintError(6, 5, ruleId, "${Warnings.BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect new line after closing brace", false),
                LintError(9, 5, ruleId, "${Warnings.BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect new line after closing brace", false)
        )
    }

    @Test
    fun `check wrong try-catch with open and close braces`() {
        lintMethod(BlockStructureBraces(),
                """
                    |fun divideOrZero(numerator: Int, denominator: Int): Int {
                    |   try { return numerator / denominator
                    |   } catch (e: ArithmeticException) {
                    |       return 0 
                    |   } catch (e: Exception) {
                    |       return 1 }
                    |}
                """.trimMargin(),
                LintError(2, 9, ruleId, "${Warnings.BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect same line after opening brace", false),
                LintError(6, 17, ruleId, "${Warnings.BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect same line after closing brace", false)
        )
    }

    @Test
    fun `check correct simple class expression`() {
        lintMethod(BlockStructureBraces(),
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
    fun `check wrong simple class expression but with config`() {
        lintMethod(BlockStructureBraces(),
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
    fun `check wrong close brace in object expression but with ignore config`() {
        lintMethod(BlockStructureBraces(),
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
    fun `check wrong open brace in object expression`() {
        lintMethod(BlockStructureBraces(),
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
                LintError(1, 9, ruleId, "${Warnings.BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect newline before opening brace", false)
        )
    }

    @Test
    fun `check init expression with wrong opening and closing brace position `(){
        lintMethod(BlockStructureBraces(),
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
                LintError(2, 8, ruleId, "${Warnings.BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect newline before opening brace", false),
                LintError(4, 14, ruleId, "${Warnings.BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect same line after closing brace", false)
        )
    }

    @Test
    fun `check correct simple constructor expression`() {
        lintMethod(BlockStructureBraces(),
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
    fun `check wrong constructor expression but with ignore opening brace config`() {
        lintMethod(BlockStructureBraces(),
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
    fun `check lambda with incorrect close brace position`() {
        lintMethod(BlockStructureBraces(),
                """
                    |class Person() {
                    |   val list = listOf("Hello", "World")
                    |   
                    |   fun foo(){
                    |       val size = list.map { it.length }
                    |       size.forEach { println(it) }
                    |   }
                    |}
                """.trimMargin(),
                LintError(5, 40, ruleId, "${Warnings.BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect same line after closing brace", false),
                LintError(6, 35, ruleId, "${Warnings.BRACES_BLOCK_STRUCTURE_ERROR.warnText()} incorrect same line after closing brace", false)
        )
    }
}
