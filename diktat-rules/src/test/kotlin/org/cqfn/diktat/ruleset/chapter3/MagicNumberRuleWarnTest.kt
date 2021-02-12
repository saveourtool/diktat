package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter3.MagicNumberRule
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Test

class MagicNumberRuleWarnTest : LintTestBase(::MagicNumberRule) {

    private val ruleId = "$DIKTAT_RULE_SET_ID:magic-number"

    @Test
    fun `simple check`() {
        lintMethod(
            """
                |fun f1oo() {
                |   val a: Byte = 4
                |   val b = 0xff
                |   val c = 2
                |   val d = 3.4
                |   val e = 3.4f
                |   val g = 1/2
                |   val f = "qwe\$\{12\}hhe"
                |}
                """.trimMargin()
        )
    }
}