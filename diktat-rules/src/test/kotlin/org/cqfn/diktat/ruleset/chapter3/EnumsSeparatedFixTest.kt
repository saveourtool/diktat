package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.rules.EnumsSeparated
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Test

class EnumsSeparatedFixTest : FixTestBase("test/paragraph3/enum_separated", EnumsSeparated()) {


    @Test
    fun `test enums split`() {
        fixAndCompare("EnumSeparatedExpected.kt", "EnumSeparatedTest.kt")
    }
}
