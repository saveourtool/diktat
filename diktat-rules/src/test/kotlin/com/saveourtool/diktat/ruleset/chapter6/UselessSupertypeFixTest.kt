package com.saveourtool.diktat.ruleset.chapter6

import com.saveourtool.diktat.ruleset.rules.chapter6.UselessSupertype
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class UselessSupertypeFixTest : FixTestBase("test/paragraph6/useless-override", ::UselessSupertype) {
    @Test
    @Tag(WarningNames.USELESS_SUPERTYPE)
    fun `fix example with one super`() {
        fixAndCompare("UselessOverrideExpected.kt", "UselessOverrideTest.kt")
    }

    @Test
    @Tag(WarningNames.USELESS_SUPERTYPE)
    fun `fix several super`() {
        fixAndCompare("SeveralSuperTypesExpected.kt", "SeveralSuperTypesTest.kt")
    }
}
