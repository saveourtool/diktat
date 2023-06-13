package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.ruleset.rules.chapter3.MultipleModifiersSequence
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
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
