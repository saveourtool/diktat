package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.rules.ClassLikeStructuresOrderRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Test

class ClassLikeStructuresOrderFixTest : FixTestBase("test/paragraph3/file_structure", ClassLikeStructuresOrderRule()) {
    @Test
    fun `should fix order and newlines between properties`() {
        fixAndCompare("DeclarationsInClassOrderExpected.kt", "DeclarationsInClassOrderTest.kt")
    }
}
