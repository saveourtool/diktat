package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.ruleset.rules.chapter3.NullableTypeRule
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class NullableTypeRuleFixTest : FixTestBase("test/paragraph3/nullable", ::NullableTypeRule) {
    @Test
    @Tag(WarningNames.NULLABLE_PROPERTY_TYPE)
    fun `should fix primitive types`() {
        fixAndCompare("NullPrimitiveExpected.kt", "NullPrimitiveTest.kt")
    }

    @Test
    @Tag(WarningNames.NULLABLE_PROPERTY_TYPE)
    fun `should fix collections`() {
        fixAndCompare("CollectionExpected.kt", "CollectionTest.kt")
    }
}
