package org.cqfn.diktat.ruleset.chapter3

import generated.WarningNames
import org.cqfn.diktat.ruleset.rules.ClassLikeStructuresOrderRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test

class ClassLikeStructuresOrderFixTest : FixTestBase("test/paragraph3/file_structure", ::ClassLikeStructuresOrderRule) {
    @Test
    @Tags(Tag(WarningNames.BLANK_LINE_BETWEEN_PROPERTIES), Tag(WarningNames.WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES))
    fun `should fix order and newlines between properties`() {
        fixAndCompare("DeclarationsInClassOrderExpected.kt", "DeclarationsInClassOrderTest.kt")
    }
}
