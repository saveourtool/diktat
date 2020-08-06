package org.cqfn.diktat.ruleset.chapter3


import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.EmptyBlock
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Test

class EmptyBlockFixTest : FixTestBase("test/paragraph3/empty_block", EmptyBlock()) {

    private val rulesConfigListEmptyBlockExist: List<RulesConfig> = listOf(
            RulesConfig(Warnings.EMPTY_BLOCK_STRUCTURE_ERROR.name, true,
                    mapOf("allowEmptyBlocks" to "True"))
    )

    @Test
    fun `should fix open and close brace in if-else expression`() {
        fixAndCompare("TryCatchEmptyBlockExpected.kt", "TryCatchEmptyBlockTest.kt", rulesConfigListEmptyBlockExist)
    }
}
