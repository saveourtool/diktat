package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.rules.BracesInConditionalsAndLoopsRule
import org.cqfn.diktat.ruleset.rules.NoSpacesRule
import org.cqfn.diktat.util.FixTestBase
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Test


class NoSpacesRuleWarn {
    @Test
    fun `test`() {
        lintMethod(NoSpacesRule(),
                """
                    |import com.pinterest.ktlint.core.KtLint; import com.pinterest.ktlint.core.LintError
                    |
                    |fun foo() : Boolean {
                    |    val a: Int = 5
                    |    if (x < -5) {
                    |       goo(); hoo()
                    |    }
                    |    else {
                    |    }
                    |    
                    |    when(x) {
                    |       1 -> println(1)
                    |       else -> println("3;5")
                    |    }
                    |    val a = 5; val b = 10
                    |    println(1); println(1)
                    |}
                """.trimMargin()
        )
    }
}