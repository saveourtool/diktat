package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.rules.BlockStructureBraces
import org.cqfn.diktat.ruleset.utils.FixTestBase
import org.junit.Test

class BlockStructureBracesFixTestTest : FixTestBase ("test/paragraph3/block_brace", BlockStructureBraces()){

    @Test
    fun `should fix open and close brace in if-else expression`() {
        fixAndCompare("IfElseBracesExpected.kt", "IfElseBracesTest.kt")
    }

    @Test
    fun `should fix open and close brace in class expression`() {
        fixAndCompare("ClassBracesExpected.kt", "ClassBracesTest.kt")
    }

    @Test
    fun `should fix open and close brace in do-while expression`() {
        fixAndCompare("DoWhileBracesExpected.kt", "DoWhileBracesTest.kt")
    }

    @Test
    fun `should fix open and close brace in loops expression`() {
        fixAndCompare("LoopsBracesExpected.kt", "LoopsBracesTest.kt")
    }

    @Test
    fun `should fix open and close brace in when expression`() {
        fixAndCompare("WhenBranchesExpected.kt", "WhenBranchesTest.kt")
    }

    @Test
    fun `should fix open and close brace in try-catch-finally expression`() {
        fixAndCompare("TryBraceExpected.kt", "TryBraceTest.kt")
    }
}