package org.cqfn.diktat.ruleset.chapter3

import generated.WarningNames
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.NoBracesLambdasWhenRule
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class NoBracesLambdasWhenRuleWarnTest {

    private val ruleId = "$DIKTAT_RULE_SET_ID:no-braces-lambdas-when"

    @Test
    @Tag(WarningNames.NO_BRACES_IN_LAMBDAS_AND_WHEN)
    fun `some test`() {
        lintMethod(NoBracesLambdasWhenRule(),
                """
                    |
                """.trimMargin()
        )
    }
}