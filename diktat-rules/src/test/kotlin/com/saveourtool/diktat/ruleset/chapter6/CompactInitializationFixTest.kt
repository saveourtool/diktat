package com.saveourtool.diktat.ruleset.chapter6

import com.saveourtool.diktat.ruleset.rules.chapter6.classes.CompactInitialization
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CompactInitializationFixTest : FixTestBase("test/chapter6/compact_initialization", ::CompactInitialization) {
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

    @Test
    @Tag(WarningNames.COMPACT_OBJECT_INITIALIZATION)
    fun `should wrap properties into apply - existing apply with value argument`() {
        fixAndCompare("ApplyWithValueArgumentExpected.kt", "ApplyWithValueArgumentTest.kt")
    }

    @Test
    @Tag(WarningNames.COMPACT_OBJECT_INITIALIZATION)
    fun `should not move statements with this keyword into apply block`() {
        fixAndCompare("ApplyOnStatementsWithThisKeywordExpected.kt", "ApplyOnStatementsWithThisKeywordTest.kt")
    }

    @Test
    @Tag(WarningNames.COMPACT_OBJECT_INITIALIZATION)
    fun `should rename field in apply block to this keyword`() {
        fixAndCompare("StatementUseFieldMultipleTimesExpected.kt", "StatementUseFieldMultipleTimesTest.kt")
    }

    @Test
    @Tag(WarningNames.COMPACT_OBJECT_INITIALIZATION)
    fun `should wrap receiver in parentheses if required`() {
        fixAndCompare("ParenthesizedReceiverExpected.kt", "ParenthesizedReceiverTest.kt")
    }
}
