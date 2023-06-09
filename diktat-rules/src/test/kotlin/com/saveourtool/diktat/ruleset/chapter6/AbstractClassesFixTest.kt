package com.saveourtool.diktat.ruleset.chapter6

import com.saveourtool.diktat.ruleset.rules.chapter6.classes.AbstractClassesRule
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames.CLASS_SHOULD_NOT_BE_ABSTRACT
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class AbstractClassesFixTest : FixTestBase("test/chapter6/abstract_classes", ::AbstractClassesRule) {
    @Test
    @Tag(CLASS_SHOULD_NOT_BE_ABSTRACT)
    fun `fix abstract class`() {
        fixAndCompare("ShouldReplaceAbstractKeywordExpected.kt", "ShouldReplaceAbstractKeywordTest.kt")
    }
}
