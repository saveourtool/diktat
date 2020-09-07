package org.cqfn.diktat.ruleset.chapter3

import generated.WarningNames
import org.cqfn.diktat.ruleset.rules.MultipleModifiersSequence
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class MultipleModifiersSequenceFixTest : FixTestBase("test/paragraph3/multiple_modifiers", MultipleModifiersSequence()) {


    @Test
    @Tag(WarningNames.WRONG_MULTIPLE_MODIFIERS_ORDER)
    fun `should fix long comment`() {
        fixAndCompare("ModifierExpected.kt", "ModifierTest.kt")
    }
}
