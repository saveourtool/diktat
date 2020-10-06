package org.cqfn.diktat.ruleset.chapter3

import generated.WarningNames
import org.cqfn.diktat.ruleset.rules.NullableTypeRule
import org.cqfn.diktat.util.FixTestBase
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
