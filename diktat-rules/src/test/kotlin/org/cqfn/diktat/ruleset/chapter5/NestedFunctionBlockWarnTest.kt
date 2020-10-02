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
}
