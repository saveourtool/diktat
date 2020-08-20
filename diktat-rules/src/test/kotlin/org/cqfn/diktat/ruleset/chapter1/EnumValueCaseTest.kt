package org.cqfn.diktat.ruleset.chapter1

import org.cqfn.diktat.ruleset.constants.StringWarnings
import org.cqfn.diktat.ruleset.rules.IdentifierNaming
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class EnumValueCaseTest : FixTestBase ("test/paragraph1/naming", IdentifierNaming()){

    @Test
    @Tag(StringWarnings.ENUM_VALUE)
    fun `incorrect enum value fix`() {
        fixAndCompare("enum_/EnumValueCaseExpected.kt", "enum_/EnumValueCaseTest.kt")
    }
}
