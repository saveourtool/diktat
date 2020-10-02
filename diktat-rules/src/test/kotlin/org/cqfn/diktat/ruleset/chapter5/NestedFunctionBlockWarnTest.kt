package org.cqfn.diktat.ruleset.chapter5

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.constants.Warnings.NESTED_BLOCK
import org.cqfn.diktat.ruleset.rules.NestedFunctionBlock
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class NestedFunctionBlockWarnTest : LintTestBase(::NestedFunctionBlock) {

    private val ruleId = "$DIKTAT_RULE_SET_ID:nested-block"

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
                LintError(1,1, ruleId, "${NESTED_BLOCK.warnText()} foo", false)
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
                """.trimMargin(),
                LintError(1,1, ruleId, "${NESTED_BLOCK.warnText()} foo", false)
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
                    |                       if(false){
                    |                           println("ne")
                    |                       }
                    |                   }
                    |               }
                    |       } else {
                    |               println("dscsds")
                    |           }
                    |       }
                    |   }
                    |}
                """.trimMargin()
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
}
