package org.cqfn.diktat.ruleset.chapter2

import generated.WarningNames
import org.cqfn.diktat.ruleset.rules.kdoc.KdocCodeBlocksFormatting
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class KdocCodeBlocksFormattingFixTest: FixTestBase("test/paragraph2/kdoc/", KdocCodeBlocksFormatting()) {

    @Test
    @Tag("FIXME")
    fun `there should be no blank line between kdoc and it's declaration code`() {
        fixAndCompare("KdocCodeBlocksFormattingExpected.kt", "KdocCodeBlocksFormattingTest.kt")
    }
}