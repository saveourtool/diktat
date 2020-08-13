package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.rules.LineLength
import org.cqfn.diktat.util.FixTestBase
import org.junit.Test

class LineLengthFixTest : FixTestBase("test/paragraph3/long_line", LineLength()) {


    @Test
    fun `should fix long comment`() {
        fixAndCompare("LongLineCommentExpected.kt", "LongLineCommentTest.kt")
    }

    @Test
    fun `should fix long binary expression`() {
        fixAndCompare("LongLineExpressionExpected.kt", "LongLineExpressionTest.kt")
    }

    @Test
    fun `should fix long function`() {
        fixAndCompare("LongLineFunExpected.kt", "LongLineFunTest.kt")
    }

    @Test
    fun `should fix long right value`() {
        fixAndCompare("LongLineRValueExpected.kt", "LongLineRValueTest.kt")
    }
}
