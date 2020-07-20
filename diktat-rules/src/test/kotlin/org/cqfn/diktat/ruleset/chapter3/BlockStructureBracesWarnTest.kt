package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.BlockStructureBraces
import org.cqfn.diktat.ruleset.rules.BracesInConditionalsAndLoopsRule
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.utils.lintMethod
import org.junit.Test

class BlockStructureBracesWarnTest {

    private val ruleId = "$DIKTAT_RULE_SET_ID:block-structure"

    @Test
    fun `check if with else if`() {
        lintMethod(BlockStructureBraces(),
                """
                    |fun foo() {
                    |    if (x > 0 && x != 10) {
                    |        bar() 
                    |        } else {
                    |        baz() 
                    |        }
                    |    
                    |    if (x < -5) {
                    |       baxx()
                    |       }
                    |    else if (x > 5) {
                    |       rt()
                    |       } else {
                    |       yy() 
                    |       }
                    |}
                    |
                    |fun foo2(array: List<Int>){
                    |
                    |   array.forEach { model ->
                    |       run {
                    |           if (model % 2 == 0) {
                    |               println(model.map { it.id })
                    |               }
                    |       }
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `check simple if `() {
        lintMethod(BlockStructureBraces(),
                """
                    |fun foo() {
                    |    if (x < -5) {
                    |       bf()
                    |    } else {
                    |       f()
                    |    }
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `check wrong if `() {
        lintMethod(BlockStructureBraces(),
                """
                    |fun foo() {
                    |    if (x < -5)
                    |    {
                    |       bf()
                    |    } else {
                    |       f()
                    |    }
                    |}
                """.trimMargin(),
                LintError(1, 1, ruleId, "${Warnings.BRACES_BLOCK_STRUCTURE_ERROR.warnText()} open braces", false)
        )
    }

    @Test
    fun `check simple function `() {
        lintMethod(BlockStructureBraces(),
                """
                    |fun foo() {
                    |   pyu()
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `check simple when `() {
        lintMethod(BlockStructureBraces(),
                """
                    |fun a(x: Int) {
                    |   when (x) { 
                    |       1 -> println(2)
                    |       else -> println("df")
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `check simple for `() {
        lintMethod(BlockStructureBraces(),
                """
                    |fun a(x: Int) {
                    |   for (i in 1..3) {
                    |       println(i)
                    |   }
                    |}
                """.trimMargin()
        )
    }
}