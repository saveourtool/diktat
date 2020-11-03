package org.cqfn.diktat.ruleset.chapter6

import generated.WarningNames
import org.cqfn.diktat.util.FixTestBase
import org.cqfn.diktat.ruleset.rules.classes.CompactInitialization
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CompactInitializationFixTest: FixTestBase("test/chapter6/compact_initialization", ::CompactInitialization) {
    @Test
    @Tag(WarningNames.COMPACT_OBJECT_INITIALIZATION)
    fun `should wrap properties into apply`() {
        fixAndCompare("SimpleExampleExpected.kt", "SimpleExampleTest.kt")
    }

    @Test
    @Tag(WarningNames.COMPACT_OBJECT_INITIALIZATION)
    fun `should wrap properties into apply also moving comments`() {
        fixAndCompare("ExampleWithCommentsExpected.kt", "ExampleWithCommentsTest.kt")
    }
}
