package org.cqfn.diktat.ruleset.chapter2

import org.cqfn.diktat.ruleset.rules.kdoc.KdocComments
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Test

class KdocCommentsFixTest: FixTestBase("test/paragraph2/kdoc/", ::KdocComments) {


    @Test
    fun `hehe`() {
        fixAndCompare("ConstructorCommentExpected.kt", "ConstructorCommentTest.kt")
    }

    @Test
    fun `hehe2`() {
        fixAndCompare("ConstructorCommentNoKDocExpected.kt", "ConstructorCommentNoKDocTest.kt")
    }
}