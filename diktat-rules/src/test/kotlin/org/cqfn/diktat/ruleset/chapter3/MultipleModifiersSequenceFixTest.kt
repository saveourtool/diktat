package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.rules.chapter3.MultipleModifiersSequence
import org.cqfn.diktat.util.FixTestBase

import org.cqfn.diktat.ruleset.constants.WarningsNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class MultipleModifiersSequenceFixTest : FixTestBase("test/paragraph3/multiple_modifiers", ::MultipleModifiersSequence) {
    @Test
    @Tag(WarningNames.WRONG_MULTIPLE_MODIFIERS_ORDER)
    fun `should fix modifiers order`() {
        fixAndCompare("ModifierExpected.kt", "ModifierTest.kt")
    }

    @Test
    @Tag(WarningNames.WRONG_MULTIPLE_MODIFIERS_ORDER)
    fun `should fix annotation order`() {
        fixAndCompare("AnnotationExpected.kt", "AnnotationTest.kt")
    }
}
