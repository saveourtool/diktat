package org.cqfn.diktat.ruleset.chapter3

import generated.WarningNames
import org.cqfn.diktat.ruleset.rules.NoBracesLambdasRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class NoBracesLambdasRuleFixTest : FixTestBase("test/paragraph3/braces", NoBracesLambdasRule()) {

    @Test
    @Tag(WarningNames.NO_BRACES_IN_LAMBDAS)
    fun `should delete lambdas braces`() {
        fixAndCompare("LambdasBracesExpected.kt", "LambdasBracesTest.kt")
    }
}