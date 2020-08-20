package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.constants.StringWarnings
import org.cqfn.diktat.ruleset.rules.ClassLikeStructuresOrderRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class ClassLikeStructuresOrderFixTest : FixTestBase("test/paragraph3/file_structure", ClassLikeStructuresOrderRule()) {
    @Test
    @Tag(StringWarnings.BLANK_LINE_BETWEEN_PROPERTIES)
    fun `should fix order and newlines between properties`() {
        fixAndCompare("DeclarationsInClassOrderExpected.kt", "DeclarationsInClassOrderTest.kt")
    }
}
