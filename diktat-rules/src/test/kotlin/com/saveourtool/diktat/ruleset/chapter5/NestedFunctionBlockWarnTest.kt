package com.saveourtool.diktat.ruleset.chapter5

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.NESTED_BLOCK
import com.saveourtool.diktat.ruleset.rules.chapter5.NestedFunctionBlock
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class NestedFunctionBlockWarnTest : LintTestBase(::NestedFunctionBlock) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${NestedFunctionBlock.NAME_ID}"
    private val rulesConfigList = listOf(
        RulesConfig(NESTED_BLOCK.name, true, mapOf("maxNestedBlockQuantity" to "2"))
    )

    @Test
    @Tag(WarningNames.NESTED_BLOCK)
    fun `should ignore lambda expression`() {
        lintMethod(
            """
                    |fun foo() {
                    |   while(true) {
                    |       println()
                    |   }
                    |
                    |   when(x) {
                    |       10 -> {10}
                    |       else -> {
                    |           if (true) {
                    |               println(1)
                    |           }
                    |       }
                    |   }
                    |
                    |   val x = {
                    |       if (true) {
                    |           if (false) {
                    |               while(false) {
                    |                   10
                    |               }
                    |           }
                    |       }
                    |   }
                    |
                    |   for(x in 1..2){
                    |       println(x)
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.NESTED_BLOCK)
    fun `check simple nested block`() {
        lintMethod(
            """
                    |fun foo() {
                    |
                    |   if (true) {
                    |       if (false) {
                    |           if (true) {
                    |               do {
                    |                   println("nested")
                    |               } while(true)
                    |           }
                    |       }
                    |   } else {
                    |       println("dscsds")
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${NESTED_BLOCK.warnText()} foo", false)
        )
    }

    @Test
    @Tag(WarningNames.NESTED_BLOCK)
    fun `check simple nested block with try`() {
        lintMethod(
            """
                    |fun foo() {
                    |
                    |   if (true) {
                    |       if (false) {
                    |           try {
                    |               try{
                    |                   try{
                    |
                    |                   } catch(ex: Exception){
                    |                       try{
                    |                           println("hi")
                    |                       } catch(ex: Exception){}
                    |                   }
                    |               } catch(ex: Exception){}
                    |           } catch(ex: Exception){}
                    |       }
                    |   } else {
                    |       println("dscsds")
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${NESTED_BLOCK.warnText()} foo", false)
        )
    }

    @Test
    @Tag(WarningNames.NESTED_BLOCK)
    fun `check simple nested block of function`() {
        lintMethod(
            """
                    |fun foo() {
                    |
                    |   if (true) {
                    |       if (false) {
                    |           fun goo() {
                    |               if(true) {
                    |
                    |               }
                    |           }
                    |       }
                    |   } else {
                    |       println("dscsds")
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.NESTED_BLOCK)
    fun `check simple nested block of local class`() {
        lintMethod(
            """
                    |fun foo() {
                    |   class A() {
                    |       fun goo() {
                    |           if (true) {
                    |               if (false) {
                    |                   while(true) {
                    |                       if(false) {
                    |                           try {
                    |                               println("ne")
                    |                           } catch (e: Exception) {}
                    |                       }
                    |                   }
                    |               }
                    |       } else {
                    |               println("dscsds")
                    |           }
                    |       }
                    |   }
                    |   if(true) {
                    |       while(true) {
                    |       }
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(3, 8, ruleId, "${NESTED_BLOCK.warnText()} goo", false)
        )
    }

    @Test
    @Tag(WarningNames.NESTED_BLOCK)
    fun `check with lambda`() {
        lintMethod(
            """
                    private fun findBlocks(node: ASTNode): List<ASTNode> {
                        val result = mutableListOf<ASTNode>()
                        node.getChildren(null).forEach {
                            when (it.elementType) {
                                IF -> Pair(it.findChildByType(THEN)?.findChildByType(BLOCK), it.findChildByType(ELSE)?.findChildByType(BLOCK))
                                WHEN -> Pair(it, null)
                                WHEN_ENTRY -> Pair(it.findChildByType(BLOCK), null)
                                FUN -> Pair(it.findChildByType(BLOCK), null)
                                else -> Pair(it.findChildByType(BODY)?.findChildByType(BLOCK), null)
                            }.let { pair ->
                                pair.let {
                                    pair.first?.let { it1 -> result.add(it1) }
                                    pair.second?.let { it2 -> result.add(it2) }
                                }
                            }
                        }
                        return result
                    }
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.NESTED_BLOCK)
    fun `check with anonymous class`() {
        lintMethod(
            """

                    val q = list.filter {it == 0}

                    val keyListener = KeyAdapter { keyEvent ->
                        if (true) {
                        } else if (false) {
                            while(true) {
                                if(true) {
                                    println(10)
                                }
                            }
                        }
                    }

                    val keyListener = object : KeyAdapter() {
                        override fun keyPressed(keyEvent : KeyEvent) {
                        }
                    }
            """.trimMargin(),
            DiktatError(4, 50, ruleId, "${NESTED_BLOCK.warnText()} { keyEvent ->...", false),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.NESTED_BLOCK)
    fun `check simple nested block inside class`() {
        lintMethod(
            """
                    |class A {
                    |   fun foo() {
                    |       if(true) {
                    |           if(false) {
                    |               if(true) {
                    |                   when(x) {
                    |                   }
                    |               }
                    |           }
                    |       }
                    |   }
                    |
                    |   fun goo() {
                    |       if(true){
                    |           if(false){
                    |               if(true){
                    |               }
                    |           }
                    |       }
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(2, 4, ruleId, "${NESTED_BLOCK.warnText()} foo", false)
        )
    }
}
