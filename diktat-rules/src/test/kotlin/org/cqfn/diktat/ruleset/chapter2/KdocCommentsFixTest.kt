package org.cqfn.diktat.ruleset.chapter2

import generated.WarningNames
import org.cqfn.diktat.ruleset.rules.kdoc.KdocComments
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class KdocCommentsFixTest: FixTestBase("test/paragraph2/kdoc/", ::KdocComments) {


    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `check fix with class kdoc`() {
        fixAndCompare("ConstructorCommentExpected.kt", "ConstructorCommentTest.kt")
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `check fix without class kdoc`() {
        fixAndCompare("ConstructorCommentNoKDocExpected.kt", "ConstructorCommentNoKDocTest.kt")
    }
}
