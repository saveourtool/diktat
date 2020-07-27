package org.cqfn.diktat.ruleset.chapter3


import org.cqfn.diktat.ruleset.rules.EmptyBlock
import org.cqfn.diktat.util.FixTestBase
import org.junit.Test

class EmptyBlockFixTest : FixTestBase("test/paragraph3/empty_block", EmptyBlock()){

    @Test
    fun `should fix open and close brace in if-else expression`() {
        fixAndCompare("TryCatchEmptyBlockExprected.kt", "TryCatchEmptyBlockTest.kt")
    }
}
