package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.rules.ClassLikeStructuresOrderRule
import org.cqfn.diktat.ruleset.utils.FixTestBase
import org.junit.Test

class ClassLikeStructuresOrderFixTest : FixTestBase("test/paragraph3/file_structure", ClassLikeStructuresOrderRule()) {
    @Test
    fun `should fix order and newlines between properties`() {
        fixAndCompare("DeclarationsInClassOrderExpected.kt", "DeclarationsInClassOrderTest.kt")
    }
}
