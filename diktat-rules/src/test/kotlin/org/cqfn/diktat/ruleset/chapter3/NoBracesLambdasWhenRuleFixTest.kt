package org.cqfn.diktat.ruleset.chapter3

import generated.WarningNames
import org.cqfn.diktat.ruleset.rules.NoBracesLambdasWhenRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class NoBracesLambdasWhenRuleFixTest : FixTestBase("test/paragraph3/statement", NoBracesLambdasWhenRule()) {

    @Test
    @Tag(WarningNames.NO_BRACES_IN_LAMBDAS_AND_WHEN)
    fun `should delete lambdas braces`() {
        fixAndCompare("LambdasBracesExpected.kt", "LambdasBracesTest.kt")
    }
}